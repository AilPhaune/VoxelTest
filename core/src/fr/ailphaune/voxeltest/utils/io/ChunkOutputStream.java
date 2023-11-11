package fr.ailphaune.voxeltest.utils.io;

import java.io.IOException;
import java.io.OutputStream;

import fr.ailphaune.voxeltest.data.world.Chunk;

public class ChunkOutputStream extends ExtendedDataOutputStream {

	public ChunkOutputStream(OutputStream out) {
		super(out);
	}

	public void writeChunk(Chunk chunk) throws IOException {
		if(chunk == null) {
			writeByte(0);
			return;
		}
		writeByte(1);
		writeChunkPos(chunk.getChunkPos());
		writeBytes(chunk.voxels);
		writeShorts(chunk.states);
		// somehow adding in the biome data breaks the whole thing ?
		/*for(int i = 0; i < chunk.biomes.length; i++) {
			Biome biome = chunk.biomes[i];
			writeInt(biome == null ? -1 : biome.getIndex());
		}*/
	}
}