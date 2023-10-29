package fr.ailphaune.voxeltest.render.voxel;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.render.mesh.chunk.RenderLayer;
import fr.ailphaune.voxeltest.textures.TextureAtlas.AtlasRegion;
import fr.ailphaune.voxeltest.voxels.AbstractVoxel;
import fr.ailphaune.voxeltest.voxels.Voxels;
import fr.ailphaune.voxeltest.voxels.game.CubeVoxel;

public class CubeVoxelRenderer implements VoxelRenderer {

	public static int VERTEX_SIZE = 10;
	
	private RenderLayer renderLayer;
	
	private VoxelPos tempVoxelPos = new VoxelPos();
	
	public CubeVoxelRenderer(RenderLayer renderLayer) {
		this.renderLayer = renderLayer;
	}
	
	private CubeVoxel voxel;
	private Chunk chunk;
	
	private int posX, posY, posZ;
	private short state;
	
	private Vector3 pos = new Vector3(), size = new Vector3();
	
	@Override
	public int render(float[] vertices, int vertexOffset, int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel) {
		if(!(voxel instanceof CubeVoxel) || !renders(x, y, z, chunk, state, voxel)) return vertexOffset;
		this.voxel = (CubeVoxel) voxel;
		this.chunk = chunk;
		this.voxel.getCubeModel(chunk, x, y, z, state, pos, size);
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.state = state;
		
		if (y < Chunk.SIZE - 1) {
			if (chunk.getFast(x, y + 1, z) == 0) vertexOffset = createTop(x, y, z, vertices, vertexOffset);
		} else {
			vertexOffset = createTop(x, y, z, vertices, vertexOffset);
		}
		if (y > 0) {
			if (chunk.getFast(x, y - 1, z) == 0) vertexOffset = createBottom(x, y, z, vertices, vertexOffset);
		} else {
			vertexOffset = createBottom(x, y, z, vertices, vertexOffset);
		}
		if (x > 0) {
			if (chunk.getFast(x - 1, y, z) == 0) vertexOffset = createLeft(x, y, z, vertices, vertexOffset);
		} else {
			vertexOffset = createLeft(x, y, z, vertices, vertexOffset);
		}
		if (x < Chunk.SIZE - 1) {
			if (chunk.getFast(x + 1, y, z) == 0) vertexOffset = createRight(x, y, z, vertices, vertexOffset);
		} else {
			vertexOffset = createRight(x, y, z, vertices, vertexOffset);
		}
		if (z > 0) {
			if (chunk.getFast(x, y, z - 1) == 0) vertexOffset = createFront(x, y, z, vertices, vertexOffset);
		} else {
			vertexOffset = createFront(x, y, z, vertices, vertexOffset);
		}
		if (z < Chunk.SIZE - 1) {
			if (chunk.getFast(x, y, z + 1) == 0) vertexOffset = createBack(x, y, z, vertices, vertexOffset);
		} else {
			vertexOffset = createBack(x, y, z, vertices, vertexOffset);
		}
		
		this.chunk = null;
		
		return vertexOffset;
	}
	
	private int addTextureCoordsToVertices(float[] vertices, int vertexOffset, int face, int vertex) {
		AtlasRegion region = voxel.getTextureForFace(face, chunk, posX, posY, posZ, state);
		if(region == null) {
			vertices[vertexOffset++] = -1;
			vertices[vertexOffset++] = -1;
		} else {
			vertices[vertexOffset++] = region.getU(vertex);
			vertices[vertexOffset++] = region.getV(vertex);
		}
		return 2;
	}
	
	public int vertexPos(float x, float y, float z, float[] vertices, int vertexOffset) {
		vertices[vertexOffset++] = x;
		vertices[vertexOffset++] = y;
		vertices[vertexOffset++] = z;
		return 3;
	}
	
	public int vertexNormal(float x, float y, float z, float[] vertices, int vertexOffset) {
		vertices[vertexOffset++] = x;
		vertices[vertexOffset++] = y;
		vertices[vertexOffset++] = z;
		return 3;
	}

	public int addLighting(int x, int y, int z, float[] vertices, int vertexOffset) {
		chunk.getVoxelPos(x, y, z, tempVoxelPos);
		vertices[vertexOffset++] = chunk.getWorld().getLightFast(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z);
		return 1;
	}
	
	public int addColorToVertices(float[] vertices, int vertexOffset, int face, int vertex) {
		vertices[vertexOffset++] = voxel.getModelVertexColor(face, vertex).toFloatBits();
		return 1;
	}
	
	// FACE_POS_Y
	public int createTop(int x, int y, int z, float[] vertices, int vertexOffset) {
		// Greedy meshing info
		
		// Top Right
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y + size.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 1, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Y, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Y, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addLighting(x, y + 1, z, vertices, vertexOffset);

		// Top Left
		vertexOffset += vertexPos(x + pos.x, y + pos.y + size.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 1, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Y, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Y, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addLighting(x, y + 1, z, vertices, vertexOffset);

		// Bottom Left
		vertexOffset += vertexPos(x + pos.x, y + pos.y + size.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 1, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Y, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Y, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addLighting(x, y + 1, z, vertices, vertexOffset);

		// Bottom Right
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y + size.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 1, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Y, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Y, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addLighting(x, y + 1, z, vertices, vertexOffset);
		return vertexOffset;
	}

	// FACE_NEG_Y
	public int createBottom(int x, int y, int z, float[] vertices, int vertexOffset) {
		// Top Right
		vertexOffset += vertexPos(x + pos.x, y + pos.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, -1, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Y, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Y, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addLighting(x, y - 1, z, vertices, vertexOffset);

		// Top Left
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, -1, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Y, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Y, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addLighting(x, y - 1, z, vertices, vertexOffset);

		// Bottom Left
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, -1, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Y, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Y, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addLighting(x, y - 1, z, vertices, vertexOffset);

		// Bottom Right
		vertexOffset += vertexPos(x + pos.x, y + pos.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, -1, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Y, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Y, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addLighting(x, y - 1, z, vertices, vertexOffset);
		return vertexOffset;
	}

	// FACE_NEG_X
	public int createLeft(int x, int y, int z, float[] vertices, int vertexOffset) {
		// Top Right
		vertexOffset += vertexPos(x + pos.x, y + pos.y + size.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(-1, 0, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_X, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_X, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addLighting(x - 1, y, z, vertices, vertexOffset);

		// Top Left
		vertexOffset += vertexPos(x + pos.x, y + pos.y + size.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(-1, 0, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_X, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_X, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addLighting(x - 1, y, z, vertices, vertexOffset);

		// Bottom Left
		vertexOffset += vertexPos(x + pos.x, y + pos.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(-1, 0, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_X, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_X, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addLighting(x - 1, y, z, vertices, vertexOffset);

		// Bottom Right
		vertexOffset += vertexPos(x + pos.x, y + pos.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(-1, 0, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_X, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_X, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addLighting(x - 1, y, z, vertices, vertexOffset);
		return vertexOffset;
	}

	// FACE_POS_X
	public int createRight(int x, int y, int z, float[] vertices, int vertexOffset) {
		// Top Right
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y + size.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(1, 0, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_X, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_X, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addLighting(x + 1, y, z, vertices, vertexOffset);

		// Top Left
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y + size.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(1, 0, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_X, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_X, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addLighting(x + 1, y, z, vertices, vertexOffset);

		// Bottom Left
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(1, 0, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_X, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_X, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addLighting(x + 1, y, z, vertices, vertexOffset);

		// Bottom Right
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(1, 0, 0, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_X, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_X, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addLighting(x + 1, y, z, vertices, vertexOffset);
		return vertexOffset;
	}

	// FACE_NEG_Z
	public int createFront(int x, int y, int z, float[] vertices, int vertexOffset) {
		// Bottom Left
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 0, 1, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Z, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Z, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addLighting(x, y, z - 1, vertices, vertexOffset);

		// Bottom Right
		vertexOffset += vertexPos(x + pos.x, y + pos.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 0, 1, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Z, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Z, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addLighting(x, y, z - 1, vertices, vertexOffset);
		
		// Top Right
		vertexOffset += vertexPos(x + pos.x, y + pos.y + size.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 0, 1, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Z, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Z, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addLighting(x, y, z - 1, vertices, vertexOffset);

		// Top Left
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y + size.y, z + pos.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 0, 1, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Z, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_NEG_Z, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addLighting(x, y, z - 1, vertices, vertexOffset);
		return vertexOffset;
	}

	// FACE_POS_Z
	public int createBack(int x, int y, int z, float[] vertices, int vertexOffset) {
		// Top Right
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y + size.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 0, -1, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Z, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Z, CubeVoxel.FACE_VERTEX_TOP_RIGHT);
		vertexOffset += addLighting(x, y, z + 1, vertices, vertexOffset);

		// Top Left
		vertexOffset += vertexPos(x + pos.x, y + pos.y + size.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 0, -1, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Z, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Z, CubeVoxel.FACE_VERTEX_TOP_LEFT);
		vertexOffset += addLighting(x, y, z + 1, vertices, vertexOffset);

		// Bottom Left
		vertexOffset += vertexPos(x + pos.x, y + pos.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 0, -1, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Z, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Z, CubeVoxel.FACE_VERTEX_BOTTOM_LEFT);
		vertexOffset += addLighting(x, y, z + 1, vertices, vertexOffset);

		// Bottom Right
		vertexOffset += vertexPos(x + pos.x + size.x, y + pos.y, z + pos.z + size.z, vertices, vertexOffset);
		vertexOffset += vertexNormal(0, 0, -1, vertices, vertexOffset);
		vertexOffset += addColorToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Z, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addTextureCoordsToVertices(vertices, vertexOffset, CubeVoxel.FACE_POS_Z, CubeVoxel.FACE_VERTEX_BOTTOM_RIGHT);
		vertexOffset += addLighting(x, y, z + 1, vertices, vertexOffset);
		return vertexOffset;
	}

	@Override
	public int vertexSize() {
		return VERTEX_SIZE;
	}

	@Override
	public boolean renders(int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel) {
		return voxel != null && (voxel instanceof CubeVoxel) && voxel.getRenderer() == this;
	}

	@Override
	public RenderLayer getRenderLayer() {
		return renderLayer;
	}

	@Override
	public boolean renderFaces(FaceMap faces, Pool<Face> pool, int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel) {
		if(!(voxel instanceof CubeVoxel) || !renders(x, y, z, chunk, state, voxel)) return false;
		this.voxel = (CubeVoxel) voxel;
		this.chunk = chunk;
		this.voxel.getCubeModel(chunk, x, y, z, state, pos, size);
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.state = state;
		
		synchronized(chunk) {
		try {

		boolean transparent = !this.voxel.isOpaque(chunk, x, y, z);
		
		if (y < Chunk.SIZE - 1) {
			if (transparent || !Voxels.getAbstractVoxel(chunk.getFast(x, y + 1, z)).isOpaque(chunk, x, y + 1, z)) {
			 	Face face = pool.obtain();
				face.firstPos.set(x, y + 1, z);
				face.lastPos.set(x + 1, y + 1, z + 1);
				face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_POS_Y, chunk, x, y, z, state);
				createTop(x, y, z, face.vertices, 0);
				if(!faces.addFace(CubeVoxel.FACE_POS_Y, face, this)) {
					pool.free(face);
				}
			}
		} else {
			Face face = pool.obtain();
			face.firstPos.set(x, y + 1, z);
			face.lastPos.set(x + 1, y + 1, z + 1);
			face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_POS_Y, chunk, x, y, z, state);
			createTop(x, y, z, face.vertices, 0);
			if(!faces.addFace(CubeVoxel.FACE_POS_Y, face, this)) {
				pool.free(face);
			}
		}
		if (y > 0) {
			if (transparent || !Voxels.getAbstractVoxel(chunk.getFast(x, y - 1, z)).isOpaque(chunk, x, y - 1, z)) {
				Face face = pool.obtain();
				face.firstPos.set(x, y, z);
				face.lastPos.set(x + 1, y, z + 1);
				createBottom(x, y, z, face.vertices, 0);
				face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_NEG_Y, chunk, x, y, z, state);
				if(!faces.addFace(CubeVoxel.FACE_NEG_Y, face, this)) {
					pool.free(face);
				}
			}
		} else {
			Face face = pool.obtain();
			face.firstPos.set(x, y, z);
			face.lastPos.set(x + 1, y, z + 1);
			createBottom(x, y, z, face.vertices, 0);
			face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_NEG_Y, chunk, x, y, z, state);
			if(!faces.addFace(CubeVoxel.FACE_NEG_Y, face, this)) {
				pool.free(face);
			}
		}
		if (x > 0) {
			if (transparent || !Voxels.getAbstractVoxel(chunk.getFast(x - 1, y, z)).isOpaque(chunk, x - 1, y, z)) {
				Face face = pool.obtain();
				face.firstPos.set(x, y, z);
				face.lastPos.set(x, y + 1, z + 1);
				createLeft(x, y, z, face.vertices, 0);
				face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_NEG_X, chunk, x, y, z, state);
				if(!faces.addFace(CubeVoxel.FACE_NEG_X, face, this)) {
					pool.free(face);
				}
			}
		} else {
			Face face = pool.obtain();
			face.firstPos.set(x, y, z);
			face.lastPos.set(x, y + 1, z + 1);
			createLeft(x, y, z, face.vertices, 0);
			face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_NEG_X, chunk, x, y, z, state);
			if(!faces.addFace(CubeVoxel.FACE_NEG_X, face, this)) {
				pool.free(face);
			}
		}
		if (x < Chunk.SIZE - 1) {
			if (transparent || !Voxels.getAbstractVoxel(chunk.getFast(x + 1, y, z)).isOpaque(chunk, x + 1, y, z)) {
				Face face = pool.obtain();
				face.firstPos.set(x + 1, y, z);
				face.lastPos.set(x + 1, y + 1, z + 1);
				createRight(x, y, z, face.vertices, 0);
				face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_POS_X, chunk, x, y, z, state);
				if(!faces.addFace(CubeVoxel.FACE_POS_X, face, this)) {
					pool.free(face);
				}
			}
		} else {
			Face face = pool.obtain();
			face.firstPos.set(x + 1, y, z);
			face.lastPos.set(x + 1, y + 1, z + 1);
			createRight(x, y, z, face.vertices, 0);
			face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_POS_X, chunk, x, y, z, state);
			if(!faces.addFace(CubeVoxel.FACE_POS_X, face, this)) {
				pool.free(face);
			}
		}
		if (z > 0) {
			if (transparent || !Voxels.getAbstractVoxel(chunk.getFast(x, y, z - 1)).isOpaque(chunk, x, y, z - 1))  {
				Face face = pool.obtain();
				face.firstPos.set(x, y, z);
				face.lastPos.set(x + 1, y + 1, z);
				createFront(x, y, z, face.vertices, 0);
				face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_NEG_Z, chunk, x, y, z, state);
				if(!faces.addFace(CubeVoxel.FACE_NEG_Z, face, this)) {
					pool.free(face);
				}
			}
		} else {
			Face face = pool.obtain();
			face.firstPos.set(x, y, z);
			face.lastPos.set(x + 1, y + 1, z);
			createFront(x, y, z, face.vertices, 0);
			face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_NEG_Z, chunk, x, y, z, state);
			if(!faces.addFace(CubeVoxel.FACE_NEG_Z, face, this)) {
				pool.free(face);
			}
		}
		if (z < Chunk.SIZE - 1) {
			if (transparent || !Voxels.getAbstractVoxel(chunk.getFast(x, y, z + 1)).isOpaque(chunk, x, y, z + 1)) {
				Face face = pool.obtain();
				face.firstPos.set(x, y, z + 1);
				face.lastPos.set(x + 1, y + 1, z + 1);
				createBack(x, y, z, face.vertices, 0);
				face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_POS_Z, chunk, x, y, z, state);
				if(!faces.addFace(CubeVoxel.FACE_POS_Z, face, this)) {
					pool.free(face);
				}
			}
		} else {
			Face face = pool.obtain();
			face.firstPos.set(x, y, z + 1);
			face.lastPos.set(x + 1, y + 1, z + 1);
			createBack(x, y, z, face.vertices, 0);
			face.texture = this.voxel.getTextureForFace(CubeVoxel.FACE_POS_Z, chunk, x, y, z, state);
			if(!faces.addFace(CubeVoxel.FACE_POS_Z, face, this)) {
				pool.free(face);
			}
		}
		
		} catch(Throwable t) {
			t.printStackTrace();
		}
		}
		
		return true;
	}

	@Override
	public boolean rendersAsFaces(int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel) {
		return voxel != null && (voxel instanceof CubeVoxel) && voxel.getRenderer() == this;
	}

	@Override
	public boolean combineFaces(int faceId, Face faceA, Face faceB, Chunk chunk) {
		// i'm too lazy to make this work with texture atlases and i don't want to change to texture arrays right now. Maybe i'll do it later but for the moment, my main goal is to have a working game
		return false;
		/*
		if(faceId == CubeVoxel.FACE_POS_X || faceId == CubeVoxel.FACE_NEG_X) return false;
		
		if(!faceA.texture.equals(faceB.texture)) {
			// not same texture, can't combine
			return false;
		}

		int aXLen = faceA.lastPos.x - faceA.firstPos.x;
		int aYLen = faceA.lastPos.y - faceA.firstPos.y;
		int aZLen = faceA.lastPos.z - faceA.firstPos.z;
		
		int bXLen = faceB.lastPos.x - faceB.firstPos.x;
		int bYLen = faceB.lastPos.y - faceB.firstPos.y;
		int bZLen = faceB.lastPos.z - faceB.firstPos.z;
		
		// Check adjacency
		if(faceA.firstPos.z != faceB.firstPos.z || faceA.lastPos.z != faceB.lastPos.z) return false;
		if(faceA.firstPos.y == faceB.firstPos.y && faceA.lastPos.y == faceB.lastPos.y) {
			if(aZLen != bZLen || aYLen != bYLen) return false;
			if(Math.abs(faceA.firstPos.x + aXLen - faceB.firstPos.x) > 0 && Math.abs(faceB.firstPos.x + bXLen - faceA.firstPos.x) > 0) return false;
		} else if(faceA.firstPos.x == faceB.firstPos.x) {
			if(aXLen != bXLen || aZLen != bZLen) return false;
			if(Math.abs(faceA.firstPos.y + aYLen - faceB.firstPos.y) > 0 && Math.abs(faceB.firstPos.y + bYLen - faceA.firstPos.y) > 0) return false;
		} else return false;
		
		// Combine faces
		faceA.combine(faceB);
		
		return true;
		*/
	}
}