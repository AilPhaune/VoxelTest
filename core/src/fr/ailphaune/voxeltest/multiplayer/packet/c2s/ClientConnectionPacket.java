package fr.ailphaune.voxeltest.multiplayer.packet.c2s;

import java.io.IOException;

import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.multiplayer.packet.Packets;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataInputStream;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataOutputStream;

public class ClientConnectionPacket extends Packet<ClientConnectionPacket.Data> {

	public ClientConnectionPacket() {
		super("ClientConnectionPacket");
	}

	@Override
	public Data readData(ExtendedDataInputStream in) throws IOException {
		Data data = new Data();
		data.userName = in.readUTF();
		in.close();
		return data;
	}

	@Override
	public boolean writeUncheckedData(PacketData data, ExtendedDataOutputStream out) throws IOException {
		if(data == null || !(data instanceof Data)) return false;
		Data d = (Data) data;
		if(d.userName == null) return false;
		out.writeUTF(d.userName);
		return true;
	}

	public static class Data implements Packet.PacketData {

		public String userName;
		
		@Override
		public ClientConnectionPacket getPacket() {
			return Packets.CLIENT_CONNECTION;
		}
	}
}