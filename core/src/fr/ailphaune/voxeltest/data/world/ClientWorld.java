package fr.ailphaune.voxeltest.data.world;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.light.Lighting;

public class ClientWorld extends World {

	public ClientWorld(Lighting lighting) {
		super(0, lighting, false);
	}
	
	@Override
	public synchronized void generateChunk(Chunk chunk) {}
	
	@Override
	public long getChunkSeed(ChunkPos pos) {
		return 0;
	}
	
	@Override
	public long getCompoundSeed(long compound) {
		return 0;
	}
	
	@Override
	public boolean isClient() {
		return true;
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
	public void clear() {
		relightQueue.clear();
		loaded_chunks.clear();
		if(saver != null) {
			saver.clear();
		}
	}
}