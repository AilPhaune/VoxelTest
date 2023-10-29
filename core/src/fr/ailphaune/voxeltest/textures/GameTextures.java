package fr.ailphaune.voxeltest.textures;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;

import fr.ailphaune.voxeltest.render.voxel.VoxelTextures;

public class GameTextures {
	
	private static final String pathVoxels = "voxeltest/textures/voxel/";
	
	public static void register() {
		AssetManager manager = new AssetManager();
		TextureManager terrainTextures = VoxelTextures.ATLAS_TERRAIN.getManager();

		terrainTextures.addRegion(loadPixmap(manager, pathVoxels + "stone.png"), "stone");
		terrainTextures.addRegion(loadPixmap(manager, pathVoxels + "dirt.png"), "dirt");
		terrainTextures.addRegion(loadPixmap(manager, pathVoxels + "sand.png"), "sand");
		terrainTextures.addRegion(loadPixmap(manager, pathVoxels + "grass_block.png"), "grass_block");
		terrainTextures.addRegion(loadPixmap(manager, pathVoxels + "white_lamp.png"), "white_lamp");
		terrainTextures.addRegion(loadPixmap(manager, pathVoxels + "red_lamp.png"), "red_lamp");
		terrainTextures.addRegion(loadPixmap(manager, pathVoxels + "green_lamp.png"), "green_lamp");
		terrainTextures.addRegion(loadPixmap(manager, pathVoxels + "blue_lamp.png"), "blue_lamp");
		
		terrainTextures.addRegion(loadPixmap(manager, pathVoxels + "rotation_debug.png"), "rotation_debug");
	}
	
	private static Pixmap loadPixmap(AssetManager manager, String name) {
		manager.load(name, Pixmap.class);
		return manager.finishLoadingAsset(name);
	}
}