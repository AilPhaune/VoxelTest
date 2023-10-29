package fr.ailphaune.voxeltest.render.mesh.chunk;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

import fr.ailphaune.voxeltest.data.pool.SynchronizedPool;
import fr.ailphaune.voxeltest.render.voxel.Face;

import com.badlogic.gdx.utils.ShortArray;

public class MeshData implements Poolable {
	
	protected ShortArray indices;
	protected FloatArray vertices;
	
	public final int maxIndices;
	/**
	 * The size of a vertex in number of floats
	 */
	public int vertexSize;
	
	public MeshData(int maxIndices, int vertexSize) {
		this.indices = new ShortArray(2048);
		this.vertices = new FloatArray(2048);
		this.maxIndices = maxIndices;
		this.vertexSize = vertexSize;
	}
	
	public synchronized boolean addFace(Face face) {
		synchronized(indices) {
		synchronized(vertices) {
		synchronized(face) {
			
			if(indices.size + 6 > maxIndices) return false;
			int index = vertices.size / vertexSize;
			// first triangle
			indices.add(index);
			indices.add(index + 1);
			indices.add(index + 2);
			// second triangle
			indices.add(index + 0);
			indices.add(index + 2);
			indices.add(index + 3);
		
			vertices.addAll(face.vertices, 0, face.vertices.length);
			
		}
		}
		}
		return true;
	}

	@Override
	public synchronized void reset() {
		synchronized(indices) {
			indices.clear();
		}
		synchronized(vertices) {
			vertices.clear();
		}
	}
	
	public synchronized ShortArray getIndices() {
		return indices;
	}
	
	public static class MeshDataPool extends SynchronizedPool<MeshData> {

		public MeshDataPool() { super(); }
		public MeshDataPool(int initialCapacity) { super(initialCapacity); }
		public MeshDataPool(int initialCapacity, int max) { super(initialCapacity, max); }
		
		@Override
		protected synchronized MeshData newObject() {
			return new MeshData(Short.MAX_VALUE, 1);
		}
		
		public synchronized MeshData obtain(int vertexSize) {
			MeshData data = super.obtain();
			data.vertexSize = vertexSize;
			return data;
		}
		
		@Override
		public synchronized MeshData obtain() {
			throw new RuntimeException(new IllegalAccessException("Use MeshDataPool.obtain(int vertexSize)"));
		}
	}

	/**
	 * Converts the given {@link MeshData}s to regular LibGDX {@link Mesh}es
	 * @param meshDatas The mesh datas
	 * @param outMeshes The mesh output array
	 * @param meshPool the pool used to allocate new meshes
	 */
	public static void convertToMeshes(Array<MeshData> meshDatas, Array<Mesh> outMeshes, Pool<Mesh> meshPool) {
		synchronized(meshDatas) { synchronized(outMeshes) {
			for(MeshData data : meshDatas) {
				synchronized(data) {
					Mesh mesh = meshPool.obtain();
					mesh.setVertices(data.vertices.items, 0, data.vertices.size);
					mesh.setIndices(data.indices.items, 0, data.indices.size);
					outMeshes.add(mesh);
				}
			}
		}}
	}
}