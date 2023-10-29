package fr.ailphaune.voxeltest.worldgen;

import fr.ailphaune.voxeltest.data.PerlinNoiseGenerator;
import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.worldgen.biome.Biome;
import fr.ailphaune.voxeltest.worldgen.biome.Biomes;

public class WorldGenerator {
	
	public static final long TERRAIN_HEIGHT_COMPOUND = 0;
	public static final long TEMPERATURE_MAP_COMPOUND = 1;
	
	private VoxelPos tempVoxelPos = new VoxelPos();
	
	public final World world;

	public PerlinNoiseGenerator terrainHeight;
	public PerlinNoiseGenerator temperatureMap;
	
	public WorldGenerator(World world) {
		this.world = world;
		this.terrainHeight = new PerlinNoiseGenerator(world.getCompoundSeed(TERRAIN_HEIGHT_COMPOUND));
		this.temperatureMap = new PerlinNoiseGenerator(world.getCompoundSeed(TEMPERATURE_MAP_COMPOUND));
	}
	
	public synchronized Biome getBiomeAt(Chunk chunk, VoxelPos pos) {
		if(temperatureMap.noise(pos.x / 128.0, pos.z / 128.0) > 0.7) {
			return Biomes.desert;
		}
		return Biomes.plains;
	}
	
	public synchronized void proceduralGenerateChunk(Chunk chunk) {
		for(int x = 0; x < Chunk.SIZE; x++) {
			for(int z = 0; z < Chunk.SIZE; z++) {
				chunk.setBiome(x, z, getBiomeAt(chunk, chunk.getVoxelPos(x, 0, z, tempVoxelPos)));
			}
		}
		
		for(int x = 0; x < Chunk.SIZE; x++) {
			for(int z = 0; z < Chunk.SIZE; z++) {
				chunk.getBiome(x, z).generateTerrainChunkColumn(world, chunk, this, x, z);
			}
		}
		
		for(int x = 0; x < Chunk.SIZE; x++) {
			for(int z = 0; z < Chunk.SIZE; z++) {
				chunk.getBiome(x, z).generateDistinctTerrainChunkColumn(world, chunk, this, x, z);
			}
		}
	}
}