package fr.ailphaune.voxeltest.data;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public interface Hitbox {
	
	/**
	 * Renders this Hitbox to the screen at the given world position
	 * @param position
	 */
	public void render(Vector3 position);
	
	/**
	 * Calculates whether or not the given ray intersects this {@link Hitbox} and if it does, the position of the intersection is written to the intersectPos {@link Vector3}
	 * @param ray The ray to test
	 * @param intersectPos The output for the position of intersection
	 * @return true if intersects, false otherwise
	 */
	public boolean intersects(Ray ray, Vector3 intersectPos);
}