package fr.ailphaune.voxeltest.render.mesh.chunk;

import fr.ailphaune.voxeltest.VoxelTestConstants;
import fr.ailphaune.voxeltest.data.Registries;
import fr.ailphaune.voxeltest.registries.Identifier;
import fr.ailphaune.voxeltest.render.voxel.VoxelTextures;

public class RenderLayers {

	private static final String IDENTIFIER_TYPE_RENDER_LAYER = "render_layer";
	
	public static final RenderLayer cuboidRenderLayer = new CuboidRenderLayer(VoxelTextures.ATLAS_TERRAIN);
	public static int CUBOID_RENDER_LAYER;
	
	public static void register() {
		CUBOID_RENDER_LAYER = Registries.RENDER_LAYERS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, IDENTIFIER_TYPE_RENDER_LAYER, "cuboid_render_layer"), cuboidRenderLayer);
	}
}