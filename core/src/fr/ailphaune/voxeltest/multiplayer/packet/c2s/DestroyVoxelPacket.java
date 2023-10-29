package fr.ailphaune.voxeltest.multiplayer.packet.c2s;

import java.io.IOException;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.multiplayer.packet.Packets;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataInputStream;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataOutputStream;

public class DestroyVoxelPacket extends Packet<DestroyVoxelPacket.Data> {
	
	public DestroyVoxelPacket() {
		super("DestroyVoxelPacket");
	}

	@Override
	public Data readData(ExtendedDataInputStream stream) throws IOException {
		Data data = new Data();
		stream.readVoxelPos(data.position);
		return data;
	}

	@Override
	public boolean writeUncheckedData(PacketData data, ExtendedDataOutputStream stream) throws IOException {
		if(data == null || !(data instanceof Data)) return false;
		Data d = (Data) data;
		if(d.position == null) return false;
		stream.writeVoxelPos(d.position);
		return true;
	}

	public static class Data implements Packet.PacketData {
		
		public final VoxelPos position = new VoxelPos();
		
		@Override
		public DestroyVoxelPacket getPacket() {
			return Packets.DESTROY_VOXEL;
		}
	}
}