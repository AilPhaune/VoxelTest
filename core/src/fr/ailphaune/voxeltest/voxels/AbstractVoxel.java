package fr.ailphaune.voxeltest.voxels;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.collision.Ray;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.VoxelTarget;
import fr.ailphaune.voxeltest.data.actions.UseActionResult;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.registries.Identifier;
import fr.ailphaune.voxeltest.registries.Registrable;
import fr.ailphaune.voxeltest.registries.Registry;
import fr.ailphaune.voxeltest.render.voxel.VoxelRenderer;

public abstract class AbstractVoxel implements Registrable<AbstractVoxel> {

	private VoxelPos tempVoxelPos = new VoxelPos();
	
	protected Registry<AbstractVoxel> registry;
	protected Identifier id;
	protected int registryIndex = -1;
	
	@Override
	public void onRegister(Registry<AbstractVoxel> registry, Identifier id, int index) {
		this.registry = registry;
		this.id = id;
		this.registryIndex = index;
	}

	@Override
	public void onUnregister() {
		this.registry = null;
		this.id = null;
		this.registryIndex = -1;
	}

	@Override
	public boolean isRegistered() {
		return this.registry != null && this.id != null && this.registryIndex >= 0;
	}

	@Override
	public Identifier getIdentifier() {
		return id;
	}

	@Override
	public Registry<AbstractVoxel> getRegistry() {
		return registry;
	}

	@Override
	public int getIndex() {
		return registryIndex;
	}
	
	/**
	 * Returns an instance to the {@link VoxelRenderer} that should be used to render this block
	 * @return A {@link VoxelRenderer}
	 */
	public abstract VoxelRenderer getRenderer();
	
	/**
	 * Returns a {@link Mesh} of the outline of this block
	 * @param chunk The chunk of the voxel
	 * @param x The x coordinate in the chunk of the voxel
	 * @param y The y coordinate in the chunk of the voxel
	 * @param z The z coordinate in the chunk of the voxel
	 * @param state The state of the voxel
	 * @return a {@link Mesh}
	 */
	public abstract Mesh constructOutlineMesh(Chunk chunk, int x, int y, int z, short state);
	
	/**
	 * Calculates which face the ray is pointing to on this voxel
	 * @param chunk The chunk of the voxel
	 * @param x The x coordinate in the chunk of the voxel
	 * @param y The y coordinate in the chunk of the voxel
	 * @param z The z coordinate in the chunk of the voxel
	 * @param state The state of the voxel
	 * @param target A block target result already initialized with the {@link VoxelPos} of the voxel the ray is intersecting
	 * @param ray The ray
	 * @return true if there is an intersection, false otherwise
	 */
	public abstract boolean getVoxelTarget(Chunk chunk, int x, int y, int z, short state, VoxelTarget target, Ray ray);

	/**
	 * Is called whenever the player tries to use this voxel
	 * @param world The world in which the accessed voxel is
	 * @param target The voxel target
	 * @param voxelPlacementResultPos If {@link UseActionResult#VOXEL_PLACEMENT} is returned, this parameter is updated with the position where the voxel should be placed
	 * @return A {@link UseActionResult} representing the result of the action
	 */
	public abstract UseActionResult onPlayerUse(World world, VoxelTarget target, VoxelPos voxelPlacementResultPos);

	/**
	 * Returns the name of this voxel
	 * @return the name of this voxel
	 */
	public abstract String getName();

	/**
	 * Returns the light level emited by this voxel
	 * @param chunk The chunk
	 * @param voxelPos The world position of the voxel
	 * @return the light level emited in R, G, B format (4bit: 0-15 for each component)
	 */
	public abstract short getLightLevel(Chunk chunk, VoxelPos voxelPos);

	public abstract boolean isOpaque(Chunk chunk, int x, int y, int z);
	
	public boolean isOpaque(Chunk chunk, VoxelPos pos) {
		chunk.getChunkPos().getRelativeVoxelPos(pos, tempVoxelPos);
		return isOpaque(chunk, tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z);
	}
}