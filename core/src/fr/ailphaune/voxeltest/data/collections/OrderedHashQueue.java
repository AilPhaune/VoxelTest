package fr.ailphaune.voxeltest.data.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ArrayReflection;

import fr.ailphaune.voxeltest.utils.IteratorUtils;

public class OrderedHashQueue<T> implements Queue<T> {

	private static final class Value<T> {
		public T value;

		public Value(T v) {
			value = v;
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this || ((obj instanceof Value) && ((Value<?>) obj).value == value);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}
	
	private Value<Object> tempValue = new Value<Object>(null);

	private HashSet<Value<T>> set;
	private Array<Value<T>> array;

	public OrderedHashQueue() {
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
		tempValue.value = o;
		boolean ret = set.contains(tempValue);
		tempValue.value = null;
		return ret;
	}

	@Override
	public Iterator<T> iterator() {
		return IteratorUtils.map(array.iterator(), v -> v.value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] toArray() {
		return toArray((T[]) ArrayReflection.newInstance(array.items.getClass().getComponentType(), array.size));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E[] toArray(E[] a) {
		if(a.length < array.size) {
			a = (E[]) ArrayReflection.newInstance(a.getClass().getComponentType(), array.size);
		}
		return IteratorUtils.toArray(iterator(), a);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		tempValue.value = o;
		if (!set.contains(tempValue)) {
			tempValue.value = null;
			return false;
		}
		set.remove(tempValue);
		array.removeValue((Value<T>) tempValue, false);
		tempValue.value = null;
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			tempValue.value = o;
			if (!set.contains(tempValue))
				return false;
		}
		tempValue.value = null;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean ret = false;
		for (T t : c) {
			if (add(t))
				ret = true;
		}
		return ret;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean ret = false;
		for (Object t : c) {
			if (remove(t))
				ret = true;
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		
		for(Value<T> test : (HashSet<Value<T>>)set.clone()) {
			T v = test.value;
			if(!c.stream().anyMatch(k -> v == k)) {
				remove(v);
				modified = true;
			}
		}
		
		return modified;
	}

	@Override
	public void clear() {
		set.clear();
		array.clear();
	}

	
	@Override
	public boolean add(T e) {
		Value<T> v = new Value<>(e);
		if (set.contains(v)) {
			array.removeValue(v, false);
		}
		set.add(v);
		array.add(v);
		return true;
	}

	@Override
	public boolean offer(T e) {
		return add(e);
	}

	@Override
	public T remove() {
		if (isEmpty())
			throw new IndexOutOfBoundsException();
		Value<T> value = array.first();
		array.removeIndex(0);
		set.remove(value);
		return value.value;
	}

	@Override
	public T poll() {
		if (isEmpty())
			return null;
		Value<T> value = array.first();
		array.removeIndex(0);
		set.remove(value);
		return value.value;
	}

	@Override
	public T element() {
		if (isEmpty())
			throw new IndexOutOfBoundsException();
		return array.first().value;
	}

	@Override
	public T peek() {
		if (isEmpty())
			return null;
		return array.first().value;
	}
}