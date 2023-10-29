package fr.ailphaune.voxeltest.screens;

import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import fr.ailphaune.voxeltest.VoxelTestGame;
import fr.ailphaune.voxeltest.controller.FPCameraController;
import fr.ailphaune.voxeltest.controller.PerspectiveCamera;
import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.Registries;
import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.VoxelTarget;
import fr.ailphaune.voxeltest.data.actions.UseActionResult;
import fr.ailphaune.voxeltest.data.pool.ChunkPool;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.ClientWorld;
import fr.ailphaune.voxeltest.entities.PlayerEntity;
import fr.ailphaune.voxeltest.light.Lighting;
import fr.ailphaune.voxeltest.multiplayer.Client;
import fr.ailphaune.voxeltest.render.mesh.chunk.MeshData.MeshDataPool;
import fr.ailphaune.voxeltest.render.outlines.BoxOutline;
import fr.ailphaune.voxeltest.render.outlines.VoxelOutline;
import fr.ailphaune.voxeltest.render.voxel.VoxelTextures;
import fr.ailphaune.voxeltest.render.voxel.VoxelWorld;
import fr.ailphaune.voxeltest.voxels.AbstractVoxel;
import fr.ailphaune.voxeltest.voxels.Voxels;

public class GameScreen extends ScreenAdapter {
	
	// public World world;
	public Client client;
	
	public SpriteBatch spriteBatch;
	public BitmapFont font;
	public ModelBatch modelBatch;
	public PerspectiveCamera camera;
	public FPCameraController controller;
	public VoxelWorld voxelWorld;

	public VoxelOutline outline;
	public BoxOutline chunkOutline;
	
	public Lighting lighting;
	
	public Texture texture, crosshair;
	
	public GameScreen() {
		AssetManager manager = new AssetManager();
		manager.load("voxeltest/textures/gui/crosshair.png", Pixmap.class);
		manager.finishLoading();
		
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		
		DefaultShader.Config config = new DefaultShader.Config();
		modelBatch = new ModelBatch(new DefaultShaderProvider(config));
		
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 0.1f;
		camera.far = 1000;

		PixmapIO.writePNG(Gdx.files.local("atlas.png"), VoxelTextures.ATLAS_TERRAIN.getPixmap());
		texture = VoxelTextures.ATLAS_TERRAIN.getTexture();
		crosshair = new Texture(manager.get("voxeltest/textures/gui/crosshair.png", Pixmap.class));
		
		chunkOutline = new BoxOutline(new Vector3(0,0,0), new Vector3(1, 1, 1).scl(Chunk.fSIZE), Chunk.SIZE);
		outline = new VoxelOutline();
		
		controller = new FPCameraController(camera);
		controller.setVelocity(10);
	}
	
	@Override
	public void show() {
		super.show();
		
		if(voxelWorld == null) {
			voxelWorld = new VoxelWorld(client.getWorld(), 4, 2, new ChunkPool(new MeshDataPool(10, 50), 16, 100));
		}
		
		Gdx.input.setInputProcessor(controller);
	}
	
	public VoxelPos cameraVoxelPos = new VoxelPos(0,0,0);
	public VoxelPos tempVoxelPos = new VoxelPos(0,0,0);
	public ChunkPos tempChunkPos = new ChunkPos(0,0,0);
	public ChunkPos cameraChunkPos = new ChunkPos(0,0,0);
	public Vector3 tempVec3 = new Vector3();
	public Vector3 playerPos = new Vector3();
	
	public byte selectedVoxel = 1;
	
	public float time = 0;
	
	public float lastUnload = 0;
	
	public boolean pause = false;
	public boolean showChunkBorders = false;
	public boolean fullbright = false;
	public boolean playerEmitsLight = true;
	
	@Override
	public void render(float deltaTime) {
		ScreenUtils.clear(0.4f, 0.4f, 0.4f, 1f, true);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		if(!client.isConnected()) {
			VoxelTestGame.getInstance().setScreen(Screens.TITLE_SCREEN);
		}
		
		ClientWorld clientWorld = client.getWorld();
		if(clientWorld == null) {
			return;
		}
		if(voxelWorld.getWorldToRender() != clientWorld) {
			voxelWorld.setWorldToRender(clientWorld);
		}
		clientWorld.calculateLightQueue();
		
		PlayerEntity player = client.getPlayerEntity();
		if(player == null) {
			// No player
			return;
		}
		
		Gdx.input.setCursorCatched(!pause);
		
		cameraVoxelPos.set((int) camera.position.x, (int) camera.position.y, (int) camera.position.z);
		// get camera chunk
		cameraVoxelPos.getChunkPos(cameraChunkPos);
		
		time += deltaTime;
		if(time - lastUnload >= 1 || Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
			lastUnload = time;
			voxelWorld.updateVisibleChunkList();
		}
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			selectedVoxel++;
			selectedVoxel %= Registries.VOXELS.size();
		}
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.F)) {
			fullbright = !fullbright;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.G)) {
			playerEmitsLight = !playerEmitsLight;
		}
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			pause = !pause;
		}

		controller.player = player;
		controller.speedMultiplier = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) ? 10 : 1;
		controller.pauseController = pause;
		controller.update();
		
		player.getCameraPosition(camera.position);
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
			List<Chunk> chunks = clientWorld.getLoadedChunks().stream().map(entry -> entry.getValue()).collect(Collectors.toList());
			for(Chunk chunk : chunks) {
				chunk.clearLights();
			}
			for(Chunk chunk : chunks) {
				clientWorld.updateLightSync(chunk);
				chunk.needsRemeshing = true;
			}
		}
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
			showChunkBorders = !showChunkBorders;
		}
		
		if(!Gdx.input.isKeyPressed(Input.Keys.F7)) {
			voxelWorld.render(camera);
			float ambient = fullbright ? 1.0f : 0.1f;
			voxelWorld.ambientColor.set(ambient, ambient, ambient, 1);
			float camera = playerEmitsLight ? 1.0f : 0.0f;
			voxelWorld.cameraEmitedColor.set(camera, camera, camera, 1);
		} else {
			voxelWorld.renderWireframe(camera);
		}

		if(showChunkBorders) {
			chunkOutline.render(camera, Color.YELLOW, 5, cameraChunkPos.asVec3(tempVec3).scl(Chunk.SIZE), true);
		}
		
		// update center chunk
		if(!voxelWorld.centerChunk.equals(cameraChunkPos)) {
			voxelWorld.centerChunk.set(cameraChunkPos);
			voxelWorld.updateVisibleChunkList();
		}
		
		VoxelTarget target = clientWorld.getTargetedVoxel(camera, 10);
		
		if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && target != null && !pause) {
			player.destroyVoxel(target.pos.x, target.pos.y, target.pos.z);
			clientWorld.setVoxel(target.pos.x, target.pos.y, target.pos.z, Voxels.AIR);
			target = clientWorld.getTargetedVoxel(camera, 10);
		}
		
		if(Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && target != null && !pause) {
			AbstractVoxel voxel = Voxels.getAbstractVoxel(clientWorld.getVoxel(target.pos));
			UseActionResult result = voxel.onPlayerUse(clientWorld, target, tempVoxelPos);
			if(result == UseActionResult.VOXEL_PLACEMENT && clientWorld.getVoxel(tempVoxelPos) == Voxels.AIR) {
				player.placeVoxel(tempVoxelPos, selectedVoxel);
				clientWorld.setVoxel(tempVoxelPos, selectedVoxel);
			}
		}

		if(target != null) {
			outline.set(clientWorld, target.pos);
			outline.render(camera, Color.BLACK, 3, target.pos.asVec3(), false);
		}

		spriteBatch.begin();
		font.draw(spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() + ", visible chunks: " + voxelWorld.renderedChunks + "/"
			+ voxelWorld.numChunks + ", world loaded chunks: " + clientWorld.getLoadedChunkCount(), 0, 20);

		player.getVoxelPos(tempVoxelPos);
		tempVoxelPos.getChunkPos(tempChunkPos);
		tempChunkPos.getRelativeVoxelPos(tempVoxelPos);
		player.getPosition(playerPos);
		
		font.draw(spriteBatch, "X/Y/Z  " + playerPos.x + " / " + playerPos.y + " / " + playerPos.z + "   |   Yaw/Pitch/Roll  " + controller.yaw + " / " + controller.pitch + " / " + controller.roll, 0, 80);
		font.draw(spriteBatch, "Chunk Position X/Y/Z  " + tempChunkPos.x + " / " + tempChunkPos.y + " / " + tempChunkPos.z, 0, 60);
		font.draw(spriteBatch, "Position in Chunk X/Y/Z  " + tempVoxelPos.x + " / " + tempVoxelPos.y + " / " + tempVoxelPos.z, 0, 40);
		
		float w = font.draw(spriteBatch, "You are currently building with: ", 0, 120).width;
		font.draw(spriteBatch, Voxels.getAbstractVoxel(selectedVoxel).getName(), w, 120);
		
		if(target != null) {
			ChunkPos pos = target.pos.getChunkPos();
			VoxelPos relChunk = pos.getRelativeVoxelPos(target.pos);
			font.draw(spriteBatch, "Looking At X/Y/Z  " + target.pos.x + " / " + target.pos.y + " / " + target.pos.z + " | Chunk X/Y/Z: " + relChunk.x + " / " + relChunk.y + " / " + relChunk.z, 0, 100);
		}

		int crossHairX = (width - crosshair.getWidth()) / 2;
		int crossHairY = (height - crosshair.getHeight()) / 2;
		
		spriteBatch.draw(crosshair, crossHairX, crossHairY);
		
		spriteBatch.end();
	}
	
	private int width, height;
	
	@Override
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		spriteBatch.dispose();
		font.dispose();
		chunkOutline.dispose();
		outline.dispose();
	}
}
