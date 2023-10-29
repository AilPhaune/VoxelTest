package fr.ailphaune.voxeltest.data.world;

import fr.ailphaune.voxeltest.light.Lighting;
import fr.ailphaune.voxeltest.multiplayer.Client;
import fr.ailphaune.voxeltest.multiplayer.packet.Packets;
import fr.ailphaune.voxeltest.multiplayer.packet.s2c.VoxelUpdatesPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.s2c.WorldChunkPacket;

public class ConnectedClientWorld extends ClientWorld {

	public ConnectedClientWorld(Client client, Lighting lighting) {
		super(lighting);
		client.addPacketListener(Packets.WORLD_CHUNK, this::onWorldChunkPacket);
		client.addPacketListener(Packets.VOXEL_UPDATES, this::onVoxelUpdatesPacket);
	}
	
	protected void onWorldChunkPacket(WorldChunkPacket.Data data, Client client) {
		Chunk chunk = getChunk(data.chunkPos);
		if(chunk == null) return;
		chunk.setVoxels(data.voxels);
		chunk.setStates(data.states);
		chunk.setBiomes(data.biomes);
		chunk.needsRelighting = true;
		chunk.needsRemeshing = true;
		addLoadedChunk(chunk);
	}
	
	protected void onVoxelUpdatesPacket(VoxelUpdatesPacket.Data data, Client client) {
		for(VoxelUpdatesPacket.Update update : data.updates) {
			setVoxel(update.x, update.y, update.z, update.voxel);
			setVoxelState(update.x, update.y, update.z, update.state);
		}
	}
}