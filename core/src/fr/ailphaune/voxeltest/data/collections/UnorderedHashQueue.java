package fr.ailphaune.voxeltest.data.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;

import com.badlogic.gdx.utils.Array;

public class UnorderedHashQueue<T> implements Queue<T> {

	private HashSet<T> set;
	private Array<T> array;
	
	public UnorderedHashQueue() {
		this.set = new HashSet<>();
		this.array = new Array<>(false, 8);
	}
	
	@Override
	public int size() {
		return this.array.size;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return array.iterator();
	}

	@Override
	public Object[] toArray() {
		return array.toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		return set.toArray(a);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if(!set.contains(o)) return false;
		set.remove(o);
		array.removeValue((T) o, false);
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean ret = false;
		for(T t : c) {
			if(add(t)) ret = true;
		}
		return ret;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean ret = false;
		for(Object t : c) {
			if(remove(t)) ret = true;
		}
		return ret;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if(!set.retainAll(c)) return false;
		for(int i = array.size-1; i >= 0; i--) {
			if(!c.contains(array.get(i))) array.removeIndex(i);
		}
		return true;
	}

	@Override
	public void clear() {
		set.clear();
		array.clear();
	}

	@Override
	public boolean add(T e) {
		if(set.contains(e)) return false;
		set.add(e);
		array.add(e);
		return true;
	}

	@Override
	public boolean offer(T e) {
		return add(e);
	}

	@Override
	public T remove() {
		if(isEmpty()) throw new IndexOutOfBoundsException();
		T value = array.first();
		array.removeIndex(0);
		set.remove(value);
		return value;
	}

	@Override
	public T poll() {
		if(isEmpty()) return null;
		T value = array.first();
		array.removeIndex(0);
		set.remove(value);
		return value;
	}

	@Override
	public T element() {
		if(isEmpty()) throw new IndexOutOfBoundsException();
		return array.first();
	}

	@Override
	public T peek() {
		if(isEmpty()) return null;
		return array.first();
	}
}