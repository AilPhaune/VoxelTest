package fr.ailphaune.voxeltest.data;

import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.registries.Registry;
import fr.ailphaune.voxeltest.render.mesh.chunk.RenderLayer;
import fr.ailphaune.voxeltest.voxels.AbstractVoxel;
import fr.ailphaune.voxeltest.worldgen.biome.Biome;

public class Registries {
	
	public static final Registry<AbstractVoxel> VOXELS = new Registry<>();
	public static final Registry<RenderLayer> RENDER_LAYERS = new Registry<>();
	public static final Registry<Biome> BIOMES = new Registry<>();
	public static final Registry<Packet<?>> PACKETS = new Registry<>();
	
}