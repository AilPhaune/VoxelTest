package fr.ailphaune.voxeltest.render.voxel;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.render.mesh.chunk.RenderLayer;
import fr.ailphaune.voxeltest.voxels.AbstractVoxel;

public interface VoxelRenderer {
	
	/**
	 * Returns the size of the generated vertices
	 * @return the size of the generated vertices
	 */
	public int vertexSize();
	
	/**
	 * Adds the vertices to the vertices array and returns the new offset in the vertices array
	 * @param vertices The array of vertices (max length 32767)
	 * @param vertexOffset The current offset in the vertices array
	 * @param x The x coordinate in the chunk
	 * @param y The y coordinate in the chunk
	 * @param z The z coordinate in the chunk
	 * @param chunk The chunk
	 * @param state The state of the voxel
	 * @return The new offset in the vertices array, -1 if not enough space left in the vertices array
	 * 
	 * @deprecated Use {@link #renderFaces(FaceMap, Pool, int, int, int, Chunk, short, AbstractVoxel)} instead
	 */
	@Deprecated
	public int render(float[] vertices, int vertexOffset, int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel);

	/**
	 * Returns whether or not this VoxelRenderer can render the given voxel
	 * @param x The x coordinate in the chunk
	 * @param y The y coordinate in the chunk
	 * @param z The z coordinate in the chunk
	 * @param chunk The chunk
	 * @param state The state of the voxel
	 * @param voxel The voxel
	 * @return
	 */
	public boolean renders(int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel);
	
	/**
	 * Returns whether or not this VoxelRenderer can render the given voxel as faces (see {@link #renderFaces(Array, Pool, int, int, int, Chunk, AbstractVoxel)})
	 * @param x The x coordinate in the chunk
	 * @param y The y coordinate in the chunk
	 * @param z The z coordinate in the chunk
	 * @param chunk The chunk
	 * @param state The state of the voxel
	 * @param voxel The voxel
	 * @return
	 */
	public boolean rendersAsFaces(int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel);
	
	/**
	 * Adds faces to draw to the faces array
	 * @param faces A map where faces will be added
	 * @param pool A pool used for obtaining {@link Face} objects
	 * @param x The x coordinate in the chunk
	 * @param y The y coordinate in the chunk
	 * @param z The z coordinate in the chunk
	 * @param chunk The chunk
	 * @param state The state of the voxel
	 * @param voxel The voxel
	 * @return true if the operation was successful, false otherwise
	 */
	public boolean renderFaces(FaceMap faces, Pool<Face> pool, int x, int y, int z, Chunk chunk, short state, AbstractVoxel voxel);
	
	/**
	 * Returns the render layer that should be used for this voxel renderer
	 * @return a render layer
	 */
	public RenderLayer getRenderLayer();

	/**
	 * Combines the two given faces into a single one if possible. The resulting face is put in the lastFace parameter.
	 * @param lastFace First face, also the result of the operation
	 * @param face Second face
	 * @param chunk The chunk in which the operation is happening
	 * @return true if the faces were combined, false otherwise
	 */
	public boolean combineFaces(int faceId, Face lastFace, Face face, Chunk chunk);
}