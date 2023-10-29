package fr.ailphaune.voxeltest.multiplayer;

import java.io.IOException;

import fr.ailphaune.voxeltest.multiplayer.packet.Packet.PacketData;
import fr.ailphaune.voxeltest.multiplayer.server.Server.ClientConnection;

@FunctionalInterface
public interface ServerPacketListener<T extends PacketData> {
	
	public void onPacket(T data, ClientConnection connection) throws IOException;
	
}