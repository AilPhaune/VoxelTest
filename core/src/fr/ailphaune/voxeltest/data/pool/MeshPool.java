package fr.ailphaune.voxeltest.data.pool;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;

public class MeshPool extends SynchronizedPool<Mesh> {

	private VertexAttributes attributes;
	
	public MeshPool(VertexAttributes attributes) {
		super();
		this.attributes = attributes;
	}
	
	public MeshPool(VertexAttributes attributes, int initialCapacity) {
		super(initialCapacity);
		this.attributes = attributes;
	}
	
	public MeshPool(VertexAttributes attributes, int initialCapacity, int max) {
		super(initialCapacity, max);
		this.attributes = attributes;
	}
	
	@Override
	protected Mesh newObject() {
		return new Mesh(true, Short.MAX_VALUE, 3 * Short.MAX_VALUE / 2, attributes);
	}
}