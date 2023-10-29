package fr.ailphaune.voxeltest.data.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SynchronizedHashSet<T> extends HashSet<T> {

	private static final long serialVersionUID = -6516545369218152036L;

	protected Object mutex = new Object();
	
	public SynchronizedHashSet() {
		super();
    }

    public SynchronizedHashSet(Collection<? extends T> c) {
        super(c);
    }

    public SynchronizedHashSet(int initialCapacity, float loadFactor) {
        super();
    }

    public SynchronizedHashSet(int initialCapacity) {
        super(initialCapacity);
    }
    
    @Override
    public boolean addAll(Collection<? extends T> c) {
    	synchronized(mutex) { return super.addAll(c); }
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
    	synchronized(mutex) { return super.containsAll(c); }
    }
    
    @Override
    public boolean equals(Object o) {
    	synchronized(mutex) { return super.equals(o); }
    }
    
    @Override
    public int hashCode() {
    	synchronized(mutex) { return super.hashCode(); }
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
    	synchronized(mutex) { return super.removeAll(c); }
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
    	synchronized(mutex) { return super.retainAll(c); }
    }
    
    @Override
    public boolean add(T e) {
    	synchronized(mutex) { return super.add(e); }
    }
    
    @Override
    public void clear() {
    	synchronized(mutex) { super.clear(); }
    }
    
    @Override
    public Object clone() {
    	synchronized(mutex) { return super.clone(); }
    }
    
    @Override
    public boolean contains(Object o) {
    	synchronized(mutex) { return super.contains(o); }
    }
    
    @Override
    public void forEach(Consumer<? super T> action) {
    	super.forEach(action);
    }
    
    @Override
    public boolean isEmpty() {
    	synchronized(mutex) { return super.isEmpty(); }
    }
    
    @Override
    public Iterator<T> iterator() {
    	return super.iterator();
    }
    
    @Override
    public Stream<T> parallelStream() {
    	return super.parallelStream();
    }
    
    @Override
    public boolean remove(Object o) {
    	synchronized(mutex) { return super.remove(o); }
    }
    
    @Override
    public boolean removeIf(Predicate<? super T> filter) {
    	return super.removeIf(filter);
    }
    
    @Override
    public int size() {
    	synchronized(mutex) { return super.size(); }
    }
    
    @Override
    public Spliterator<T> spliterator() {
    	return super.spliterator();
    }
    
    @Override
    public Stream<T> stream() {
    	return super.stream();
    }
    
    @Override
    public Object[] toArray() {
    	synchronized(mutex) { return super.toArray(); }
    }
    
    @Override
    public <E> E[] toArray(IntFunction<E[]> generator) {
    	synchronized(mutex) { return super.toArray(generator); }
    }
    
    public <E> E[] toArray(E[] a) {
    	synchronized(mutex) { return super.toArray(a); }
    }
    
    @Override
    public String toString() {
    	synchronized(mutex) { return super.toString(); }
    }
}