package fr.ailphaune.voxeltest.multiplayer.server;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.data.world.WorldLoadRegion;
import fr.ailphaune.voxeltest.entities.PlayerEntity;
import fr.ailphaune.voxeltest.multiplayer.server.Server.ClientConnection;

public class OnlinePlayer {

	protected PlayerEntity entity;

	// Package private
	World world;
	WorldLoadRegion.PlayerWorldLoadRegion loadRegion;

	public OnlinePlayer(PlayerEntity entity, World world, ClientConnection connection) {
		this.entity = entity;
		this.world = world;
		Server server = connection.getServer();
		ChunkPos loadCenter = entity.getVoxelPos(VoxelPos.TEMP).getChunkPos();
		this.loadRegion = new WorldLoadRegion.PlayerWorldLoadRegion(world, loadCenter, server.getSimulationDistanceXZ(), server.getSimulationDistanceY(), server.getSimulationDistanceXZ());
	}

	public PlayerEntity getPlayerEntity() {
		return entity;
	}

	public World getWorld() {
		return world;
	}

	public WorldLoadRegion.PlayerWorldLoadRegion getLoadRegion() {
		return loadRegion;
	}
}