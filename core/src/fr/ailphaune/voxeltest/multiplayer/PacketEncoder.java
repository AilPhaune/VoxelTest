package fr.ailphaune.voxeltest.multiplayer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPOutputStream;

import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.multiplayer.packet.Packet.PacketData;
import fr.ailphaune.voxeltest.utils.io.BetterByteArrayOutputStream;
import fr.ailphaune.voxeltest.utils.io.DataIO;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataOutputStream;

public class PacketEncoder {
	
	public static boolean encode(PacketData data, BetterByteArrayOutputStream out) throws IOException {
		Packet<?> packet = data.getPacket();

		int pos = out.size();
		
		out.write(DataIO.writeInt(0));
		
		GZIPOutputStream gzos = new GZIPOutputStream(out);
		ExtendedDataOutputStream edos = new ExtendedDataOutputStream(gzos);

		// Write the packet type as the first 4 bytes
		edos.writeInt(packet.getIndex());
		boolean success = packet.writeUncheckedData(data, edos);
		gzos.finish();
		
		ByteBuffer.wrap(out.getBuffer(), pos, 4).putInt(out.size() - pos - 4);
		
		return success;
	}
	
	public static boolean encode(PacketData data, OutputStream out) throws IOException {
		Packet<?> packet = data.getPacket();

		BetterByteArrayOutputStream bbaos = new BetterByteArrayOutputStream(512);
		GZIPOutputStream gzos = new GZIPOutputStream(bbaos);
		ExtendedDataOutputStream edos = new ExtendedDataOutputStream(gzos);

		// Write the packet type as the first 4 bytes
		edos.writeInt(packet.getIndex());
		boolean success = packet.writeUncheckedData(data, edos);
		gzos.finish();
		
		out.write(DataIO.writeInt(bbaos.size()));
		bbaos.writeTo(out);
		
		return success;
	}
}