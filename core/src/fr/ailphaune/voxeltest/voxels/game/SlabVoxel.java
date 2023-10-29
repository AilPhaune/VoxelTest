package fr.ailphaune.voxeltest.voxels.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.VoxelTarget;
import fr.ailphaune.voxeltest.data.actions.UseActionResult;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.textures.TextureAtlas.AtlasRegion;

public class SlabVoxel extends CubeVoxel {

	public static final short STATE_POS_X = 0;
	public static final short STATE_NEG_X = 1;
	public static final short STATE_POS_Y = 2;
	public static final short STATE_NEG_Y = 3;
	public static final short STATE_POS_Z = 4;
	public static final short STATE_NEG_Z = 5;
	public static final short STATE_FULL  = 6;
	
	public SlabVoxel() {
		super();
	}
 
	public SlabVoxel(Color faceColor) {
		super(faceColor);
	}
	
	public SlabVoxel(String atlasRegionName) {
		super(atlasRegionName);
	}
	
	public SlabVoxel(String atlasRegionNameFacePosX, String atlasRegionNameFaceNegX, String atlasRegionNameFacePosY, String atlasRegionNameFaceNegY, String atlasRegionNameFacePosZ, String atlasRegionNameFaceNegZ) {
		super(atlasRegionNameFacePosX, atlasRegionNameFaceNegX, atlasRegionNameFacePosY, atlasRegionNameFaceNegY, atlasRegionNameFacePosZ, atlasRegionNameFaceNegZ);
	}
	
	private AtlasRegion textureFaces[] = new AtlasRegion[64];
	
	@Override
	public AtlasRegion getTextureForFace(int face, Chunk chunk, int x, int y, int z, short state) {
		int idx = ((face & 7) << 3) + (state & 7);
		if(textureFaces[idx] != null) return textureFaces[idx];
		AtlasRegion def = super.getTextureForFace(face, chunk, x, y, z, state);
		switch(state) {
			case STATE_POS_X:
				if(face == FACE_NEG_X || face == FACE_POS_X) return textureFaces[idx] = def;
				return textureFaces[idx] = def.subRegion(0.5f, 0, 1, 1);
			case STATE_NEG_X:
				if(face == FACE_NEG_X || face == FACE_POS_X) return textureFaces[idx] = def;
				return textureFaces[idx] = def.subRegion(0, 0, 0.5f, 1);
			case STATE_POS_Y:
				if(face == FACE_NEG_Y || face == FACE_POS_Y) return textureFaces[idx] = def;
				return textureFaces[idx] = def.subRegion(0, 0.5f, 1, 1);
			case STATE_NEG_Y:
				if(face == FACE_NEG_Y || face == FACE_POS_Y) return textureFaces[idx] = def;
				return textureFaces[idx] = def.subRegion(0, 0.5f, 1, 1);
			case STATE_POS_Z:
				if(face == FACE_NEG_Z || face == FACE_POS_Z) return textureFaces[idx] = def;
				if(face == FACE_NEG_Y || face == FACE_POS_Y) return textureFaces[idx] = def.subRegion(0, 0.5f, 1, 1);
				return textureFaces[idx] = def.subRegion(0.5f, 0, 1, 1);
			case STATE_NEG_Z:
				if(face == FACE_NEG_Z || face == FACE_POS_Z) return textureFaces[idx] = def;
				if(face == FACE_NEG_Y || face == FACE_POS_Y) return textureFaces[idx] = def.subRegion(0, 0, 1, 0.5f);
				return textureFaces[idx] = def.subRegion(0, 0, 0.5f, 1);
			default:
				return textureFaces[idx] = def;
		}
	}
	
	@Override
	public void getCubeModel(Chunk chunk, int x, int y, int z, short state, Vector3 outPos, Vector3 outSize) {
		switch(state) {
			case STATE_POS_X:
				outPos.set(0.5f, 0, 0);
				outSize.set(0.5f, 1, 1);
				break;
			case STATE_POS_Y:
				outPos.set(0, 0.5f, 0);
				outSize.set(1, 0.5f, 1);
				break;
			case STATE_POS_Z:
				outPos.set(0, 0, 0.5f);
				outSize.set(1, 1, 0.5f);
				break;
			case STATE_NEG_X:
				outPos.set(0, 0, 0);
				outSize.set(0.5f, 1, 1);
				break;
			case STATE_NEG_Y:
				outPos.set(0, 0, 0);
				outSize.set(1, 0.5f, 1);
				break;
			case STATE_NEG_Z:
				outPos.set(0, 0, 0);
				outSize.set(1, 1, 0.5f);
				break;
			default:
				super.getCubeModel(chunk, x, y, z, state, outPos, outSize);
				break;
		}
	}
	
	@Override
	public UseActionResult onPlayerUse(World world, VoxelTarget target, VoxelPos voxelPlacementResultPos) {
		world.setVoxelState(target.pos, (short) ((world.getVoxelState(target.pos) + 1) % 7));
		return UseActionResult.USE;
	}
	
	@Override
	public boolean isOpaque(Chunk chunk, int x, int y, int z) {
		return chunk.getStateFast(x, y, z) == STATE_FULL;
	}
}