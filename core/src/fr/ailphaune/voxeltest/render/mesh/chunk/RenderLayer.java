package fr.ailphaune.voxeltest.render.mesh.chunk;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import fr.ailphaune.voxeltest.registries.Registrable;
import fr.ailphaune.voxeltest.render.voxel.VoxelChunk;
import fr.ailphaune.voxeltest.textures.TextureAtlas;

public interface RenderLayer extends Registrable<RenderLayer> {
	
	public int renderToMeshData(Array<MeshData> meshes, MeshData.MeshDataPool pool, VoxelChunk voxelChunk);

	/**
	 * Returns the texture atlas to bind before rendering generated meshes
	 * @return a texture atlas
	 */
	public TextureAtlas getTextureAtlas();

	public Pool<Mesh> getMeshPool();
}