package fr.ailphaune.voxeltest.render.voxel;
import java.util.Arrays;
import java.util.concurrent.Future;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool.Poolable;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.Registries;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.render.mesh.chunk.MeshData;
import fr.ailphaune.voxeltest.render.mesh.chunk.RenderLayer;
import fr.ailphaune.voxeltest.render.mesh.chunk.MeshData.MeshDataPool;
import fr.ailphaune.voxeltest.threading.ThreadPools;

public class VoxelChunk implements Poolable, Disposable {
	public final int size;
	
	public Chunk chunkToRender;
	
	public final Vector3 offset = new Vector3();
	public ChunkPos relativePosToCenterChunk = new ChunkPos(0, 0, 0);
	
	private Future<?> meshingTask;
	
	private MeshDataPool meshDataPool;
	
	@SuppressWarnings("unchecked")
	public VoxelChunk(int size, MeshDataPool meshDataPool) {
		this.size = size;
		final int layers = Registries.RENDER_LAYERS.size();
		this.meshes = new Array[layers];
		this.meshDatas = new Array[layers];
		this.dirtyMesh = new boolean[layers];
		this.availableMeshData = new boolean[layers];
		this.numVertices = new int[layers];
		Arrays.fill(this.dirtyMesh, true);
		this.meshDataPool = meshDataPool;
		for(int i = 0; i < layers; i++) {
			meshes[i] = new Array<Mesh>();
		}
		for(int i = 0; i < layers; i++) {
			meshDatas[i] = new Array<MeshData>();
		}
	}

	public byte get(int x, int y, int z) {
		return chunkToRender.get(x, y, z);
	}

	public byte getFast(int x, int y, int z) {
		return chunkToRender.getFast(x, y, z);
	}

	public void set(int x, int y, int z, byte voxel) {
		chunkToRender.set(x, y, z, voxel);
	}

	public void setFast(int x, int y, int z, byte voxel) {
		chunkToRender.setFast(x, y, z, voxel);
	}

	public Array<Mesh> meshes[];
	public Array<MeshData> meshDatas[];
	public boolean dirty = true;
	public boolean dirtyMesh[], availableMeshData[];
	public int[] numVertices;

	protected synchronized void renderDirtyNow() {
		for(int i = 0; i < meshes.length; i++) {
			if(dirty || dirtyMesh[i]) {
				availableMeshData[i] = false;
				RenderLayer renderLayer = Registries.RENDER_LAYERS.get(i);
				meshDataPool.freeAll(meshDatas[i]);
				meshDatas[i].clear();
				numVertices[i] = renderLayer.renderToMeshData(meshDatas[i], meshDataPool, this);
				availableMeshData[i] = true;
			}
			dirtyMesh[i] = false;
		}
		dirty = false;
		chunkToRender.needsRemeshing = false;
		this.meshingTask = null;
	}
	
	public void renderDirty() {
		if(this.meshingTask != null && !this.meshingTask.isDone()) {
			return;
		}
		for(int i = 0; i < meshDatas.length; i++) {
			if(!availableMeshData[i]) continue;
			availableMeshData[i] = false;
			
			RenderLayer renderLayer = Registries.RENDER_LAYERS.get(i);
			
			Array<Mesh> meshes = new Array<Mesh>();
			
			// convert to meshes
			MeshData.convertToMeshes(meshDatas[i], meshes, renderLayer.getMeshPool());
			
			// free previous meshes
			renderLayer.getMeshPool().freeAll(this.meshes[i]);
			this.meshes[i].clear();
			
			this.meshes[i] = meshes;
			
			// free mesh datas
			meshDataPool.freeAll(meshDatas[i]);
			meshDatas[i].clear();
		}
		this.meshingTask = ThreadPools.CLIENT_RENDERING.submit(this::renderDirtyNow);
	}

	@Override
	public void reset() {
		chunkToRender = null;
		offset.set(0, 0, 0);
		relativePosToCenterChunk.set(0, 0, 0);
		dirty = true;
		Arrays.fill(dirtyMesh, true);
		Arrays.fill(numVertices, 0);
	}

	@Override
	public void dispose() {
		reset();
		for(int i = 0; i < meshes.length; i++) {
			Registries.RENDER_LAYERS.get(i).getMeshPool().freeAll(meshes[i]);
		}
	}
}