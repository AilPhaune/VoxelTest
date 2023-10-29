package fr.ailphaune.voxeltest.registries;

import java.util.Objects;

public class Identifier {
	
	public final String provider, type, name;
	
	private String generatedStr;
	
	public Identifier(String provider, String type, String name) {
		Objects.requireNonNull(provider, "Provider is null");
		Objects.requireNonNull(type, "Type is null");
		Objects.requireNonNull(name, "Name is null");
		if(provider.contains(":") || provider.contains("/")) throw new IllegalArgumentException();
		if(type.contains(":") || type.contains("/")) throw new IllegalArgumentException();
		if(name.contains(":") || name.contains("/")) throw new IllegalArgumentException();
		this.provider = provider;
		this.type = type;
		this.name = name;
		this.generatedStr = provider + ":" + type + "/" + name;
	}
	
	@Override
	public String toString() {
		return generatedStr;
	}
	
	@Override
	public int hashCode() {
		return generatedStr.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(obj == this) return true;
		if(!obj.getClass().equals(getClass())) return false;
		Identifier o = (Identifier) obj;
		return o.generatedStr.equals(generatedStr);
	}
}