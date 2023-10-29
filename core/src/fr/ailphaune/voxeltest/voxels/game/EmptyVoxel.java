package fr.ailphaune.voxeltest.voxels.game;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pool;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.VoxelTarget;
import fr.ailphaune.voxeltest.data.actions.UseActionResult;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.render.mesh.chunk.RenderLayer;
import fr.ailphaune.voxeltest.render.voxel.Face;
import fr.ailphaune.voxeltest.render.voxel.FaceMap;
import fr.ailphaune.voxeltest.render.voxel.VoxelRenderer;
import fr.ailphaune.voxeltest.voxels.AbstractVoxel;

public class EmptyVoxel extends AbstractVoxel {

	public static VoxelRenderer EMPTY_RENDERER = new VoxelRenderer() {
		@Override
		public int render(float[] vertices, int vertexOffset, int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel) {
			return vertexOffset;
		}
		@Override
		public int vertexSize() {
			return 0;
		}
		@Override
		public RenderLayer getRenderLayer() {
			return null;
		}
		@Override
		public boolean renders(int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel) {
			return false;
		}
		@Override
		public boolean renderFaces(FaceMap faces, Pool<Face> pool, int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel) {
			return true;
		}
		@Override
		public boolean rendersAsFaces(int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel) {
			return false;
		}
		@Override
		public boolean combineFaces(int faceId, Face lastFace, Face face, Chunk chunk) {
			return false;
		}
	};
	
	@Override
	public VoxelRenderer getRenderer() {
		return EMPTY_RENDERER;
	}

	@Override
	public Mesh constructOutlineMesh(Chunk chunk, int x, int y, int z, short state) {
		return null;
	}

	@Override
	public boolean getVoxelTarget(Chunk chunk, int x, int y, int z, short state, VoxelTarget target, Ray ray) {
		return false;
	}

	@Override
	public UseActionResult onPlayerUse(World world, VoxelTarget target, VoxelPos voxelPlacementResultPos) {
		return UseActionResult.NONE;
	}

	@Override
	public String getName() {
		return ":empty";
	}

	@Override
	public short getLightLevel(Chunk chunk, VoxelPos voxelPos) {
		return 0;
	}

	@Override
	public boolean isOpaque(Chunk chunk, VoxelPos voxelPos) {
		return false;
	}

	@Override
	public boolean isOpaque(Chunk chunk, int x, int y, int z) {
		return false;
	}
}