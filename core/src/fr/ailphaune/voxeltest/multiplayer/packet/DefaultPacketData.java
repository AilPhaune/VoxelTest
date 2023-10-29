package fr.ailphaune.voxeltest.multiplayer.packet;

import java.io.Serializable;
import java.util.Objects;

import fr.ailphaune.voxeltest.multiplayer.packet.Packet.PacketData;

public class DefaultPacketData<T extends Serializable> implements PacketData {

	private Packet<DefaultPacketData<T>> packet;
	private T data;
	
	public DefaultPacketData(Packet<DefaultPacketData<T>> packet, T data) {
		Objects.requireNonNull(data, "null data");
		this.packet = packet;
		this.data = data;
	}
	
	@Override
	public Packet<DefaultPacketData<T>> getPacket() {
		return packet;
	}

	public T getData() {
		return data;
	}
}