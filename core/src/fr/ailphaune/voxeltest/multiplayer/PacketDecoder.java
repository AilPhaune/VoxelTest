package fr.ailphaune.voxeltest.multiplayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import fr.ailphaune.voxeltest.data.Registries;
import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.multiplayer.packet.Packet.PacketData;
import fr.ailphaune.voxeltest.utils.io.ByteBufferInputStream;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataInputStream;
import fr.ailphaune.voxeltest.utils.io.LimitedInputStream;

public class PacketDecoder {
	
    public static PacketData decode(ByteBuffer in) throws InvalidPacketException {
        // Make sure there are at least four bytes available to read (packet length)
    	if (in.remaining() < 4) {
    		return null;
        }
        
        int rIdx = in.position();
        
        // Read the packet length
        int packetLength = in.getInt();
        if(packetLength < 0) {
        	throw new IllegalArgumentException("Invalid packet length: " + packetLength);
        }
        
        if(in.remaining() < packetLength) {
        	in.position(rIdx);
        	return null;
        }

        try {
	        LimitedInputStream limited = new LimitedInputStream(new ByteBufferInputStream(in), packetLength);
	        GZIPInputStream gzis = new GZIPInputStream(limited);
	        ExtendedDataInputStream dis = new ExtendedDataInputStream(gzis);
	        
	        // Read the packet type
	        int packetType = dis.readInt();
	
	        Packet<?> packet = Registries.PACKETS.get(packetType);
	        if(packet == null) {
	        	in.position(rIdx);
		        limited.consume();
	        	dis.close();
	        	throw new InvalidPacketException("Invalid packet type: " + packetType);
	        }
        
	        PacketData data = packet.readData(dis);
	        if(data == null) {
	        	in.position(rIdx);
		        limited.consume();
	        	dis.close();
	        	return null;
	        }
	        
	        limited.consume();
	        
	        return data;
        } catch(InvalidPacketException e) {
        	in.position(rIdx);
        	throw e;
        } catch(IOException e) {
        	in.position(rIdx);
        	throw new InvalidPacketException(e);
        }
    }
}
