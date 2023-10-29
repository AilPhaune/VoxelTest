package fr.ailphaune.voxeltest.voxels.game;

import java.util.Arrays;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.VoxelTarget;
import fr.ailphaune.voxeltest.data.actions.UseActionResult;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.light.Lighting;
import fr.ailphaune.voxeltest.render.mesh.chunk.RenderLayers;
import fr.ailphaune.voxeltest.render.voxel.CubeVoxelRenderer;
import fr.ailphaune.voxeltest.render.voxel.VoxelRenderer;
import fr.ailphaune.voxeltest.textures.TextureAtlas;
import fr.ailphaune.voxeltest.textures.TextureAtlas.AtlasRegion;
import fr.ailphaune.voxeltest.voxels.AbstractVoxel;

public class CubeVoxel extends AbstractVoxel {

	public static final int FACE_POS_X = 0;
	public static final int FACE_POS_Y = 1;
	public static final int FACE_POS_Z = 2;
	public static final int FACE_NEG_X = 3;
	public static final int FACE_NEG_Y = 4;
	public static final int FACE_NEG_Z = 5;

	// the binary representation of the vertices correspond to the UV coordinates
	public static final int FACE_VERTEX_BOTTOM_LEFT = 0;
	public static final int FACE_VERTEX_BOTTOM_RIGHT = 1;
	public static final int FACE_VERTEX_TOP_LEFT = 2;
	public static final int FACE_VERTEX_TOP_RIGHT = 3;
	
	private static final short OUTLINE_INDICES[] = {
			// -Y
			0, 1,
			1, 2,
			2, 3,
			3, 0,
			// +Y
			4, 5,
			5, 6,
			6, 7,
			7, 4,
			// x, x+4
			0, 4,
			1, 5,
			2, 6,
			3, 7
	};
	
	public static CubeVoxelRenderer CUBE_RENDERER = new CubeVoxelRenderer(RenderLayers.cuboidRenderLayer);
	
	public static Color TRANSPARENT = new Color(0, 0, 0, 0);
	
	protected Color faceColor = TRANSPARENT;
	protected String atlasRegionName[] = new String[6];
	
	protected short lightLevel = 0;
	
	/**
	 * Generates a default cube voxel that has no texture, and white face color
	 */
	public CubeVoxel() {
		this(Color.WHITE);
	}
	
	/**
	 * Generates a default cube voxel that has no texture, with faces of the given color
	 * @param faceColor The color of the faces of this voxel
	 */
	public CubeVoxel(Color faceColor) {
		this.faceColor = faceColor;
	}
	
	/**
	 * Generates a default cube voxel that has no color, using the given atlas region name as a texture
	 * @param atlasRegionName The name of the atlas region to use as a texture
	 */
	public CubeVoxel(String atlasRegionName) {
		Arrays.fill(this.atlasRegionName, atlasRegionName);
		this.faceColor = Color.RED;
	}
	
	public CubeVoxel(String atlasRegionNameFacePosX, String atlasRegionNameFaceNegX, String atlasRegionNameFacePosY, String atlasRegionNameFaceNegY, String atlasRegionNameFacePosZ, String atlasRegionNameFaceNegZ) {
		this.atlasRegionName[FACE_POS_X] = atlasRegionNameFacePosX;
		this.atlasRegionName[FACE_NEG_X] = atlasRegionNameFaceNegX;
		this.atlasRegionName[FACE_POS_Y] = atlasRegionNameFacePosY;
		this.atlasRegionName[FACE_NEG_Y] = atlasRegionNameFaceNegY;
		this.atlasRegionName[FACE_POS_Z] = atlasRegionNameFacePosZ;
		this.atlasRegionName[FACE_NEG_Z] = atlasRegionNameFaceNegZ;
	}
	
	@Override
	public VoxelRenderer getRenderer() {
		return CUBE_RENDERER;
	}
	
	public void getCubeModel(Chunk chunk, int x, int y, int z, short state, Vector3 outPos, Vector3 outSize) {
		outPos.set(0, 0, 0);
		outSize.set(1, 1, 1);
	}

	public Color getModelVertexColor(int face, int vertex) {
		return faceColor;
	}
	
	private AtlasRegion atlasRegion[] = new AtlasRegion[6];
	public AtlasRegion getTextureForFace(int face, Chunk chunk, int x, int y, int z, short state) {
		if(face < 0 || face >= 6) return null;
		if(atlasRegion[face] != null) return atlasRegion[face];
		if(atlasRegionName == null) return null;
		TextureAtlas atlas = getRenderer().getRenderLayer().getTextureAtlas();
		if(atlas == null) return null;
		AtlasRegion region = atlas.getRegion(atlasRegionName[face]);
		atlasRegion[face] = region;
		return atlasRegion[face];
	}

	@Override
	public Mesh constructOutlineMesh(Chunk chunk, int x, int y, int z, short state) {
		float vertices[] = {
			0, 0, 0,
			1, 0, 0,
			1, 0, 1,
			0, 0, 1,
			0, 1, 0,
			1, 1, 0,
			1, 1, 1,
			0, 1, 1,
		};
		Mesh mesh = new Mesh(true, vertices.length, OUTLINE_INDICES.length, VertexAttribute.Position());
		mesh.setVertices(vertices);
		mesh.setIndices(OUTLINE_INDICES);
		return mesh;
	}

	protected Vector3 _getVoxelTarget_intersection = new Vector3();
	protected Vector3 _getVoxelTarget_minVec = new Vector3();
	protected Vector3 _getVoxelTarget_maxVec = new Vector3();
	protected BoundingBox _getVoxelTarget_bounds = new BoundingBox();
	
	@Override
	public boolean getVoxelTarget(Chunk chunk, int posX, int posY, int posZ, short state, VoxelTarget target, Ray ray) {
		if(Intersector.intersectRayBounds(ray, _getVoxelTarget_bounds.set(target.pos.asVec3(_getVoxelTarget_minVec), target.pos.asVec3(_getVoxelTarget_maxVec).add(1,1,1)), _getVoxelTarget_intersection)) {
			target.intersectCoords.set(_getVoxelTarget_intersection.sub(target.pos.x, target.pos.y, target.pos.z));
			Vector3 intersect = _getVoxelTarget_intersection.sub(0.5f);
			float x = Math.abs(intersect.x);
			float y = Math.abs(intersect.y);
			float z = Math.abs(intersect.z);
			
			if(x > y && x > z) {
				target.faceId = intersect.x >= 0 ? FACE_POS_X : FACE_NEG_X;
			} else if(y > x && y > z) {
				target.faceId = intersect.y >= 0 ? FACE_POS_Y : FACE_NEG_Y;
			} else if(z > x && z > y) {
				target.faceId = intersect.z >= 0 ? FACE_POS_Z : FACE_NEG_Z;
			} else {
				target.faceId = -1;
			}
			
			return true;
		}
		return false;
	}

	@Override
	public UseActionResult onPlayerUse(World world, VoxelTarget target, VoxelPos voxelPlacementResultPos) {
		voxelPlacementResultPos.set(target.pos);
		if(target.faceId == FACE_POS_X) {
			voxelPlacementResultPos.x++;
		} else if(target.faceId == FACE_NEG_X) {
			voxelPlacementResultPos.x--;
		} else if(target.faceId == FACE_POS_Y) {
			voxelPlacementResultPos.y++;
		} else if(target.faceId == FACE_NEG_Y) {
			voxelPlacementResultPos.y--;
		} else if(target.faceId == FACE_POS_Z) {
			voxelPlacementResultPos.z++;
		} else if(target.faceId == FACE_NEG_Z) {
			voxelPlacementResultPos.z--;
		} else {
			return UseActionResult.NONE;
		}
		return UseActionResult.VOXEL_PLACEMENT;
	}

	@Override
	public String getName() {
		return getIdentifier().name;
	}
	
	public CubeVoxel setLighLevel(int red, int green, int blue) {
		lightLevel = Lighting.getLight(red, green, blue);
		return this;
	}

	@Override
	public short getLightLevel(Chunk chunk, VoxelPos voxelPos) {
		return lightLevel;
	}

	@Override
	public boolean isOpaque(Chunk chunk, int x, int y, int z) {
		return true;
	}
}