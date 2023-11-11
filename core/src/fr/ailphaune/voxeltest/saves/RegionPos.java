package fr.ailphaune.voxeltest.saves;

import java.util.Objects;

import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.data.ChunkPos;

public class RegionPos {

	public int x, y, z;

	public RegionPos() {
		set(0, 0, 0);
	}
	
	public RegionPos(RegionPos source) {
		set(source);
	}
	
	public RegionPos(ChunkPos chunkPos) {
		set(chunkPos);
	}
	
	public RegionPos(int x, int y, int z) {
		set(x, y, z);
	}

	public RegionPos set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public RegionPos set(RegionPos pos) {
		return set(pos.x, pos.y, pos.z);
	}

	public void set(ChunkPos chunkPos) {
		x = chunkPos.x / WorldRegion.SIZE;
		y = chunkPos.y / WorldRegion.SIZE;
		z = chunkPos.z / WorldRegion.SIZE;
		if(chunkPos.x < 0 && (chunkPos.x % WorldRegion.SIZE) != 0) x--;
		if(chunkPos.y < 0 && (chunkPos.y % WorldRegion.SIZE) != 0) y--;
		if(chunkPos.z < 0 && (chunkPos.z % WorldRegion.SIZE) != 0) z--;
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
	    RegionPos other = (RegionPos) obj;
	    return x == other.x && y == other.y && z == other.z;
	}
	
	@Override
	public String toString() {
		return "RegionPos{x=" + x + ",y=" + y + ",z=" + z + "}";
	}

	public Vector3 asVec3() {
		return asVec3(new Vector3());
	}

	public Vector3 asVec3(Vector3 vec) {
		return vec.set(x, y, z);
	}
	
	public ChunkPos getRelativeChunk(ChunkPos position, ChunkPos output) {
		int x = position.x - (this.x * WorldRegion.SIZE);
		int y = position.y - (this.y * WorldRegion.SIZE);
		int z = position.z - (this.z * WorldRegion.SIZE);
		
		return output.set(x, y, z);
	}
}