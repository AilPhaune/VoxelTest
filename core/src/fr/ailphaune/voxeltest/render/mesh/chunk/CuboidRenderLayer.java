package fr.ailphaune.voxeltest.render.mesh.chunk;

import java.util.Map;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import fr.ailphaune.voxeltest.data.pool.MeshPool;
import fr.ailphaune.voxeltest.registries.Identifier;
import fr.ailphaune.voxeltest.registries.Registry;
import fr.ailphaune.voxeltest.render.voxel.Face;
import fr.ailphaune.voxeltest.render.voxel.FaceMap;
import fr.ailphaune.voxeltest.render.voxel.VoxelChunk;
import fr.ailphaune.voxeltest.render.voxel.VoxelRenderer;
import fr.ailphaune.voxeltest.render.voxel.Face.FacePool;
import fr.ailphaune.voxeltest.textures.TextureAtlas;
import fr.ailphaune.voxeltest.voxels.AbstractVoxel;
import fr.ailphaune.voxeltest.voxels.Voxels;
import fr.ailphaune.voxeltest.voxels.game.CubeVoxel;

public class CuboidRenderLayer implements RenderLayer {

	private static Array<VertexAttribute> attributes;
	private static final VertexAttributes vertexAttributes;
	public static final int vertexSize;
	
	private static MeshPool MESH_POOL;
	
	static {
		attributes = new Array<>();
		attributes.add(VertexAttribute.Position());
		attributes.add(VertexAttribute.Normal());
		attributes.add(VertexAttribute.ColorPacked());
		attributes.add(VertexAttribute.TexCoords(0));
		attributes.add(new VertexAttribute(Usage.Generic, 1, "a_light"));
		
		vertexAttributes = new VertexAttributes(attributes.toArray(VertexAttribute.class));
		vertexSize = vertexAttributes.vertexSize / 4;
		
		MESH_POOL = new MeshPool(vertexAttributes, 16, 64);
	}
	
	public final TextureAtlas atlas;
	
	public CuboidRenderLayer(TextureAtlas atlas) {
		this.atlas = atlas;
		this.facePool = new FacePool(vertexAttributes, 32, 16384);
	}
	
	private Registry<RenderLayer> registry;
	private Identifier id;
	private int index;
	
	@Override
	public void onRegister(Registry<RenderLayer> registry, Identifier id, int index) {
		this.registry = registry;
		this.id = id;
		this.index = index;
	}
	
	@Override
	public void onUnregister() {
		this.registry = null;
		this.id = null;
		this.index = -1;
	}
	
	@Override
	public boolean isRegistered() {
		return id != null && registry != null;
	}
	
	@Override
	public Identifier getIdentifier() {
		return id;
	}
	
	@Override
	public Registry<RenderLayer> getRegistry() {
		return registry;
	}
	
	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public TextureAtlas getTextureAtlas() {
		return atlas;
	}

	protected FacePool facePool;

	@Override
	public synchronized int renderToMeshData(Array<MeshData> meshes, MeshData.MeshDataPool pool, VoxelChunk voxelChunk) {
		VoxelRenderer renderer = CubeVoxel.CUBE_RENDERER;
		
		FaceMap faces = new FaceMap(voxelChunk.chunkToRender);
		
		MeshData data = meshes.size == 0 ? null : meshes.get(meshes.size - 1);
		if(data == null || data.vertexSize != vertexSize) {
			data = pool.obtain(vertexSize);
			meshes.add(data);
		}
		
		for (int y = 0; y < voxelChunk.size; y++) {
			for (int z = 0; z < voxelChunk.size; z++) {
				for (int x = 0; x < voxelChunk.size; x++) {
					byte voxelid = voxelChunk.chunkToRender.getFast(x, y, z);
					
					if(voxelid == 0) continue;
					
					short state = voxelChunk.chunkToRender.getStateFast(x, y, z);
					
					AbstractVoxel voxel = Voxels.getAbstractVoxel(voxelid);
					
					if(renderer.rendersAsFaces(x, y, z, voxelChunk.chunkToRender, state, voxel)) {
						renderer.renderFaces(faces, facePool, x, y, z, voxelChunk.chunkToRender, state, voxel);
					} else if(renderer.renders(x, y, z, voxelChunk.chunkToRender, state, voxel)) {
						System.err.println("<CuboidRenderLayer> Use renderFaces instead of render");
						// vertexOffset = renderer.render(vertices, vertexOffset, x, y, z, voxelChunk.chunkToRender, state, voxel);
					}
				}
			}
		}
		
		int numVertices = 0;
		
		for(Map.Entry<Integer, Array<Face>> entry : faces.getEntries()) {
			Array<Face> f = entry.getValue();
			for(Face face : f) {
				if(!data.addFace(face)) {
					data = pool.obtain(vertexSize);
					meshes.add(data);
					data.addFace(face);
				}
				numVertices += 4;
			}
			facePool.freeAll(f);
		}
		
		return numVertices;
	}

	@Override
	public Pool<Mesh> getMeshPool() {
		return MESH_POOL;
	}
}