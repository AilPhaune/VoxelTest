package fr.ailphaune.voxeltest.render.outlines;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Vector3;

public class BoxOutline extends OutlineRenderer {
	
	private int vertexOffset = 0;
	private int indexOffset = 0;
	
	private int divisions;
	private float dx, dy, dz;
	private Vector3 pos;
	
	public BoxOutline(Vector3 pos, Vector3 size, int divisions) {
		dx = size.x / divisions;
		dy = size.y / divisions;
		dz = size.z / divisions;
		this.pos = pos;
		this.divisions = divisions;
		int vertexCount = (divisions+1)*(divisions+1)*6;
		float vertices[] = new float[vertexCount*3];
		int lineCountPerFace = divisions*(divisions+1)*2;
		short indices[] = new short[lineCountPerFace*6*2];

		makeY(vertices, indices, pos.y);
		makeY(vertices, indices, pos.y + size.y);
		makeX(vertices, indices, pos.x);
		makeX(vertices, indices, pos.x + size.x);
		makeZ(vertices, indices, pos.z);
		makeZ(vertices, indices, pos.z + size.z);
		
		mesh = new Mesh(true, vertices.length, indices.length, VertexAttribute.Position());
		mesh.setVertices(vertices);
		mesh.setIndices(indices);
	}
	
	private void makeY(float[] vertices, short[] indices, float y) {
		short curVertexI = (short) (this.vertexOffset / 3);
		
		float x = pos.x, z;
		for(int ix = 0; ix <= this.divisions; ix++) {
			z = pos.z;
			for(int iz = 0; iz <= this.divisions; iz++) {
				vertices[vertexOffset++] = x;
				vertices[vertexOffset++] = y;
				vertices[vertexOffset++] = z;
				if(iz < this.divisions) {
					indices[indexOffset++] = curVertexI;
					indices[indexOffset++] = (short) (curVertexI + 1);
				}
				if(ix < this.divisions) {
					indices[indexOffset++] = curVertexI;
					indices[indexOffset++] = (short) (curVertexI + divisions + 1);
				}
				curVertexI++;
				z += dz;
			}
			x += dx;
		}
	}
	
	private void makeX(float[] vertices, short[] indices, float x) {
		short curVertexI = (short) (this.vertexOffset / 3);
		
		float y = pos.y, z;
		for(int iy = 0; iy <= this.divisions; iy++) {
			z = pos.z;
			for(int iz = 0; iz <= this.divisions; iz++) {
				vertices[vertexOffset++] = x;
				vertices[vertexOffset++] = y;
				vertices[vertexOffset++] = z;
				if(iz < this.divisions) {
					indices[indexOffset++] = curVertexI;
					indices[indexOffset++] = (short) (curVertexI + 1);
				}
				if(iy < this.divisions) {
					indices[indexOffset++] = curVertexI;
					indices[indexOffset++] = (short) (curVertexI + divisions + 1);
				}
				curVertexI++;
				z += dz;
			}
			y += dy;
		}
	}
	

	
	private void makeZ(float[] vertices, short[] indices, float z) {
		short curVertexI = (short) (this.vertexOffset / 3);
		
		float y = pos.y, x;
		for(int iy = 0; iy <= this.divisions; iy++) {
			x = pos.z;
			for(int ix = 0; ix <= this.divisions; ix++) {
				vertices[vertexOffset++] = x;
				vertices[vertexOffset++] = y;
				vertices[vertexOffset++] = z;
				if(ix < this.divisions) {
					indices[indexOffset++] = curVertexI;
					indices[indexOffset++] = (short) (curVertexI + 1);
				}
				if(iy < this.divisions) {
					indices[indexOffset++] = curVertexI;
					indices[indexOffset++] = (short) (curVertexI + divisions + 1);
				}
				curVertexI++;
				x += dx;
			}
			y += dy;
		}
	}
}