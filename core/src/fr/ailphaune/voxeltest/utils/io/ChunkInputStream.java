package fr.ailphaune.voxeltest.utils.io;

import java.io.IOException;
import java.io.InputStream;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.utils.ArrayUtils;

public class ChunkInputStream extends ExtendedDataInputStream {

	private final ChunkPos useless = new ChunkPos();
	
	public ChunkInputStream(InputStream in) {
		super(in);
	}
	
	public Chunk readChunk(World world) throws IOException {
		if(readByte() != 1) return null;
		ChunkPos pos = new ChunkPos();
		readChunkPos(pos);
		Chunk chunk = world.createEmptyChunk(pos);
		readBytes(chunk.voxels);
		readShorts(chunk.states);
		// Biomes just break everything i'm so confused
		/*for(int i = 0; i < chunk.biomes.length; i++) {
			chunk.biomes[i] = Registries.BIOMES.get(readInt());
		}*/
		return chunk;
	}
	
	public Chunk readChunk(Chunk out) throws IOException {
		if(readByte() != 1) return null;
		readChunkPos(useless);
		readBytes(out.voxels);
		if(ArrayUtils.contains(out.voxels, (byte) -1)) {
			System.out.println("WHAT ?");
		}
		readShorts(out.states);
		// same here
		/*for(int i = 0; i < out.biomes.length; i++) {
			out.biomes[i] = Registries.BIOMES.get(readInt());
		}*/
		return out;
	}
}