package fr.ailphaune.voxeltest.render.voxel;

import java.util.Arrays;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.pool.SynchronizedPool;
import fr.ailphaune.voxeltest.textures.TextureAtlas.AtlasRegion;

public class Face implements Poolable {

	private static final Vector3 tempVec3_1 = new Vector3();
	private static final Vector3 tempVec3_2 = new Vector3();

	private static final Vector2 tempVec2_1 = new Vector2();
	private static final Vector2 tempVec2_2 = new Vector2();
	private static final Vector2 tempVec2_3 = new Vector2();
	
	public final VertexAttributes vertexAttributes;
	
	/**
	 * The size of a vertex in number of floats
	 */
	public final int vertexSize;
	
	private final int offsetPosition, offsetTexCoord;
	
	public Face(VertexAttributes vertexAttributes) {
		this.vertexAttributes = vertexAttributes;
		this.firstPos = new VoxelPos();
		this.lastPos = new VoxelPos();
		vertices = new float[vertexAttributes.vertexSize];
		vertexSize = vertexAttributes.vertexSize / 4;

		offsetTexCoord = vertexAttributes.getOffset(Usage.TextureCoordinates);
		offsetPosition = vertexAttributes.getOffset(Usage.Position);
	}
	
	public float[] vertices;
	public final VoxelPos firstPos, lastPos;
	public AtlasRegion texture;
	
	// private Vector3 u = new Vector3(), v = new Vector3();
	
	@Override
	public void reset() {
		Arrays.fill(vertices, 0);
		firstPos.set(0, 0, 0);
		lastPos.set(0, 0, 0);
		texture = null;
	}

	protected int getClosestVertexIndex(Vector3 pos) {
		float dist1 = distSq(pos.x - vertices[0*vertexSize + offsetPosition], pos.y - vertices[0*vertexSize + offsetPosition + 1], pos.z - vertices[0*vertexSize + offsetPosition + 2]);
		float dist2 = distSq(pos.x - vertices[1*vertexSize + offsetPosition], pos.y - vertices[1*vertexSize + offsetPosition + 1], pos.z - vertices[1*vertexSize + offsetPosition + 2]);
		float dist3 = distSq(pos.x - vertices[2*vertexSize + offsetPosition], pos.y - vertices[2*vertexSize + offsetPosition + 1], pos.z - vertices[2*vertexSize + offsetPosition + 2]);
		float dist4 = distSq(pos.x - vertices[3*vertexSize + offsetPosition], pos.y - vertices[3*vertexSize + offsetPosition + 1], pos.z - vertices[3*vertexSize + offsetPosition + 2]);
		if(dist1 <= dist2 && dist1 <= dist3 && dist1 <= dist4) return 0;
		if(dist2 <= dist1 && dist2 <= dist3 && dist2 <= dist4) return 1;
		if(dist3 <= dist2 && dist3 <= dist1 && dist3 <= dist4) return 2;
		if(dist4 <= dist2 && dist4 <= dist3 && dist4 <= dist1) return 3;
		return 0;
	}
	
	/*private Vector3 getPosition(int vertex, Vector3 result) {
		return result.set(vertices[vertex*vertexSize + offsetPosition], vertices[vertex*vertexSize + offsetPosition + 1], vertices[vertex*vertexSize + offsetPosition + 2]);
	}*/

	private Vector2 getTextCoord(int vertex, Vector2 result) {
		return result.set(vertices[vertex*vertexSize + offsetTexCoord], vertices[vertex*vertexSize + offsetTexCoord + 1]);
	}
	
	private void setTextCoord(int vertex, Vector2 coord) {
		vertices[vertex*vertexSize + offsetTexCoord] = coord.x;
		vertices[vertex*vertexSize + offsetTexCoord + 1] = coord.y;
	}

	/**
	 * Combines faceB to this face.<br>
	 * Info: i'm not entirely sure if this code works, if face combining doesn't work, just don't use if
	 * @param faceB The face to combine to this one
	 */
	public void combine(Face faceB) {
		// FIXME: i'm not entirely sure if this code works, if it doesn't, just don't use it i guess
		faceB.lastPos.asVec3(tempVec3_1);
		int aIndex = getClosestVertexIndex(tempVec3_1);
		int bIndex = faceB.getClosestVertexIndex(tempVec3_1);

		//Vector2 aUV = getTextCoord(aIndex, tempVec2_1);
		Vector2 bUV = faceB.getTextCoord(bIndex, tempVec2_2);
		
		System.arraycopy(faceB.vertices, bIndex*vertexSize, vertices, aIndex*vertexSize, vertexSize);
		// lastPos = faceB.lastPos;
		
		tempVec3_1.sub(lastPos.x, lastPos.y, lastPos.z);
		int aIndex2 = getClosestVertexIndex(faceB.firstPos.asVec3(tempVec3_2));
		int bIndex2 = faceB.getClosestVertexIndex(tempVec3_1.add(tempVec3_2));

		//Vector2 a2UV = getTextCoord(aIndex2, tempVec2_4);
		Vector2 b2UV = faceB.getTextCoord(bIndex2, tempVec2_3);
		
		boolean isWrapU = Math.abs(bUV.y - b2UV.y) <= Math.abs(bUV.x - b2UV.x); // isWrapU = dv <= du
		
		System.arraycopy(faceB.vertices, bIndex2*vertexSize, vertices, aIndex2*vertexSize, vertexSize);
		
		if(isWrapU) {
			for(int vertex = 0; vertex < 4; vertex++) {
				setTextCoord(vertex, getTextCoord(vertex, tempVec2_1).add(0, 1));
			}
		} else {
			for(int vertex = 0; vertex < 4; vertex++) {
				setTextCoord(vertex, getTextCoord(vertex, tempVec2_1).add(1, 0));
			}
		}
		
		lastPos.set(faceB.lastPos);
	}
	
	private static final float distSq(float dx, float dy, float dz) {
		return dx*dx + dy*dy + dz*dz;
	}

	public static class FacePool extends SynchronizedPool<Face> {

		public final VertexAttributes vertexAttributes;
		
		public FacePool(VertexAttributes vertexAttributes) {
			this.vertexAttributes = vertexAttributes;
		}
		
		public FacePool(VertexAttributes vertexAttributes, int initialCapacity) {
			super(initialCapacity);
			this.vertexAttributes = vertexAttributes;
		}
		
		public FacePool(VertexAttributes vertexAttributes, int initialCapacity, int maxCapacity) {
			super(initialCapacity, maxCapacity);
			this.vertexAttributes = vertexAttributes;
		}
		
		@Override
		protected synchronized Face newObject() {
			return new Face(vertexAttributes);
		}
	}
}