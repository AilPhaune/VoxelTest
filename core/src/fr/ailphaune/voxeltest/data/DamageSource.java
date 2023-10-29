package fr.ailphaune.voxeltest.data;

import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.data.world.World;

public interface DamageSource {
	
	/**
	 * Returns the which world the {@link DamageSource} is in
	 * @return the which world the {@link DamageSource} is in
	 */
	public World getWorld();
	
	/**
	 * Writes the position of this {@link DamageSource} to the out {@link Vector3} and returns it
	 * @param out The output {@link Vector3} in which the position is written
	 * @return The out {@link Vector3}
	 */
	public Vector3 getPosition(Vector3 out);
	
}