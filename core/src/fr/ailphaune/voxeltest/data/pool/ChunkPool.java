package fr.ailphaune.voxeltest.data.pool;

import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.render.mesh.chunk.MeshData.MeshDataPool;
import fr.ailphaune.voxeltest.render.voxel.VoxelChunk;

public class ChunkPool extends SynchronizedPool<VoxelChunk> {

	public final MeshDataPool meshDataPool;
	
	public ChunkPool(MeshDataPool meshDataPool) {
		super();
		this.meshDataPool = meshDataPool;
	}
	
	public ChunkPool(MeshDataPool meshDataPool, int initialCapacity) {
		super(initialCapacity);
		this.meshDataPool = meshDataPool;
	}
	
	public ChunkPool(MeshDataPool meshDataPool, int initialCapacity, int max) {
		super(initialCapacity, max);
		this.meshDataPool = meshDataPool;
	}
	
	@Override
	protected VoxelChunk newObject() {
		return new VoxelChunk(Chunk.SIZE, meshDataPool);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		meshDataPool.dispose();
	}
}