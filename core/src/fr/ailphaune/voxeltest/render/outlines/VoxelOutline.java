package fr.ailphaune.voxeltest.render.outlines;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.voxels.Voxels;

public class VoxelOutline extends OutlineRenderer {

	private static ChunkPos tempChunkPos = new ChunkPos();
	private static VoxelPos tempVoxelPos = new VoxelPos();
	
	
	public VoxelOutline(World world, VoxelPos pos) {
		set(world, pos);
	}
	
	public VoxelOutline() {}

	public void set(World world, VoxelPos pos) {
		pos.getChunkPos(tempChunkPos);
		tempChunkPos.getRelativeVoxelPos(pos, tempVoxelPos);
		Chunk chunk = world.getChunk(tempChunkPos);
		mesh = Voxels.getAbstractVoxel(world.getVoxel(pos)).constructOutlineMesh(chunk, tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z, chunk.getStateFast(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z));
	}
}