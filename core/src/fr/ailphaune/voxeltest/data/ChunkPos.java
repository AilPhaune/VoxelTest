package fr.ailphaune.voxeltest.data;

import java.util.Objects;

import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.data.world.Chunk;

public class ChunkPos {

	public int x, y, z;

	public ChunkPos() {
		set(0, 0, 0);
	}
	
	public ChunkPos(ChunkPos source) {
		set(source);
	}
	
	public ChunkPos(int x, int y, int z) {
		set(x, y, z);
	}

	public ChunkPos set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public ChunkPos set(ChunkPos pos) {
		return set(pos.x, pos.y, pos.z);
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
	    ChunkPos other = (ChunkPos) obj;
	    return x == other.x && y == other.y && z == other.z;
	}
	
	@Override
	public String toString() {
		return "ChunkPos{x=" + x + ",y=" + y + ",z=" + z + "}";
	}

	public Vector3 asVec3() {
		return asVec3(new Vector3());
	}

	public Vector3 asVec3(Vector3 vec) {
		return vec.set(x, y, z);
	}
	
	/**
	 * Calculates the coordinates of the given {@link VoxelPos} relative to this {@link ChunkPos}.
	 * @param position The position of the voxel
	 * @return the coordinates of the voxel relative to this chunk
	 */
	public VoxelPos getRelativeVoxelPos(VoxelPos position) {
		return getRelativeVoxelPos(position, new VoxelPos(0,0,0));
	}

	/**
	 * Calculates the coordinates of the given {@link VoxelPos} relative to this {@link ChunkPos} and puts the result in the output parameter.
	 * @param position The position of the voxel
	 * @param output The output
	 * @return output
	 */
	public VoxelPos getRelativeVoxelPos(VoxelPos position, VoxelPos output) {
		int x = position.x - (this.x * Chunk.SIZE);
		int y = position.y - (this.y * Chunk.SIZE);
		int z = position.z - (this.z * Chunk.SIZE);
		
		return output.set(x, y, z);
	}
	
	/**
	 * Calculates the world {@link VoxelPos} coordinates from the x, y, z coordinates relative to this chunk
	 * @param x The relative x coordinate to this chunk
	 * @param y The relative y coordinate to this chunk
	 * @param z The relative z coordinate to this chunk
	 * @return The world coordinates of the given relative coordinates
	 */
	public VoxelPos getVoxelPos(int x, int y, int z) {
		return new VoxelPos(x * Chunk.SIZE + x, y * Chunk.SIZE + y, z * Chunk.SIZE + z);
	}
	
	/**
	 * Calculates the world {@link VoxelPos} coordinates from the x, y, z coordinates relative to this chunk and writes the result to the out parameter
	 * @param x The relative x coordinate to this chunk
	 * @param y The relative y coordinate to this chunk
	 * @param z The relative z coordinate to this chunk
	 * @param out The result of the calculation will be written here
	 * @return The world coordinates of the given relative coordinates
	 */
	public VoxelPos getVoxelPos(int x, int y, int z, VoxelPos out) {
		return out.set(x * Chunk.SIZE + x, y * Chunk.SIZE + y, z * Chunk.SIZE + z);
	}

	public ChunkPos add(int dx, int dy, int dz) {
		return set(x + dx, y + dy, z + dz);
	}
}