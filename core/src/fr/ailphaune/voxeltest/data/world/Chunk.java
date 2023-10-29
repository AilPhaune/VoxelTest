package fr.ailphaune.voxeltest.data.world;

import java.util.Arrays;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.Registries;
import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.worldgen.biome.Biome;

public class Chunk {

	public static final int SIZE = 16;
	public static final int SIZE2 = SIZE*SIZE;
	public static final int SIZE3 = SIZE*SIZE*SIZE;
	
	public static final float fSIZE = (float) SIZE;
	
	public byte voxels[];
	
	public short light[];
	public short states[];
	
	public Biome biomes[];

	public final int chunkX;
	public final int chunkY;
	public final int chunkZ;
	
	private World world;
	public boolean needsRemeshing = false;
	public boolean needsRelighting = false;
	
	private ChunkPos chunkPos;
	
	public final long chunkSeed;
	
	public Chunk(int chunkX, int chunkY, int chunkZ, World world) {
		voxels = new byte[SIZE3];
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.chunkPos = new ChunkPos(chunkX, chunkY, chunkZ);
		
		this.chunkSeed = world.getChunkSeed(getChunkPos());
		
		this.world = world;
		this.biomes = new Biome[SIZE2];
		this.light = new short[SIZE3];
		this.states = new short[SIZE3];
	}
	
	public World getWorld() {
		return world;
	}

	public Biome getBiome(int x, int z) {
		return biomes[x * SIZE + z];
	}
	
	public Biome setBiome(int x, int z, Biome biome) {
		return biomes[x * SIZE + z] = biome;
	}
	
	public byte get(int x, int y, int z) {
		return getFast(x, y, z);
	}

	public byte getFast(int x, int y, int z) {
		if (x < 0 || x >= SIZE) return 0;
		if (y < 0 || y >= SIZE) return 0;
		if (z < 0 || z >= SIZE) return 0;
		return voxels[x + z * SIZE + y * SIZE2];
	}
	
	public short getState(int x, int y, int z) {
		if (x < 0 || x >= SIZE) return 0;
		if (y < 0 || y >= SIZE) return 0;
		if (z < 0 || z >= SIZE) return 0;
		return getStateFast(x, y, z);
	}

	public short getStateFast(int x, int y, int z) {
		return states[x + z * SIZE + y * SIZE2];
	}

	public short getLight(int x, int y, int z) {
		if(x < 0 || x >= Chunk.SIZE || y < 0 || y >= Chunk.SIZE || z < 0 || z >= Chunk.SIZE) return 0;
		return light[x + z * SIZE + y * SIZE2];
	}
	
	public int getSkyLight(int x, int y, int z) {
		return 13;
	}
	
	public short setLight(int x, int y, int z, short light) {
		if(x < 0 || x >= Chunk.SIZE || y < 0 || y >= Chunk.SIZE || z < 0 || z >= Chunk.SIZE) return 0;
		this.needsRemeshing = true;
		return this.light[x + z * SIZE + y * SIZE2] = light;
	}

	public void clearLights() {
		Arrays.fill(light, (short) 0);
	}
	
	public void set(int x, int y, int z, byte voxel) {
		if (x < 0 || x >= SIZE) return;
		if (y < 0 || y >= SIZE) return;
		if (z < 0 || z >= SIZE) return;
		setFast(x, y, z, voxel);
	}

	public void setFast(int x, int y, int z, byte voxel) {
		voxels[x + z * SIZE + y * SIZE2] = voxel;
		states[x + z * SIZE + y * SIZE2] = 0;
		needsRemeshing = true;
		needsRelighting = true;
	}
	
	public void setState(int x, int y, int z, short state) {
		if (x < 0 || x >= SIZE) return;
		if (y < 0 || y >= SIZE) return;
		if (z < 0 || z >= SIZE) return;
		setStateFast(x, y, z, state);
	}

	public void setStateFast(int x, int y, int z, short state) {
		states[x + z * SIZE + y * SIZE2] = state;
		needsRemeshing = true;
		needsRelighting = true;
	}

	public VoxelPos getVoxelPos(int x, int y, int z) {
		return new VoxelPos(getChunkPos().x * Chunk.SIZE + x, getChunkPos().y * Chunk.SIZE + y, getChunkPos().z * Chunk.SIZE + z);
	}
	
	public VoxelPos getVoxelPos(int x, int y, int z, VoxelPos out) {
		return out.set(getChunkPos().x * Chunk.SIZE + x, getChunkPos().y * Chunk.SIZE + y, getChunkPos().z * Chunk.SIZE + z);
	}

	public ChunkPos getChunkPos() {
		return chunkPos;
	}
	
	public void setVoxels(byte[] voxels) {
		if(this.voxels.length != voxels.length) throw new IllegalArgumentException("Invalid array length");
		System.arraycopy(voxels, 0, this.voxels, 0, voxels.length);
	}
	
	public void setStates(short[] states) {
		if(this.states.length != states.length) throw new IllegalArgumentException("Invalid array length");
		System.arraycopy(states, 0, this.states, 0, states.length);
	}

	public void setBiomes(short[] biomes) {
		if(this.biomes.length != biomes.length) throw new IllegalArgumentException("Invalid array length");
		for(int i = 0; i < biomes.length; i++) {
			this.biomes[i] = Registries.BIOMES.get(Short.toUnsignedInt(biomes[i]));
		}
	}

	public static class Invalid extends Chunk {

		public Invalid(int chunkX, int chunkY, int chunkZ, World world) {
			super(chunkX, chunkY, chunkZ, world);
		}
		
		@Override
		public byte getFast(int x, int y, int z) {
			return 0;
		}

		@Override
		public void setFast(int x, int y, int z, byte voxel) {}
	}
}