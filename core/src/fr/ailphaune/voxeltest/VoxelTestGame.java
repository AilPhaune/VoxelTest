package fr.ailphaune.voxeltest;

import java.io.IOException;
import java.util.Objects;

import com.badlogic.gdx.Game;

import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.events.EventBus;
import fr.ailphaune.voxeltest.events.SubscribeEvent;
import fr.ailphaune.voxeltest.mods.events.ContentInitializeEvent;
import fr.ailphaune.voxeltest.mods.events.ContentInitializeEvent.GameDisposedEvent;
import fr.ailphaune.voxeltest.mods.events.ContentInitializeEvent.PostContentInitializeEvent;
import fr.ailphaune.voxeltest.multiplayer.Client;
import fr.ailphaune.voxeltest.multiplayer.Client.LocalClient;
import fr.ailphaune.voxeltest.multiplayer.packet.Packets;
import fr.ailphaune.voxeltest.multiplayer.server.Server;
import fr.ailphaune.voxeltest.render.mesh.chunk.RenderLayers;
import fr.ailphaune.voxeltest.render.voxel.VoxelShaders;
import fr.ailphaune.voxeltest.render.voxel.VoxelTextures;
import fr.ailphaune.voxeltest.screens.Screens;
import fr.ailphaune.voxeltest.textures.GameTextures;
import fr.ailphaune.voxeltest.threading.ThreadPools;
import fr.ailphaune.voxeltest.voxels.Voxels;
import fr.ailphaune.voxeltest.worldgen.biome.Biomes;

public class VoxelTestGame extends Game {

	public static final EventBus GAME_LIFECYCLE_EVENT_BUS = new EventBus();
	
	private static VoxelTestGame INSTANCE;
	
	public static VoxelTestGame getInstance() {
		return INSTANCE;
	}
	
	public final boolean IS_DEBUG;

	private String username;
	
	protected Client client;
	protected Server server;
	
	public VoxelTestGame(boolean debugMode) {
		if(INSTANCE != null) throw new RuntimeException("Only one instance of the Game is allowed");
		INSTANCE = this;
		IS_DEBUG = debugMode;
		if(debugMode) {
			username = "DebugPlayer" + (int)Math.floor(100 + Math.random() * 900); // DebugPlayer100 - DebugPlayer999
		} else {
			username = "Player" + (int)Math.floor(100 + Math.random() * 900); // Player100 - Player999
		}
	}

	public String getUsername() {
		return username;
	}

	public void disconnectClient() throws IOException {
		if(client == null) return;
		try {
			client.disconnect();
		} finally {
			client = null;
		}
	}
	
	public Client createClient(String host, int port) throws IOException {
		disconnectClient();
		return client = new Client(host, port);
	}
	
	public Client getClient() {
		return client;
	}
	
	public LocalClient createLocalClient() throws IOException {
		Objects.requireNonNull(server, "Server is null");
		disconnectClient();
		return server.getLocalClient();
	}
	
	public LocalClient createConnectedLocalClient() throws IOException {
		LocalClient lc = createLocalClient();
		lc.connect(getUsername());
		return lc;
	}
	
	public Server getServer() {
		return server;
	}
	
	public void shutdownServer() {
		if(server == null) return;
		try {
			server.shutdown();
		} finally {
			server = null;
		}
	}

	public Server createServer(World world, int port) {
		shutdownServer();
		return server = new Server(world, port);
	}

	@Override
	public void create() {
		GAME_LIFECYCLE_EVENT_BUS.subscribeEventListener(this);

		// Load content
		GAME_LIFECYCLE_EVENT_BUS.dispatchEvent(new ContentInitializeEvent());
		
		// All content is now initialized
		GAME_LIFECYCLE_EVENT_BUS.dispatchEvent(new PostContentInitializeEvent());
		
		setScreen(Screens.TITLE_SCREEN);
	}

	@Override
	public void render() {
		super.render();
	}
	
	@Override
	public void dispose() {
		GAME_LIFECYCLE_EVENT_BUS.dispatchEvent(new GameDisposedEvent());
		
		getScreen().dispose();
		

		Server server = getServer();
		if(server != null) {
			server.dispose();
		}
		
		System.exit(0);
	}
	
	@SubscribeEvent
	public void onInitContent(EventBus bus, ContentInitializeEvent event) {
		bus.subscribeEventListener(ThreadPools.class);

		Packets.register();
		VoxelShaders.loadShaders();
		GameTextures.register();
		Voxels.register();
		RenderLayers.register();
		Biomes.register();
	}
	
	@SubscribeEvent
	public void onPostInitContent(EventBus bus, PostContentInitializeEvent event) {
		VoxelTextures.ATLAS_TERRAIN.getManager().generateAtlas();
		
		Screens.register();
	}
}