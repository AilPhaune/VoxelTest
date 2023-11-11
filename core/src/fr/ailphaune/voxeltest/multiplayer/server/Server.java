package fr.ailphaune.voxeltest.multiplayer.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.VoxelTestGame;
import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.Registries;
import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.collections.SynchronizedHashSet;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.entities.PlayerEntity;
import fr.ailphaune.voxeltest.events.BaseEvent;
import fr.ailphaune.voxeltest.events.EventBus;
import fr.ailphaune.voxeltest.multiplayer.Client;
import fr.ailphaune.voxeltest.multiplayer.InvalidPacketException;
import fr.ailphaune.voxeltest.multiplayer.PacketDecoder;
import fr.ailphaune.voxeltest.multiplayer.PacketEncoder;
import fr.ailphaune.voxeltest.multiplayer.ServerPacketListener;
import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.multiplayer.packet.Packet.PacketData;
import fr.ailphaune.voxeltest.multiplayer.packet.Packets;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.ClientConnectionPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.DestroyVoxelPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.PlaceVoxelPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.PlayerMovePacket;
import fr.ailphaune.voxeltest.multiplayer.packet.s2c.JoinWorldPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.s2c.VoxelUpdatesPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.s2c.WorldChunkPacket;
import fr.ailphaune.voxeltest.multiplayer.server.events.ClientPreConnectEvent;
import fr.ailphaune.voxeltest.multiplayer.server.events.ServerChunkLoadedEvent;
import fr.ailphaune.voxeltest.registries.Identifier;
import fr.ailphaune.voxeltest.utils.io.BetterByteArrayOutputStream;
import fr.ailphaune.voxeltest.utils.io.ByteBufferInputStream;
import fr.ailphaune.voxeltest.voxels.Voxels;

public class Server extends Thread {

	public static int k;

	public final int port;

	private int maxPSize = 4 * 1024 * 1024; // Default max packet size: 4Mb
	private int connectionTimeout = 15 * 1000; // 15 seconds before closing the connection if no ClientConnectionPacket
												// is received
	public final World world;

	public boolean open = false;

	protected int simulationDistanceXZ = 5, simulationDistanceY = 5;

	private Vector3 tempVec3_1 = new Vector3();
	private Vector3 tempVec3_2 = new Vector3();

	private ChunkPos tempChunkPos_1 = new ChunkPos();
	
	private VoxelPos tempVoxelPos_1 = new VoxelPos();
	
	public Server(World world, int port) {
		super("Server-" + port);
		setPriority(Thread.MAX_PRIORITY);
		this.port = port;
		this.world = world;
		world.ticker.useServer(this);

		addPacketListener(Packets.CLIENT_CONNECTION, this::onClientInitConnection);
		addPacketListener(Packets.PLAYER_MOVE_PACKET, this::onPlayerMovePacket);
		addPacketListener(Packets.DESTROY_VOXEL, this::onPlayerDestroyPacket);
		addPacketListener(Packets.PLACE_VOXEL, this::onPlayerPlacePacket);
		
	}

	public Server(World world, int port, int maxPacketSize) {
		this(world, port);
		maxPSize = maxPacketSize;
	}

	public void setSimulationDistance(int distance) {
		setSimulationDistance(distance, distance);
	}

	public void setSimulationDistance(int distanceXZ, int distanceY) {
		this.simulationDistanceXZ = distanceXZ;
		this.simulationDistanceY = distanceY;
	}

	public int getSimulationDistanceXZ() {
		return simulationDistanceXZ;
	}

	public int getSimulationDistanceY() {
		return simulationDistanceY;
	}

	protected Selector selector;
	protected ServerSocketChannel serverSocketChannel;

	protected LocalClientConnection lcc;

	/**
	 * A list of all successfully connected clients
	 */
	protected ArrayList<ClientConnection> clients = new ArrayList<>();
	/**
	 * A set of all connected clients that have not yet initiated the connection
	 * process
	 */
	protected HashSet<ClientConnection> connecting = new HashSet<>();
	/**
	 * A mapping form username->{@link ClientConnection}
	 */
	protected ConcurrentHashMap<String, ClientConnection> usernameMap = new ConcurrentHashMap<>();

	/**
	 * A mapping from {@link ChunkPos} to a set of all {@link ClientConnection}s
	 * interested in events from the given chunk
	 */
	protected ConcurrentHashMap<ChunkPos, HashSet<ClientConnection>> chunkInterest = new ConcurrentHashMap<>();

	protected AtomicBoolean opened = new AtomicBoolean(false), stop = new AtomicBoolean(false);

	protected EventBus SERVER_EVENTS = new EventBus();

	public EventBus getEventBus() {
		return SERVER_EVENTS;
	}

	/**
	 * Returns the connection timeout in milliseconds. <br>
	 * See {@link #disconnectFailedConnections()}
	 * 
	 * @return
	 */
	public int getConnectionTimeout() {
		return this.connectionTimeout;
	}

	/**
	 * Sets a new connection timeout in milliseconds and returns the previous one.
	 * <br>
	 * See {@link #getConnectionTimeout()}
	 * 
	 * @param newTimeout The new timeout in milliseconds
	 * @return The previous timeout in milliseconds
	 */
	public int setConnectionTimeout(int newTimeout) {
		int last = this.connectionTimeout;
		this.connectionTimeout = newTimeout;
		return last;
	}

	/**
	 * Returns the {@link LocalClientConnection} for this server of creates one if
	 * there isn't one yet
	 * 
	 * @return this server's {@link LocalClientConnection}
	 */
	public LocalClientConnection getLocalClientConnection() {
		if (lcc != null)
			return lcc;
		lcc = new LocalClientConnection();
		connecting.add(lcc);
		lcc.lc = new Client.LocalClient(lcc.pendingPackets::add, lcc);
		return lcc;
	}

	public void tick() {
		
	}
	
	/**
	 * Returns the {@link Client} associated to this server's
	 * {@link LocalClientConnection}. <br>
	 * See {@link #getLocalClientConnection()}.
	 * 
	 * @return this server {@link LocalClientConnection}'s {@link Client}
	 */
	public Client.LocalClient getLocalClient() {
		return getLocalClientConnection().lc;
	}

	/**
	 * Returns the {@link Client} associated to this server's
	 * {@link LocalClientConnection} and connects it to the server using the given
	 * parameters. <br>
	 * See {@link #getLocalClientConnection()}.
	 * 
	 * @param username The username to use for the connection
	 * @return this server {@link LocalClientConnection}'s {@link Client}
	 */
	public Client.LocalClient getConnectedLocalClient(String username) throws IOException {
		LocalClientConnection lcc = getLocalClientConnection();
		if (lcc.username == null) {
			lcc.lc.connect(username);
		}
		return lcc.lc;
	}

	/**
	 * Event listener for a {@link ClientConnectionPacket} packet
	 * 
	 * @param data       The data of the packet
	 * @param connection The {@link ClientConnection} that received the packet
	 */
	protected void onClientInitConnection(ClientConnectionPacket.Data data, ClientConnection connection) {
		if (usernameMap.containsKey(data.userName)) {
			System.out.println("Username " + data.userName + " already taken");
			connection.close();
			return;
		}
		ClientPreConnectEvent event = new ClientPreConnectEvent(connection, data);
		getEventBus().dispatchEvent(event);
		if (event.isCancelled()) {
			connection.close();
			return;
		}
		if (connection instanceof LocalClientConnection) {
			System.out.println("Local client connected using username " + data.userName);
		} else {
			System.out.println("Client connected using username " + data.userName);
		}
		connecting.remove(connection);
		connection.username = data.userName;
		usernameMap.put(data.userName, connection);
		connection.initOnlinePlayer();
		clients.add(connection);

		JoinWorldPacket.Data packet = new JoinWorldPacket.Data();
		packet.uuid = UUID.randomUUID();
		packet.hp = connection.onlinePlayer.entity.hp;
		connection.onlinePlayer.entity.getPosition(packet.position);
		packet.yaw = connection.onlinePlayer.entity.yaw;
		packet.pitch = connection.onlinePlayer.entity.pitch;
		connection.sendPacket(packet);
		connection.updateWorldInterest();
		connection.sendWorldData();
	}

	private ChunkPos _onPlayerMovePacket_tempChunkPos = new ChunkPos();

	/**
	 * Event listener for a {@link ClientMovePacket} packet
	 * 
	 * @param data       The data of the packet
	 * @param connection The {@link ClientConnection} that received the packet
	 */
	protected void onPlayerMovePacket(PlayerMovePacket.Data data, ClientConnection connection) throws IOException {
		if (connection.onlinePlayer == null) {
			return;
		}
		connection.onlinePlayer.entity.setPosition(data.position);
		ChunkPos newPos = connection.onlinePlayer.entity.getVoxelPos(VoxelPos.TEMP)
				.getChunkPos(_onPlayerMovePacket_tempChunkPos);
		ChunkPos lastPos = connection.onlinePlayer.loadRegion.playerChunk;
		if (!newPos.equals(lastPos)) {
			lastPos.set(newPos);
			connection.updateWorldInterest();
			connection.sendWorldData();
		}
	}
	
	/**
	 * Event listener for a {@link DestroyVoxelPacket} packet
	 * 
	 * @param data       The data of the packet
	 * @param connection The {@link ClientConnection} that received the packet
	 */
	protected void onPlayerDestroyPacket(DestroyVoxelPacket.Data data, ClientConnection connection) {
		OnlinePlayer online = connection.getOnlinePlayer();
		PlayerEntity player = online.getPlayerEntity();
		
		// Prepare update
		VoxelUpdatesPacket.Update u = new VoxelUpdatesPacket.Update();
		u.x = data.position.x;
		u.y = data.position.y;
		u.z = data.position.z;
		
		int reachD = player.getMaxReachDestroy();
		
		if(data.position.asVec3(tempVec3_1).dst2(player.getPosition(tempVec3_2)) > reachD*reachD) {
			// Player is too far
			if(!online.loadRegion.contains(data.position)) {
				// Player is not supposed to know what's there, so don't send him the block update
				return;
			}
			u.voxel = world.getVoxel(data.position);
			u.state = world.getVoxelState(data.position);
			connection.sendUpdate(u);
			return;
		}

		// TODO: create event class and dispatch event to the event bus
		
		world.setVoxel(data.position, Voxels.AIR);
		u.voxel = Voxels.AIR;
		u.state = 0;
		
		sendVoxelUpdate(u);
	}
	
	/**
	 * Event listener for a {@link PlaceVoxelPacket} packet
	 * 
	 * @param data       The data of the packet
	 * @param connection The {@link ClientConnection} that received the packet
	 */
	protected void onPlayerPlacePacket(PlaceVoxelPacket.Data data, ClientConnection connection) {
		OnlinePlayer online = connection.getOnlinePlayer();
		PlayerEntity player = online.getPlayerEntity();
		
		// Prepare update
		VoxelUpdatesPacket.Update u = new VoxelUpdatesPacket.Update();
		u.x = data.position.x;
		u.y = data.position.y;
		u.z = data.position.z;
		
		int reachD = player.getMaxReachDestroy();
		
		if(data.position.asVec3(tempVec3_1).dst2(player.getPosition(tempVec3_2)) > reachD*reachD) {
			// Player is too far
			if(!online.loadRegion.contains(data.position)) {
				// Player is not supposed to know what's there, so don't send him the block update
				return;
			}
			u.voxel = world.getVoxel(data.position);
			u.state = world.getVoxelState(data.position);
			connection.sendUpdate(u);
			return;
		}

		// TODO: create event class and dispatch event to the event bus
		
		world.setVoxel(data.position, data.voxel);
		u.voxel = data.voxel;
		u.state = 0;
	
		sendVoxelUpdate(u);
	}
	
	protected void onChunkLoaded(EventBus bus, ServerChunkLoadedEvent event) {
		sendChunkUpdate(event.chunk);
	}
	
	public void sendVoxelUpdate(VoxelUpdatesPacket.Update update) {
		ChunkPos pos = tempVoxelPos_1.set(update.x, update.y, update.z).getChunkPos(tempChunkPos_1);
		HashSet<ClientConnection> interested = chunkInterest.getOrDefault(pos, null);
		if(interested != null) {
			for(ClientConnection connection : interested) {
				connection.sendUpdate(update);
			}
		}
	}
	
	public void sendChunkUpdate(Chunk chunk) {
		HashSet<ClientConnection> interested = chunkInterest.getOrDefault(chunk.getChunkPos(), null);
		if(interested != null) {
			for(ClientConnection connection : interested) {
				connection.sendChunk(chunk);
			}
		}
	}

	protected void addChunkInterest(ClientConnection connection, ChunkPos chunk) {
		connection.interestedChunks.add(chunk);
		HashSet<ClientConnection> connections = chunkInterest.getOrDefault(chunk, null);
		if(connections == null) {
			connections = new SynchronizedHashSet<ClientConnection>();
			chunkInterest.put(chunk, connections);
		}
		connections.add(connection);
	}
	
	protected void removeChunkInterest(ClientConnection connection, ChunkPos chunk) {
		connection.interestedChunks.remove(chunk);
		HashSet<ClientConnection> connections = chunkInterest.getOrDefault(chunk, null);
		if(connections == null) {
			return;
		}
		connections.remove(connection);
		if(connections.size() == 0) {
			chunkInterest.remove(chunk);
		}
	}
	
	private int lastLen = -1;
	/**
	 * Disconnects all the connected clients that have not initiated the connection
	 * process in time. <br>
	 * See {@link #getConnectionTimeout()} and @link #setConnectionTimeout()}
	 */
	protected void disconnectFailedConnections() {
		if (lastLen != this.connecting.size()) {
			lastLen = this.connecting.size();
			System.out.println("Connecting queue: " + this.connecting);
		}
		ArrayList<ClientConnection> connections = new ArrayList<>();
		for (ClientConnection connection : this.connecting) {
			if (System.currentTimeMillis() - connection.createdTimesamp >= getConnectionTimeout()) {
				connections.add(connection);
			}
		}
		for (ClientConnection connection : connections) {
			this.connecting.remove(connection);
			connection.close();
		}
	}

	/**
	 * Removes a {@link ClientConnection} from the server. <br>
	 * THIS METHOD DOES NOT CLOSE THE CONNECTION AND THE UNDERLYING SOCKET CHANNEL.
	 * <br>
	 * Instead, use {@link ClientConnection#close()}
	 * 
	 * @param connection The connection
	 */
	protected void removeClientConnection(ClientConnection connection) {
		connecting.remove(connection);
		clients.remove(connection);
		if (connection.username != null) {
			usernameMap.remove(connection.username);
		}
		if (connection.onlinePlayer != null) {
			world.loadRegions.remove(connection.onlinePlayer.loadRegion);
		}
	}

	@Override
	public void run() {
		SERVER_EVENTS.subscribeEvent(ServerChunkLoadedEvent.class, this::onChunkLoaded);
		
		VoxelTestGame.GAME_LIFECYCLE_EVENT_BUS.dispatchEvent(new ServerStartEvent(this));
		SERVER_EVENTS.dispatchEvent(new ServerStartEvent(this));

		while (!stop.get() && !opened.get()) {
			try {
				disconnectFailedConnections();
				if (lcc != null) {
					lcc.parsePackets();
					lcc.preparePackets();
				}
				for (ClientConnection connection : clients) {
					if (connection == lcc)
						continue;
					connection.parsePackets();
				}

				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				shutdown();
				return;
			}
		}

		if (!opened.get())
			return;

		try {
			// Create a selector
			selector = Selector.open();

			// Create a server socket channel and bind it to a port
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.bind(new InetSocketAddress("localhost", this.port));
			serverSocketChannel.configureBlocking(false);

			// Register the server socket channel with the selector for accepting
			// connections
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("Server started");

			while (serverSocketChannel.isOpen()) {
				if (!opened.get() || stop.get()) {
					serverSocketChannel.close();
					return;
				}
				disconnectFailedConnections();
				// Wait for events
				selector.select(20);
				if (lcc != null) {
					lcc.parsePackets();
					lcc.preparePackets();
				}

				// Get the selected keys
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				// Process each key
				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();

					if (key.isAcceptable()) {
						// Accept a new connection
						ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
						SocketChannel clientChannel = serverChannel.accept();
						clientChannel.configureBlocking(false);

						ClientConnection connection = new ClientConnection(clientChannel);
						clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
						this.connecting.add(connection);

						System.out.println("New client connected: " + clientChannel.getRemoteAddress());
					} else if (key.isReadable()) {
						// Read data from the client
						SocketChannel clientChannel = (SocketChannel) key.channel();
						ClientConnection connection = (ClientConnection) key.attachment();

						try {
							int bytesRead = -1;
							if (clientChannel.isConnected()) {
								while (clientChannel.isConnected()
										&& (bytesRead = clientChannel.read(ByteBuffer.wrap(connection.buf))) > 0) {
									connection.receiveBuffer(connection.buf, bytesRead);
								}
							}
							if (bytesRead == -1) {
								System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
								// Connection closed by client
								connection.close();
							} else if (bytesRead == 0) {
								try {
									connection.parsePackets();
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								}
							}
						} catch (Throwable t) {
							t.printStackTrace();
							connection.close();
						}
					} else if (key.isWritable()) {
						// Read data from the client
						SocketChannel clientChannel = (SocketChannel) key.channel();
						ClientConnection connection = (ClientConnection) key.attachment();
						connection.preparePackets();
						
						try {
							synchronized (connection.packetSendQueueBuffer) {
								if (connection.packetSendQueueBuffer.size() != 0) {
									int newBufferSize = (connection.packetSendQueue == null ? 0
											: connection.packetSendQueue.remaining())
											+ connection.packetSendQueueBuffer.size();
									if (newBufferSize > getMaxPacketSize()) {
										connection.close();
										return;
									}
									BetterByteArrayOutputStream baos = new BetterByteArrayOutputStream(
											newBufferSize);
									if (connection.packetSendQueue != null) {
										ByteBufferInputStream bbis = new ByteBufferInputStream(
												connection.packetSendQueue);
										byte[] buf = new byte[1024];
										int nRead;
										while ((nRead = bbis.read(buf)) > 0) {
											baos.write(buf, 0, nRead);
										}
										bbis.close();
									}
									baos.write(connection.packetSendQueueBuffer.getBuffer(), 0,
											connection.packetSendQueueBuffer.size());
									
									connection.packetSendQueue = ByteBuffer.wrap(baos.getBuffer());
									connection.packetSendQueueBuffer.reset(512);
								}
							}
							if (connection.packetSendQueue != null && connection.packetSendQueue.remaining() > 0) {
								clientChannel.write(connection.packetSendQueue);
							}
						} catch (Throwable t) {
							t.printStackTrace();
							connection.close();
						}
					}

					// Remove the processed key
					keyIterator.remove();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cleans up the resources taken by the server. <br>
	 * This method closes all connections.
	 */
	public synchronized void dispose() {
		synchronized (clients) {
			for (int i = clients.size() - 1; i >= 0; i--) {
				clients.get(i).close();
			}
		}
		try {
			serverSocketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		usernameMap.clear();
		connecting.clear();
	}

	/**
	 * Returns the max size of the receiving packet buffer for each client.
	 * 
	 * @return the max size of the receiving packet buffer in bytes.
	 */
	public int getMaxPacketSize() {
		return maxPSize;
	}

	/**
	 * Opens the server socket channel. The server will now accept connections.
	 */
	public void open() {
		opened.set(true);
	}

	public void shutdown() {
		opened.set(false);
		stop.set(true);
		while (isAlive()) {
			Thread.yield();
			try {
				join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private HashMap<Identifier, ArrayList<ServerPacketListener<?>>> packetListeners = new HashMap<>();

	private void execPacketListener(ClientConnection connection, PacketData data) throws IOException {
		ArrayList<ServerPacketListener<?>> listeners = packetListeners.getOrDefault(data.getPacket().getIdentifier(),
				null);
		if (listeners == null)
			return;
		for (int i = 0; i < listeners.size(); i++) {
			@SuppressWarnings("unchecked")
			ServerPacketListener<PacketData> listener = (ServerPacketListener<PacketData>) listeners.get(i);
			if (listener == null)
				continue;
			listener.onPacket(data, connection);
		}
	}

	/**
	 * Adds a listener to the given type of packet, which will be executed when a
	 * packet of that type is received.
	 * 
	 * @param <T>      The type of packet data you're listening for
	 * @param packet   The packet class you're listening to
	 * @param listener The listener that will be executed
	 * @throws IllegalArgumentException if the given packet is not registered in the
	 *                                  {@link Registries#PACKETS} registry
	 */
	public <T extends PacketData> void addPacketListener(Packet<T> packet, ServerPacketListener<T> listener) {
		Objects.requireNonNull(packet, "Packet is null");
		Objects.requireNonNull(listener, "Packet listener is null");
		if (packet.getRegistry() != Registries.PACKETS)
			throw new IllegalArgumentException(
					"Packet " + packet.getClass().getName() + " is not registered in the Registries.PACKETS registry");
		ArrayList<ServerPacketListener<?>> listeners = packetListeners.getOrDefault(packet.getIdentifier(), null);
		if (listeners == null) {
			listeners = new ArrayList<>();
			packetListeners.put(packet.getIdentifier(), listeners);
		}
		listeners.add(listener);
	}

	public static class ServerStartEvent extends BaseEvent {
		private Server server;

		public ServerStartEvent(Server server) {
			super(false);
			this.server = server;
		}

		public Server getServer() {
			return server;
		}
	}

	public class ClientConnection {

		protected OnlinePlayer onlinePlayer;

		protected String username;

		public long createdTimesamp;
		protected BetterByteArrayOutputStream baos, packetSendQueueBuffer;
		private SocketChannel clientChannel;

		private boolean closed = false;

		protected byte[] buf = new byte[4096]; // 4 KB

		protected ByteBuffer packetSendQueue;

		private VoxelPos tempVoxelPos = new VoxelPos();

		private HashSet<ChunkPos> acquiredChunks = new HashSet<>();
		private HashSet<ChunkPos> interestedChunks = new HashSet<>();

		private ClientConnection(SocketChannel clientChannel) {
			this.clientChannel = clientChannel;
			this.createdTimesamp = System.currentTimeMillis();

			baos = new BetterByteArrayOutputStream(1024);
			packetSendQueueBuffer = new BetterByteArrayOutputStream(4096);
		}

		/**
		 * Updates the the {@link #interestedChunks} field and the server's {@link Server#chunkInterest} map
		 */
		protected void updateWorldInterest() {
			ArrayList<ChunkPos> toRemove = new ArrayList<>();
			for (ChunkPos pos : acquiredChunks) {
				if (!onlinePlayer.loadRegion.keepsChunkLoaded(pos)) {
					toRemove.add(pos);
				}
			}
			for (ChunkPos pos : toRemove) {
				getServer().removeChunkInterest(this, pos);
			}
			for (int dx = -simulationDistanceXZ; dx <= simulationDistanceXZ; dx++) {
				for (int dz = -simulationDistanceXZ; dz <= simulationDistanceXZ; dz++) {
					for (int dy = -simulationDistanceY; dy <= simulationDistanceY; dy++) {
						ChunkPos pos = onlinePlayer.entity.getVoxelPos(tempVoxelPos).getChunkPos(new ChunkPos()).add(dx, dy, dz);
						getServer().addChunkInterest(this, pos);
					}
				}
			}
		}
		
		/**
		 * Sends world data to the client
		 */
		public void sendWorldData() {
			World world = onlinePlayer.getWorld();
			ArrayList<ChunkPos> toRemove = new ArrayList<>();
			for (ChunkPos pos : acquiredChunks) {
				if (!onlinePlayer.loadRegion.keepsChunkLoaded(pos)) {
					toRemove.add(pos);
				}
			}
			for (ChunkPos pos : toRemove) {
				acquiredChunks.remove(pos);
			}
			for (int dx = -simulationDistanceXZ; dx <= simulationDistanceXZ; dx++) {
				for (int dz = -simulationDistanceXZ; dz <= simulationDistanceXZ; dz++) {
					for (int dy = -simulationDistanceY; dy <= simulationDistanceY; dy++) {
						ChunkPos pos = onlinePlayer.entity.getVoxelPos(tempVoxelPos).getChunkPos(new ChunkPos()).add(dx, dy, dz);
						if (acquiredChunks.contains(pos) || !onlinePlayer.loadRegion.keepsChunkLoaded(pos))
							continue;
						Chunk chunk = world.getChunk(pos);
						sendChunk(chunk);
					}
				}
			}
		}

		protected void initOnlinePlayer() {
			onlinePlayer = new OnlinePlayer(new PlayerEntity(UUID.randomUUID(), new Vector3(0.5f, 80.0f, 0.5f), 20),
					world, this);
			world.loadRegions.add(onlinePlayer.loadRegion);
		}

		protected void receiveBuffer(byte[] buf, int len) throws IOException {
			synchronized (baos) {
				baos.write(buf, 0, len);
				if (baos.size() > getServer().getMaxPacketSize()) {
					parsePackets();
				}
				if (baos.size() > getServer().getMaxPacketSize()) {
					close();
				}
			}
		}

		/**
		 * Closes the connection, disconnects the client, and cleans up resources used by this connection
		 */
		public void close() {
			closed = true;
			getServer().removeClientConnection(this);
			try {
				if (clientChannel != null)
					clientChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			dispose();
		}

		/**
		 * @return true if the connection is closed, false if it's still open
		 */
		public boolean isClosed() {
			return closed;
		}

		/**
		 * Returns the {@link Server} that handles this connection
		 * @return the {@link Server} that handles this connection
		 */
		public Server getServer() {
			return Server.this;
		}

		public OnlinePlayer getOnlinePlayer() {
			return onlinePlayer;
		}

		protected void parsePackets() throws IOException {
			synchronized (baos) {
				ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
				PacketData data;
				IOException e = null;
				try {
					while ((data = PacketDecoder.decode(buffer)) != null && !closed) {
						Server.this.execPacketListener(this, data);
					}
				} catch (InvalidPacketException e_) {
					close();
					e_.printStackTrace();
					return;
				} catch (IOException e_) {
					e = e_;
				}
				baos = new BetterByteArrayOutputStream(512);
				while (buffer.remaining() > 0) {
					int nRead = Math.min(buffer.remaining(), buf.length);
					buffer.get(buf, 0, nRead);
					baos.write(buf, 0, nRead);
				}
				if (e != null)
					throw e;
			}
		}

		protected void dispose() {
			if (baos != null) {
				baos.reset(0);
				baos = null;
			}
			if (packetSendQueueBuffer != null) {
				packetSendQueueBuffer.reset(0);
				packetSendQueueBuffer = null;
			}
			packetSendQueue = null;
			buf = null;
		}
		
		protected void preparePackets() {
			synchronized(pendingUpdates) {
				if(pendingUpdates.size() > 0) {
					VoxelUpdatesPacket.Data data = new VoxelUpdatesPacket.Data();
					data.updates = pendingUpdates.toArray(new VoxelUpdatesPacket.Update[pendingUpdates.size()]);
					pendingUpdates = new ArrayList<>();
					sendPacket(data);
				}
			}
		}

		/**
		 * Sends the given {@link PacketData} to the client asynchronously.
		 * @param data The data to send
		 */
		public void sendPacket(PacketData data) {
			try {
				BetterByteArrayOutputStream bbaos = new BetterByteArrayOutputStream(512);
				if (PacketEncoder.encode(data, bbaos)) {
					synchronized (this.packetSendQueueBuffer) {
						bbaos.writeTo(this.packetSendQueueBuffer);
					}
				} else {
					throw new IOException("Can't send packet " + data);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		protected List<VoxelUpdatesPacket.Update> pendingUpdates = new ArrayList<>();
		
		public void sendUpdate(VoxelUpdatesPacket.Update update) {
			synchronized(pendingUpdates) {
				pendingUpdates.add(update);
			}
		}
		
		public void sendChunk(Chunk chunk) {
			WorldChunkPacket.Data data = Packets.WORLD_CHUNK.fromChunk(chunk);
			sendPacket(data);
			acquiredChunks.add(chunk.getChunkPos());
		}
	}

	public class LocalClientConnection extends ClientConnection {

		protected ConcurrentLinkedQueue<PacketData> pendingPackets = new ConcurrentLinkedQueue<>();

		protected Consumer<PacketData> packetReceiver;
		protected Client.LocalClient lc;

		private LocalClientConnection() {
			super(null);
			baos = null;
			buf = null;
		}

		@Override
		protected void receiveBuffer(byte[] buf, int len) {
		}

		@Override
		protected void parsePackets() throws IOException {
			while (!pendingPackets.isEmpty() && !this.isClosed()) {
				Server.this.execPacketListener(this, pendingPackets.poll());
			}
		}

		@Override
		public void close() {
			super.close();
			if (getServer().lcc == this) {
				getServer().lcc = null;
			}
			if (lc == null)
				return;
			Client.LocalClient _lc = lc;
			lc = null;
			_lc.disconnect();
		}

		@Override
		protected void dispose() {
		}

		@Override
		public void sendPacket(PacketData data) {
			lc.receivePacketFromServer(data);
		}
	}
}