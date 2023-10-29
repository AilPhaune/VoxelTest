package fr.ailphaune.voxeltest.multiplayer.server.events;

import fr.ailphaune.voxeltest.events.BaseEvent;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.ClientConnectionPacket;
import fr.ailphaune.voxeltest.multiplayer.server.Server.ClientConnection;

public class ClientPreConnectEvent extends BaseEvent {

	public final ClientConnection connection;
	public final ClientConnectionPacket.Data packet;
	
	public ClientPreConnectEvent(ClientConnection connection, ClientConnectionPacket.Data packet) {
		super(true);
		this.connection = connection;
		this.packet = packet;
	}
}