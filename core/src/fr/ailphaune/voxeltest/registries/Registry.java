package fr.ailphaune.voxeltest.registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.Disposable;

public class Registry<T extends Registrable<T>> implements Disposable {
	
	private HashMap<Identifier, T> registry;
	private List<T> indexMap;
	
	public Registry() {
		registry = new HashMap<>();
		indexMap = new ArrayList<>();
	}
	
	public synchronized int register(Identifier id, T value) {
		if(registry == null) throw new IllegalStateException("Registry is disposed");
		int index = indexMap.size();
		registry.put(id, value);
		indexMap.add(value);
		value.onRegister(this, id, index);
		return value.getIndex();
	}

	public synchronized T get(Identifier id) {
		if(registry == null) throw new IllegalStateException("Registry is disposed");
		return registry.getOrDefault(id, null);
	}
	
	public synchronized T get(int index) {
		if(indexMap == null) throw new IllegalStateException("Registry is disposed");
		if(index >= indexMap.size() || index < 0) {
			return null;
		}
		return indexMap.get(index);
	}
	
	public synchronized T get(Identifier id, T def) {
		if(registry == null) throw new IllegalStateException("Registry is disposed");
		return registry.getOrDefault(id, def);
	}
	
	@Override
	public synchronized void dispose() {
		for(Entry<Identifier, T> pair : registry.entrySet()) {
			pair.getValue().onUnregister();
		}
		registry.clear();
		registry = null;
		indexMap.clear();
		indexMap = null;
	}

	public int size() {
		return indexMap.size();
	}
}