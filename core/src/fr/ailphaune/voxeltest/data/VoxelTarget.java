package fr.ailphaune.voxeltest.data;

import com.badlogic.gdx.math.Vector3;

public class VoxelTarget {
	
	public VoxelPos pos;
	public int faceId;
	
	/**
	 * The coordinates the voxel intersected the ray (relative to {@link VoxelPos})
	 */
	public Vector3 intersectCoords;
	
	public VoxelTarget(VoxelPos pos, int faceId, Vector3 intersectCoords) {
		this.intersectCoords = intersectCoords;
		this.pos = pos;
		this.faceId = faceId;
	}
	
}