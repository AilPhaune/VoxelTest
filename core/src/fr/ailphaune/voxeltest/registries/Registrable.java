package fr.ailphaune.voxeltest.registries;

public interface Registrable<T extends Registrable<T>> {

	public void onRegister(Registry<T> registry, Identifier id, int index);
	public void onUnregister();
	public boolean isRegistered();
	public Identifier getIdentifier();
	public Registry<T> getRegistry();
	public int getIndex();
	
}