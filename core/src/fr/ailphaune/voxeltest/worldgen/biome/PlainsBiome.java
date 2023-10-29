package fr.ailphaune.voxeltest.worldgen.biome;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.voxels.Voxels;
import fr.ailphaune.voxeltest.worldgen.WorldGenerator;

public class PlainsBiome extends Biome {

	private VoxelPos tempVoxelPos = new VoxelPos();
	
	public PlainsBiome() {
		super(Voxels.DIRT, Voxels.GRASS_BLOCK);
	}
	
	@Override
	public void generateDistinctTerrainChunkColumn(World world, Chunk chunk, WorldGenerator generator, int x, int z) {
		for(int y = 0; y < Chunk.SIZE; y++) {
			VoxelPos worldPos = chunk.getVoxelPos(x, y, z, tempVoxelPos);
			
			double heightDiff = heightMultiplier*generator.terrainHeight.noise(worldPos.x / 16.0, worldPos.z / 16.0);
			double height = Math.round(biomeTerrainHeight + heightDiff);
			
			if(chunk.getVoxelPos(x, y, z).y >= height - 1 && chunk.get(x, y, z) == Voxels.STONE) {
				chunk.set(x, y, z, surfaceVoxel);
			} else if(chunk.getVoxelPos(x, y, z).y >= height - 4 && chunk.get(x, y, z) == Voxels.STONE) {
				chunk.set(x, y, z, underSurfaceVoxel);
			}
		}
	}
}