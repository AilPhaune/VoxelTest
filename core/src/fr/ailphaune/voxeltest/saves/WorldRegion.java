package fr.ailphaune.voxeltest.saves;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.utils.io.ChunkInputStream;
import fr.ailphaune.voxeltest.utils.io.ChunkOutputStream;

public class WorldRegion {

	public static final int SIZE = 1;
	public static final int SIZE2 = SIZE * SIZE;
	public static final int SIZE3 = SIZE2 * SIZE;

	private final ChunkPos tempChunkPos = new ChunkPos();

	public boolean loaded = false;

	protected RegionPos pos;
	protected World world;
	protected Chunk[] chunks;

	public WorldRegion(RegionPos pos, World world) {
		this.pos = new RegionPos(pos);
		this.chunks = new Chunk[SIZE3];
		this.world = world;
	}

	public synchronized Chunk getChunk(int rx, int ry, int rz) {
		return chunks[rx + rz * SIZE + ry * SIZE2];
	}

	public synchronized Chunk getChunk(ChunkPos chunkPos) {
		ChunkPos relPos = pos.getRelativeChunk(chunkPos, tempChunkPos);
		if (relPos.x < 0 || relPos.x >= SIZE || relPos.y < 0 || relPos.y >= SIZE || relPos.z < 0 || relPos.z >= SIZE) {
			throw new IndexOutOfBoundsException("Chunk is not in this region");
		}
		return getChunk(relPos.x, relPos.y, relPos.z);
	}

	protected synchronized void put(int rx, int ry, int rz, Chunk chunk) {
		chunks[rx + rz * SIZE + ry * SIZE2] = chunk;
	}

	public synchronized void putChunk(Chunk chunk) {
		ChunkPos relPos = pos.getRelativeChunk(chunk.getChunkPos(), tempChunkPos);
		if (relPos.x < 0 || relPos.x >= SIZE || relPos.y < 0 || relPos.y >= SIZE || relPos.z < 0 || relPos.z >= SIZE) {
			throw new IndexOutOfBoundsException("Chunk is not in this region");
		}
		put(relPos.x, relPos.y, relPos.z, chunk);
	}

	public synchronized void writeTo(OutputStream os) throws IOException {
		ChunkOutputStream cos = new ChunkOutputStream(os);
		cos.writeRegionPos(pos);
		for (Chunk chunk : chunks) {
			cos.writeChunk(chunk);
		}
		cos.close();
	}

	public synchronized void readFrom(InputStream is) throws IOException {
		ChunkInputStream cis = new ChunkInputStream(is);
		cis.readRegionPos(pos);
		for (int i = 0; i < chunks.length; i++) {
			chunks[i] = chunks[i] == null ? cis.readChunk(world) : cis.readChunk(chunks[i]);
		}
		loaded = true;
		cis.close();
	}

	public synchronized Chunk get(Chunk chunk) {
		Chunk mChunk = getChunk(chunk.getChunkPos());
		if (mChunk == null) {
			putChunk(chunk);
			return null;
		}
		return mChunk;
	}
}