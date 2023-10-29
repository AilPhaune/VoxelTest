package fr.ailphaune.voxeltest.worldgen.biome;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.registries.Identifier;
import fr.ailphaune.voxeltest.registries.Registrable;
import fr.ailphaune.voxeltest.registries.Registry;
import fr.ailphaune.voxeltest.voxels.Voxels;
import fr.ailphaune.voxeltest.worldgen.WorldGenerator;

public class Biome implements Registrable<Biome> {

	private VoxelPos tempVoxelPos = new VoxelPos();

	protected double heightMultiplier;
	protected double biomeTerrainHeight;
	
	protected byte underSurfaceVoxel, surfaceVoxel;
	
	public Biome() {
		this(Voxels.STONE, Voxels.STONE);
	}
	
	public Biome(byte underSurfaceVoxel, byte surfaceVoxel) {
		heightMultiplier = 15.0;
		biomeTerrainHeight = 60.0;
		this.underSurfaceVoxel = underSurfaceVoxel;
		this.surfaceVoxel = surfaceVoxel;
	}
	
	private Registry<Biome> registry;
	private Identifier id;
	private int idx;
	
	@Override
	public void onRegister(Registry<Biome> registry, Identifier id, int index) {
		this.registry = registry;
		this.id = id;
		this.idx = index;
	}

	@Override
	public void onUnregister() {
		id = null;
		idx = 0;
		registry = null;
	}

	@Override
	public boolean isRegistered() {
		return id != null && registry != null;
	}

	@Override
	public Identifier getIdentifier() {
		return id;
	}

	@Override
	public Registry<Biome> getRegistry() {
		return registry;
	}

	@Override
	public int getIndex() {
		return idx;
	}

	public void generateTerrainChunkColumn(World world, Chunk chunk, WorldGenerator generator, int x, int z) {
		for(int y = 0; y < Chunk.SIZE; y++) {
			VoxelPos worldPos = chunk.getVoxelPos(x, y, z, tempVoxelPos);
			
			double heightDiff = heightMultiplier*generator.terrainHeight.noise(worldPos.x / 16.0, worldPos.z / 16.0);
			int height = (int) Math.round(biomeTerrainHeight + heightDiff);
			
			if(chunk.getVoxelPos(x, y, z).y < height) {
				chunk.set(x, y, z, Voxels.STONE);
			}
		}
	}

	public void generateDistinctTerrainChunkColumn(World world, Chunk chunk, WorldGenerator generator, int x, int z) {
		for(int y = 0; y < Chunk.SIZE; y++) {
			VoxelPos worldPos = chunk.getVoxelPos(x, y, z, tempVoxelPos);
			
			double heightDiff = heightMultiplier*generator.terrainHeight.noise(worldPos.x / 16.0, worldPos.z / 16.0);
			int height = (int) Math.round(biomeTerrainHeight + heightDiff);
			
			if(chunk.getVoxelPos(x, y, z).y >= height - 1 && chunk.get(x, y, z) == Voxels.STONE) {
				chunk.set(x, y, z, surfaceVoxel);
			} else if(chunk.getVoxelPos(x, y, z).y >= height - 4 && chunk.get(x, y, z) == Voxels.STONE) {
				chunk.set(x, y, z, underSurfaceVoxel);
			}
		}
	}
}