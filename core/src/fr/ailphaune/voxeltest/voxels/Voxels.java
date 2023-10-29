package fr.ailphaune.voxeltest.voxels;

import fr.ailphaune.voxeltest.VoxelTestConstants;
import fr.ailphaune.voxeltest.data.Registries;
import fr.ailphaune.voxeltest.registries.Identifier;
import fr.ailphaune.voxeltest.render.voxel.VoxelRenderer;
import fr.ailphaune.voxeltest.voxels.game.CubeVoxel;
import fr.ailphaune.voxeltest.voxels.game.EmptyVoxel;
import fr.ailphaune.voxeltest.voxels.game.SlabVoxel;

public class Voxels {

	private static final String IDENTIFIER_TYPE_VOXEL = "voxel";
	
	public static final AbstractVoxel air = new EmptyVoxel();
	public static byte AIR;

	public static final AbstractVoxel stone = new CubeVoxel("stone");
	public static byte STONE;
	
	public static final AbstractVoxel stoneSlab = new SlabVoxel("stone");
	public static byte STONE_SLAB;

	public static final AbstractVoxel dirt = new CubeVoxel("dirt");
	public static byte DIRT;

	public static final AbstractVoxel dirtSlab = new SlabVoxel("dirt");
	public static byte DIRT_SLAB;

	public static final AbstractVoxel sand = new CubeVoxel("sand");
	public static byte SAND;
	
	public static final AbstractVoxel grassBlock = new CubeVoxel("grass_block");
	public static byte GRASS_BLOCK;
	
	public static final AbstractVoxel grassBlockSlab = new SlabVoxel("grass_block");
	public static byte GRASS_BLOCK_SLAB;

	public static final AbstractVoxel whiteLamp= new CubeVoxel("white_lamp").setLighLevel(15, 15, 15);
	public static byte GLOWSTONE;
	 
	public static final AbstractVoxel redLamp = new CubeVoxel("red_lamp").setLighLevel(15, 0, 0);
	public static byte RED_LAMP;
	
	public static final AbstractVoxel greenLamp = new CubeVoxel("green_lamp").setLighLevel(0, 15, 0);
	public static byte GREEN_LAMP;
	
	public static final AbstractVoxel blueLamp = new CubeVoxel("blue_lamp").setLighLevel(0, 0, 15);
	public static byte BLUE_LAMP;
	
	public static final AbstractVoxel rotationDebug = new CubeVoxel("rotation_debug").setLighLevel(15, 15, 15);
	public static byte ROTATION_DEBUG;
	
	public static VoxelRenderer getRenderer(byte voxel) {
		return getAbstractVoxel(voxel).getRenderer();
	}

	public static AbstractVoxel getAbstractVoxel(byte voxel) {
		return Registries.VOXELS.get(voxel);
	}

	public static void register() {
		AIR = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "air"), air);
		STONE = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "stone"), stone);
		STONE_SLAB = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "stone_slab"), stoneSlab);
		DIRT = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "dirt"), dirt);
		DIRT_SLAB = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "dirt_slab"), dirtSlab);
		SAND = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "sand"), sand);
		GRASS_BLOCK = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "grass_block"), grassBlock);
		GRASS_BLOCK_SLAB = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "grass_block_slab"), grassBlockSlab);
		GLOWSTONE = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "white_lamp"), whiteLamp);
		RED_LAMP = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "red_lamp"), redLamp);
		GREEN_LAMP = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "green_lamp"), greenLamp);
		BLUE_LAMP = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "blue_lamp"), blueLamp);
		ROTATION_DEBUG = (byte) Registries.VOXELS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, Voxels.IDENTIFIER_TYPE_VOXEL, "rotation_debug"), rotationDebug);
	}
}