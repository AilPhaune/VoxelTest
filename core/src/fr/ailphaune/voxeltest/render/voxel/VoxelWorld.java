package fr.ailphaune.voxeltest.render.voxel;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.Registries;
import fr.ailphaune.voxeltest.data.pool.ChunkPool;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.ClientWorld;

public class VoxelWorld {
	public final List<VoxelChunk> chunks;
	public float[] vertices;
	public final int chunksX;
	public final int chunksY;
	public final int chunksZ;
	public final int voxelsX;
	public final int voxelsY;
	public final int voxelsZ;
	public int renderedChunks;
	public int numChunks;
	
	public int renderDistanceXZ;
	public int renderDistanceY;
	
	private static final Vector3 chunkCenter = new Vector3();
	private static final Vector3 chunkDimensions = new Vector3(Chunk.SIZE, Chunk.SIZE, Chunk.SIZE);
	private static final int chunkHalfDimension = Chunk.SIZE / 2;
	
	public ChunkPos centerChunk;
	protected ClientWorld worldToRender;
	
	public final ChunkPool chunkPool;
	
	public VoxelWorld(ClientWorld worldToRender, int renderDistanceXZ, int renderDistanceY, ChunkPool chunkPool) {
		this.chunksX = renderDistanceXZ * 2 + 1;
		this.chunksY = renderDistanceY * 2 + 1;
		this.chunksZ = renderDistanceXZ * 2 + 1;
		this.chunks = new ArrayList<VoxelChunk>();
		this.numChunks = chunksX * chunksY * chunksZ;
		this.voxelsX = chunksX * Chunk.SIZE;
		this.voxelsY = chunksY * Chunk.SIZE;
		this.voxelsZ = chunksZ * Chunk.SIZE;
		this.renderDistanceXZ = renderDistanceXZ;
		this.renderDistanceY = renderDistanceY;
		this.chunkPool = chunkPool;
		
		centerChunk = new ChunkPos(0, 0, 0);
		
		setWorldToRender(worldToRender);
	}
	
	public void setWorldToRender(ClientWorld clientWorld) {
		if(clientWorld != null && clientWorld.isServer()) throw new IllegalArgumentException("Can only render a client world !");

		for(VoxelChunk chunk : chunks) {
			chunkPool.free(chunk);
		}
		chunks.clear();
		
		this.worldToRender = clientWorld;
		
		updateVisibleChunkList();
	}

	public ClientWorld getWorldToRender() {
		return worldToRender;
	}
	
	public boolean rendersChunkBasedOnSettings(ChunkPos pos) {
		return Math.abs(pos.x - centerChunk.x) <= renderDistanceXZ && Math.abs(pos.y - centerChunk.y) <= renderDistanceY && Math.abs(pos.z - centerChunk.z) <= renderDistanceXZ;
	}
	
	public void updateVisibleChunkList() {
		if(worldToRender == null) {
			for(int i = chunks.size() - 1; i >= 0; i--) {
				VoxelChunk chunk = chunks.get(i);
				chunks.remove(i);
				chunkPool.free(chunk);
			}
			return;
		}
		for(int i = chunks.size() - 1; i >= 0; i--) {
			VoxelChunk chunk = chunks.get(i);
			ChunkPos pos = chunk.chunkToRender.getChunkPos();
			chunk.relativePosToCenterChunk.set(pos.x - centerChunk.x, pos.y - centerChunk.y, pos.z - centerChunk.z);
			if(!rendersChunkBasedOnSettings(pos)) {
				chunks.remove(i);
				chunkPool.free(chunk);
			}
		}
		
		for (int y = -renderDistanceY; y <= renderDistanceY; y++) {
			for (int z = -renderDistanceXZ; z <= renderDistanceXZ; z++) {
				for (int x = -renderDistanceXZ; x <= renderDistanceXZ; x++) {
					ChunkPos pos = new ChunkPos(centerChunk.x + x, centerChunk.y + y, centerChunk.z + z);
					if(chunks.stream().anyMatch(chunk -> chunk.chunkToRender.getChunkPos().equals(pos))) {
						continue;
					}
					VoxelChunk chunk = chunkPool.obtain();
					chunk.relativePosToCenterChunk.set(x, y, z);
					chunk.chunkToRender = worldToRender.getChunk(x + centerChunk.x, y + centerChunk.y, z + centerChunk.z);
					chunk.offset.set(chunk.chunkToRender.chunkX * Chunk.SIZE, chunk.chunkToRender.chunkY * Chunk.SIZE, chunk.chunkToRender.chunkZ * Chunk.SIZE);
					chunks.add(chunk);
				}
			}
		}
	}
	
	public boolean isChunkVisible(VoxelChunk chunk, Camera camera)	{
		chunkCenter.set(chunk.offset.x + chunkHalfDimension, chunk.offset.y + chunkHalfDimension, chunk.offset.z + chunkHalfDimension);
		return camera.frustum.boundsInFrustum(chunkCenter, chunkDimensions);
	}
	
	public float shortestDistanceToCamera(VoxelChunk chunk, Camera camera) {
		chunkCenter.set(chunk.offset.x + chunkHalfDimension, chunk.offset.y + chunkHalfDimension, chunk.offset.z + chunkHalfDimension);
		return camera.position.dst2(chunkCenter);
	}
	
	private Vector3 tempVec3 = new Vector3();

	public final Color ambientColor = new Color(), cameraEmitedColor = new Color();
	
	public void render(PerspectiveCamera camera, int primitiveType) {
		renderedChunks = 0;
		
		VoxelShaders.bindTerrain(camera.combined);
		VoxelShaders.terrainShader.setUniformi("u_texture", 0);
		VoxelShaders.terrainShader.setUniformf("u_ambientColor", ambientColor);
		
		VoxelShaders.terrainShader.setUniformf("u_dynamicLightPos[0]", camera.position);
		VoxelShaders.terrainShader.setUniformf("u_dynamicLightColor[0]", cameraEmitedColor);
		VoxelShaders.terrainShader.setUniformi("u_dynamicLightCount", 1);
		
		// VoxelShaders.terrainShader.setUniformf("u_cameraPosition", camera.position);
		
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glFrontFace(GL20.GL_CCW);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		
		for(int i = 0; i < chunks.size(); i++) {
			VoxelChunk chunk = chunks.get(i);
			Chunk chunkToRender = worldToRender.getChunk(chunk.relativePosToCenterChunk.x + centerChunk.x, chunk.relativePosToCenterChunk.y + centerChunk.y, chunk.relativePosToCenterChunk.z + centerChunk.z);
			if(chunkToRender != chunk.chunkToRender) {
				chunk.dirty = true;
			}
			chunk.chunkToRender = chunkToRender;
			chunk.dirty |= chunkToRender.needsRemeshing;
			
			if(isChunkVisible(chunk, camera)) {
				if(chunk.chunkToRender.needsRelighting) {
					worldToRender.updateLightSync(chunk.chunkToRender);
				}
				chunk.renderDirty();

				VoxelShaders.terrainShader.setUniformf("u_chunkOffset", chunk.chunkToRender.getChunkPos().asVec3(tempVec3).scl(Chunk.fSIZE));
				
				boolean renderedChunk = false;
				for(int j = 0; j < chunk.meshes.length; j++) {
					if(chunk.numVertices[j] == 0) continue;
					
					Texture texture = Registries.RENDER_LAYERS.get(j).getTextureAtlas().getTexture();
					texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
					texture.bind();
					
					for(Mesh mesh : chunk.meshes[j]) {
						mesh.render(VoxelShaders.terrainShader, primitiveType);
					}
					
					renderedChunk = true;
				}
				if(renderedChunk) renderedChunks++;
			}
		}
		
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	}

	public void render(PerspectiveCamera camera) {
		render(camera, GL20.GL_TRIANGLES);
	}

	public void renderWireframe(PerspectiveCamera camera) {
		render(camera, GL20.GL_LINES);
	}
}