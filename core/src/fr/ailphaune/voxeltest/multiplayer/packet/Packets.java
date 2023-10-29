package fr.ailphaune.voxeltest.multiplayer.packet;

import fr.ailphaune.voxeltest.VoxelTestConstants;
import fr.ailphaune.voxeltest.data.Registries;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.ClientConnectionPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.DestroyVoxelPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.PlaceVoxelPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.PlayerMovePacket;
import fr.ailphaune.voxeltest.multiplayer.packet.s2c.JoinWorldPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.s2c.VoxelUpdatesPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.s2c.WorldChunkPacket;
import fr.ailphaune.voxeltest.registries.Identifier;

public class Packets {

	public static final String TYPE_PACKET = "packet";
	
	public static final DefaultPacket<String> PING_PONG = new DefaultPacket<>("PingPongPacket");
	
	public static final ClientConnectionPacket CLIENT_CONNECTION = new ClientConnectionPacket();

	public static final PlayerMovePacket PLAYER_MOVE_PACKET = new PlayerMovePacket();

	public static final JoinWorldPacket JOIN_WORLD = new JoinWorldPacket();

	public static final WorldChunkPacket WORLD_CHUNK = new WorldChunkPacket();

	public static final DestroyVoxelPacket DESTROY_VOXEL = new DestroyVoxelPacket();
	public static final PlaceVoxelPacket PLACE_VOXEL = new PlaceVoxelPacket();
	public static final VoxelUpdatesPacket VOXEL_UPDATES = new VoxelUpdatesPacket();
	
	public static void register() {
		Registries.PACKETS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, TYPE_PACKET, "ping_pong"), PING_PONG);
		Registries.PACKETS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, TYPE_PACKET, "client_connection"), CLIENT_CONNECTION);
		Registries.PACKETS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, TYPE_PACKET, "join_world"), JOIN_WORLD);
		Registries.PACKETS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, TYPE_PACKET, "player_move"), PLAYER_MOVE_PACKET);
		Registries.PACKETS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, TYPE_PACKET, "world_chunk"), WORLD_CHUNK);
		Registries.PACKETS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, TYPE_PACKET, "destroy_voxel"), DESTROY_VOXEL);
		Registries.PACKETS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, TYPE_PACKET, "place_voxel"), PLACE_VOXEL);
		Registries.PACKETS.register(new Identifier(VoxelTestConstants.DEFAULT_PROVIDER, TYPE_PACKET, "voxel_updates"), VOXEL_UPDATES);
	}
	
}