package fr.ailphaune.voxeltest.multiplayer.packet;

import java.io.IOException;

import fr.ailphaune.voxeltest.registries.Identifier;
import fr.ailphaune.voxeltest.registries.Registrable;
import fr.ailphaune.voxeltest.registries.Registry;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataInputStream;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataOutputStream;

public abstract class Packet<Data extends Packet.PacketData> implements Registrable<Packet<?>> {

	public final String name;
	
	public Packet(String name) {
		this.name = name;
	}
	
	protected Registry<Packet<?>> registry;
	protected Identifier id;
	protected int index;
	
	@Override
	public void onRegister(Registry<Packet<?>> registry, Identifier id, int index) {
		this.registry = registry;
		this.id = id;
		this.index = index;
	}

	@Override
	public void onUnregister() {
		this.registry = null;
		this.id = null;
	}

	@Override
	public boolean isRegistered() {
		return this.registry != null;
	}

	@Override
	public Identifier getIdentifier() {
		return this.id;
	}

	@Override
	public Registry<Packet<?>> getRegistry() {
		return this.registry;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	public abstract Data readData(ExtendedDataInputStream stream) throws IOException;
	public abstract boolean writeUncheckedData(PacketData data, ExtendedDataOutputStream stream) throws IOException;
	
	public boolean writeData(Data data, ExtendedDataOutputStream stream) throws IOException {
		return writeUncheckedData(data, stream);
	}
	
	public interface PacketData {
		public Packet<?> getPacket();
	}
}