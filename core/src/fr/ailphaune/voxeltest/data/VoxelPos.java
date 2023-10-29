package fr.ailphaune.voxeltest.data;

import java.util.Objects;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

import fr.ailphaune.voxeltest.data.world.Chunk;

public class VoxelPos implements Poolable {

	public static final VoxelPos TEMP = new VoxelPos();
	
	public int x, y, z;
	
	public VoxelPos() {
		set(0, 0, 0);
	}
	
	public VoxelPos(VoxelPos source) {
		set(source);
	}
	
	public VoxelPos(int x, int y, int z) {
		set(x, y, z);
	}

	public VoxelPos set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public VoxelPos set(VoxelPos pos) {
		return set(pos.x, pos.y, pos.z);
	}

	public VoxelPos set(Vector3 vec) {
		return set((int) vec.x, (int) vec.y, (int) vec.z);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
	        return true;
	    if (obj == null || getClass() != obj.getClass())
	        return false;
	    VoxelPos other = (VoxelPos) obj;
	    return x == other.x && y == other.y && z == other.z;
	}
	
	@Override
	public String toString() {
		return "VoxelPos{x=" + x + ",y=" + y + ",z=" + z + "}";
	}

	public Vector3 asVec3() {
		return asVec3(new Vector3());
	}

	public Vector3 asVec3(Vector3 vec) {
		return vec.set(x, y, z);
	}

	/**
	 * Calculates the chunk coordinates of the chunk containing this {@link VoxelPos}
	 * @return the position of the chunk containing this {@link VoxelPos}
	 */
	public ChunkPos getChunkPos() {
		return getChunkPos(new ChunkPos(0,0,0));
	}

	/**
	 * Calculates the chunk coordinates of the chunk containing this {@link VoxelPos} and puts the result in the output parameter
	 * @param output The output of the calculation
	 * @return output
	 */
	public ChunkPos getChunkPos(ChunkPos output) {
		int chunkX = x / Chunk.SIZE;
		int chunkY = y / Chunk.SIZE;
		int chunkZ = z / Chunk.SIZE;
		if(x < 0 && (x % Chunk.SIZE) != 0) chunkX--;
		if(y < 0 && (y % Chunk.SIZE) != 0) chunkY--;
		if(z < 0 && (z % Chunk.SIZE) != 0) chunkZ--;
		output.x = chunkX;
		output.y = chunkY;
		output.z = chunkZ;
		return output;
	}

	@Override
	public void reset() {
		set(0, 0, 0);
	}
}