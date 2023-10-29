package fr.ailphaune.voxeltest.worldgen.biome;

import fr.ailphaune.voxeltest.VoxelTestConstants;
import fr.ailphaune.voxeltest.data.Registries;
import fr.ailphaune.voxeltest.registries.Identifier;

public class Biomes {
	
	private static final String PROVIDER_TYPE_BIOME = "biome";

	public static final Biome plains = new PlainsBiome();
	public static int PLAINS;
	
	public static final Biome desert = new DesertBiome();
	public static int DESERT;
	
	public static void register() {
		DESERT = Registries.BIOMES.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, PROVIDER_TYPE_BIOME, "desert"), desert);
		PLAINS = Registries.BIOMES.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, PROVIDER_TYPE_BIOME, "plains"), plains);
	}
	
}