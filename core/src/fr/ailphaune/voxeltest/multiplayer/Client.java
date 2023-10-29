package fr.ailphaune.voxeltest.multiplayer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import fr.ailphaune.voxeltest.VoxelTestGame;
import fr.ailphaune.voxeltest.data.world.ClientWorld;
import fr.ailphaune.voxeltest.data.world.ConnectedClientWorld;
import fr.ailphaune.voxeltest.entities.PlayerEntity;
import fr.ailphaune.voxeltest.events.BaseEvent;
import fr.ailphaune.voxeltest.events.EventBus;
import fr.ailphaune.voxeltest.light.Lighting;
import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.multiplayer.packet.Packet.PacketData;
import fr.ailphaune.voxeltest.multiplayer.packet.Packets;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.ClientConnectionPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.s2c.JoinWorldPacket;
import fr.ailphaune.voxeltest.multiplayer.server.Server.LocalClientConnection;
import fr.ailphaune.voxeltest.registries.Identifier;
import fr.ailphaune.voxeltest.utils.io.BetterByteArrayOutputStream;
import fr.ailphaune.voxeltest.utils.io.ByteBufferInputStream;

public class Client extends Thread {

	public final String host;
	public final int port;

	private HashMap<Identifier, ArrayList<ClientPacketListener<?>>> packetListeners = new HashMap<>();

	protected BetterByteArrayOutputStream packetSendQueueBuffer = new BetterByteArrayOutputStream(512);
	protected ByteBuffer packetSendQueue;

	public Client(String host, int port) {
		super("Client-" + host + ":" + port);
		System.out.println("CREATE CLIENT: " + host + ":" + port);
		setPriority(Thread.MAX_PRIORITY);
		this.host = host;
		this.port = port;

		this.addPacketListener(Packets.JOIN_WORLD, this::onJoinWorldPacket);
	}

	private SocketChannel socketChannel;

	protected EventBus EVENT_BUS = new EventBus();

	protected AtomicBoolean stop = new AtomicBoolean(false);

	protected PlayerEntity playerEntity;
	protected ClientWorld world;

	protected byte[] buf = new byte[64 * 1024]; // 64 KB

	public synchronized void connect(String username) {
		AtomicBoolean started = new AtomicBoolean(false);
		EVENT_BUS.subscribeEvent(ClientStartEvent.class, (bus, event) -> {
			started.set(true);
		});
		this.start();
		while (!started.get()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ClientConnectionPacket.Data data = new ClientConnectionPacket.Data();
		data.userName = username;
		sendPacket(data);
	}

	protected void onJoinWorldPacket(JoinWorldPacket.Data data, Client client) {
		if (world == null) {
			world = new ConnectedClientWorld(this, new Lighting());
		}
		playerEntity = new PlayerEntity(data.uuid, data.position, data.hp);
		playerEntity.setClient(this);
		playerEntity.yaw = data.yaw;
		playerEntity.pitch = data.pitch;
		world.clear();
	}

	public PlayerEntity getPlayerEntity() {
		return playerEntity;
	}

	public ClientWorld getWorld() {
		return world;
	}

	@Override
	public void run() {
		System.out.println("Connecting to " + host + ":" + port);
		try {
			InetSocketAddress serverAddress = new InetSocketAddress(host, port);

			// Open a non-blocking socket channel
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);

			// Connect to the server
			socketChannel.connect(serverAddress);

			// Wait for the connection to be established
			while (!socketChannel.finishConnect()) {
				Thread.sleep(20);
			}

			System.out.println("Connected to " + host + ":" + port);

			VoxelTestGame.GAME_LIFECYCLE_EVENT_BUS.dispatchEvent(new ClientStartEvent(this));
			EVENT_BUS.dispatchEvent(new ClientStartEvent(this));

			while (socketChannel.isOpen()) {
				if (stop.get()) {
					socketChannel.close();
					return;
				}

				try {
					synchronized (packetSendQueueBuffer) {
						if (packetSendQueueBuffer.size() != 0) {
							int newBufferSize = (packetSendQueue == null ? 0 : packetSendQueue.remaining())
									+ packetSendQueueBuffer.size();
							if (newBufferSize > getMaxPacketSize()) {
								disconnect();
								return;
							}
							BetterByteArrayOutputStream baos = new BetterByteArrayOutputStream(newBufferSize);
							if (packetSendQueue != null) {
								ByteBufferInputStream bbis = new ByteBufferInputStream(packetSendQueue);
								byte[] buf = new byte[1024];
								int nRead;
								while ((nRead = bbis.read(buf)) > 0) {
									baos.write(buf, 0, nRead);
								}
								bbis.close();
							}
							baos.write(packetSendQueueBuffer.getBuffer(), 0, packetSendQueueBuffer.size());
							packetSendQueue = ByteBuffer.wrap(baos.getBuffer());
							packetSendQueueBuffer.reset(512);
						}
					}
					if (packetSendQueue != null && packetSendQueue.remaining() > 0) {
						socketChannel.write(packetSendQueue);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				int bytesRead;
				while ((bytesRead = socketChannel.read(ByteBuffer.wrap(buf))) > 0) {
					receiveBuffer(buf, bytesRead);
				}
				if (bytesRead == -1) {
					System.out.println("Server closed connection: " + socketChannel.getRemoteAddress());
					// Connection closed by client
					socketChannel.close();
				} else if (bytesRead == 0) {
					try {
						parsePackets();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
				Thread.sleep(30);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int getMaxPacketSize() {
		return 32 * 1024 * 1024; // 32 Mb
	}

	private BetterByteArrayOutputStream baos = new BetterByteArrayOutputStream(1024);

	protected void receiveBuffer(byte[] buffer, int len) {
		synchronized (baos) {
			baos.write(buffer, 0, len);
		}
	}

	protected void parsePackets() throws IOException {
		synchronized (baos) {
			ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
			PacketData data;
			IOException e = null;
			try {
				while ((data = PacketDecoder.decode(buffer)) != null) {
					execPacketListener(data);
				}
			} catch (InvalidPacketException e_) {
				disconnect();
				e_.printStackTrace();
				return;
			} catch (IOException e_) {
				e = e_;
			}
			baos.reset(512);
			while (buffer.remaining() > 0) {
				int nRead = Math.min(buffer.remaining(), buf.length);
				buffer.get(buf, 0, nRead);
				baos.write(buf, 0, nRead);
			}
			if (e != null)
				throw e;
		}
	}

	private void execPacketListener(PacketData data) throws IOException {
		ArrayList<ClientPacketListener<?>> listeners = packetListeners.getOrDefault(data.getPacket().getIdentifier(),
				null);
		if (listeners == null)
			return;
		for (int i = 0; i < listeners.size(); i++) {
			@SuppressWarnings("unchecked")
			ClientPacketListener<PacketData> listener = (ClientPacketListener<PacketData>) listeners.get(i);
			if (listener == null)
				continue;
			listener.onPacket(data, this);
		}
	}

	public <T extends PacketData> void addPacketListener(Packet<T> packet, ClientPacketListener<T> listener) {
		ArrayList<ClientPacketListener<?>> listeners = packetListeners.getOrDefault(packet.getIdentifier(), null);
		if (listeners == null) {
			listeners = new ArrayList<>();
			packetListeners.put(packet.getIdentifier(), listeners);
		}
		listeners.add(listener);
	}

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

	public void disconnect() throws IOException {
		this.stop.set(true);
	}

	public boolean isConnected() {
		return super.isAlive() && !this.stop.get() && this.socketChannel != null && this.socketChannel.isConnected();
	}

	public static class LocalClient extends Client {

		protected ConcurrentLinkedQueue<PacketData> pendingPackets = new ConcurrentLinkedQueue<>();

		protected Consumer<PacketData> packetReceiver;
		protected LocalClientConnection lcc;

		public LocalClient(Consumer<PacketData> packetReceiver, LocalClientConnection lcc) {
			super(null, 0);
			this.packetReceiver = packetReceiver;
			this.lcc = lcc;
		}

		@Override
		public void run() {
			VoxelTestGame.GAME_LIFECYCLE_EVENT_BUS.dispatchEvent(new ClientStartEvent(this));
			EVENT_BUS.dispatchEvent(new ClientStartEvent(this));

			while (!this.stop.get()) {
				while (!pendingPackets.isEmpty()) {
					try {
						super.execPacketListener(pendingPackets.poll());
					} catch (IOException e) {
						e.printStackTrace();
						disconnect();
					}
				}

				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public boolean isConnected() {
			return !this.stop.get();
		}

		@Override
		public void sendPacket(PacketData data) {
			this.packetReceiver.accept(data);
		}

		@Override
		protected void parsePackets() {
		}

		@Override
		protected void receiveBuffer(byte[] buffer, int len) {
		}

		@Override
		public void disconnect() {
			this.stop.set(true);
			if (this.lcc == null)
				return;
			LocalClientConnection _lcc = lcc;
			this.lcc = null;
			_lcc.close();
		}

		public void receivePacketFromServer(PacketData data) {
			pendingPackets.add(data);
		}
	}

	public static class ClientStartEvent extends BaseEvent {

		private Client client;

		public ClientStartEvent(Client client) {
			super(false);
			this.client = client;
		}

		public Client getClient() {
			return client;
		}

	}
}