package fr.ailphaune.voxeltest.render.voxel;

import com.badlogic.gdx.math.Vector3;

public class VoxelIntersect {

	@FunctionalInterface
	public static interface RayCastCallback {
		boolean stopSearch(float intersectX, float intersectY, float intersectZ, Vector3 face);
	}

	static float distSq(float x, float y, float z, float x2, float y2, float z2) {
		return distSq(x - x2, y - y2, z - z2);
	}

	static float distSq(float dx, float dy, float dz) {
		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Call the callback with (x,y,z,value,face) of all blocks along the line
	 * segment from point 'origin' in vector direction 'direction' of length
	 * 'radius'. 'radius' may be infinite.
	 * 
	 * 'face' is the normal vector of the face of that block that was entered. It
	 * should not be used after the callback returns.
	 * 
	 * If the callback returns a true value, the traversal will be stopped.
	 */
	public static void raycastToVoxelGrid(Vector3 origin, Vector3 direction, float maxDist, RayCastCallback callback) {
		// From "A Fast Voxel Traversal Algorithm for Ray Tracing"
		// by John Amanatides and Andrew Woo, 1987
		// <http://www.cse.yorku.ca/~amana/research/grid.pdf>
		// <http://citeseer.ist.psu.edu/viewdoc/summary?doi=10.1.1.42.3443>
		// Extensions to the described algorithm:
		// • Imposed a distance limit.
		// • The face passed through to reach the current cube is provided to
		// the callback.

		// The foundation of this algorithm is a parameterized representation of
		// the provided ray,
		// origin + t * direction,
		// except that t is not actually stored; rather, at any given point in the
		// traversal, we keep track of the *greater* t values which we would have
		// if we took a step sufficient to cross a cube boundary along that axis
		// (i.e. change the integer part of the coordinate) in the variables
		// tMaxX, tMaxY, and tMaxZ.

		// Cube containing origin point.
		float x = (float) Math.floor(origin.x);
		float y = (float) Math.floor(origin.y);
		float z = (float) Math.floor(origin.z);
		// Break out direction vector.
		float dx = direction.x;
		float dy = direction.y;
		float dz = direction.z;
		// Direction to increment x,y,z when stepping.
		float stepX = signum(dx);
		float stepY = signum(dy);
		float stepZ = signum(dz);
		// See description above. The initial values depend on the fractional
		// part of the origin.
		float tMaxX = intbound(origin.x, dx);
		float tMaxY = intbound(origin.y, dy);
		float tMaxZ = intbound(origin.z, dz);
		// The change in t when taking a step (always positive).
		float tDeltaX = stepX / dx;
		float tDeltaY = stepY / dy;
		float tDeltaZ = stepZ / dz;
		// Buffer for reporting faces to the callback.
		Vector3 face = new Vector3();

		// Avoids an infinite loop.
		if (dx == 0 && dy == 0 && dz == 0) {
			return;
		}

		// Rescale from units of 1 cube-edge to units of 'direction' so we can
		// compare with 't'.
		float radius = (float) (maxDist / Math.sqrt(dx * dx + dy * dy + dz * dz));

		while (distSq(x, y, z, origin.x, origin.y, origin.z) < maxDist * maxDist) {

			if (callback.stopSearch(x, y, z, face))
				break;

			// tMaxX stores the t-value at which we cross a cube boundary along the
			// X axis, and similarly for Y and Z. Therefore, choosing the least tMax
			// chooses the closest cube boundary. Only the first case of the four
			// has been commented in detail.
			if (tMaxX < tMaxY) {
				if (tMaxX < tMaxZ) {
					if (tMaxX > radius)
						break;
					// Update which cube we are now in.
					x += stepX;
					// Adjust tMaxX to the next X-oriented boundary crossing.
					tMaxX += tDeltaX;
					// Record the normal vector of the cube face we entered.
					face.x = -stepX;
					face.y = 0;
					face.z = 0;
				} else {
					if (tMaxZ > radius)
						break;
					z += stepZ;
					tMaxZ += tDeltaZ;
					face.x = 0;
					face.y = 0;
					face.z = -stepZ;
				}
			} else {
				if (tMaxY < tMaxZ) {
					if (tMaxY > radius)
						break;
					y += stepY;
					tMaxY += tDeltaY;
					face.x = 0;
					face.y = -stepY;
					face.z = 0;
				} else {
					// Identical to the second case, repeated for simplicity in
					// the conditionals.
					if (tMaxZ > radius)
						break;
					z += stepZ;
					tMaxZ += tDeltaZ;
					face.x = 0;
					face.y = 0;
					face.z = -stepZ;
				}
			}
		}
	}

	static float intbound(float s, float ds) {
		// Find the smallest positive t such that s+t*ds is an integer.
		if (ds == 0) {
			return Float.POSITIVE_INFINITY;
		}
		if (ds < 0) {
			return intbound(-s, -ds);
		} else {
			s = mod(s, 1);
			// problem is now s+t*ds = 1
			return (1 - s) / ds;
		}
	}

	static int signum(float x) {
		return x > 0 ? 1 : -1;
	}

	static float mod(float value, int modulus) {
		return (value % modulus + modulus) % modulus;
	}
}