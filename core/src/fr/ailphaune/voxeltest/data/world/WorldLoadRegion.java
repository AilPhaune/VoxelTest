package fr.ailphaune.voxeltest.data.world;

import java.util.List;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.VoxelPos;

public class WorldLoadRegion {

	public World world;
	
	protected ChunkPos tempChunkPos = new ChunkPos();

	public WorldLoadRegion(World world) {
		this.world = world;
	}

	/**
	 * Checks whether or not this {@link WorldLoadRegion} keeps the given chunk
	 * loaded
	 * 
	 * @param pos The position of the chunk to test
	 * @return true if this {@link WorldLoadRegion} keeps this chunk loaded, false
	 *         otherwise
	 */
	public boolean keepsChunkLoaded(ChunkPos pos) {
		return false;
	}

	public boolean contains(VoxelPos position) {
		return keepsChunkLoaded(position.getChunkPos(tempChunkPos));
	}

	public static class PlayerWorldLoadRegion extends WorldLoadRegion {

		public ChunkPos playerChunk;
		public int distanceX, distanceY, distanceZ;

		public PlayerWorldLoadRegion(World world, ChunkPos playerChunk, int distance) {
			this(world, playerChunk, distance, distance, distance);
		}

		public PlayerWorldLoadRegion(World world, ChunkPos playerChunk, int distanceX, int distanceY, int distanceZ) {
			super(world);
			this.playerChunk = playerChunk;
			this.distanceX = distanceX;
			this.distanceY = distanceY;
			this.distanceZ = distanceZ;
		}

		@Override
		public boolean keepsChunkLoaded(ChunkPos pos) {
			int dx = Math.abs(pos.x - playerChunk.x);
			int dy = Math.abs(pos.y - playerChunk.y);
			int dz = Math.abs(pos.z - playerChunk.z);
			return dx <= distanceX && dy <= distanceY && dz <= distanceZ;
		}
	}

	public static boolean allowsUnload(List<WorldLoadRegion> loadRegions, ChunkPos pos) {
		for (WorldLoadRegion region : loadRegions) {
			if (region != null && region.keepsChunkLoaded(pos)) {
				return false;
			}
		}
		return true;
	}
}