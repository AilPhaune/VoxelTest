package fr.ailphaune.voxeltest.multiplayer;

import java.io.IOException;

import fr.ailphaune.voxeltest.multiplayer.packet.Packet.PacketData;

@FunctionalInterface
public interface ClientPacketListener<T extends PacketData> {
	
	public void onPacket(T data, Client client) throws IOException;
	
}