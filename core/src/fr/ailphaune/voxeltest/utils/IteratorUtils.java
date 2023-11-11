package fr.ailphaune.voxeltest.utils;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public class IteratorUtils {
	
	private static class MappedIterator<F, T> implements Iterator<T>, Iterable<T> {
		
		private Iterator<F> iterator;
		private Function<F, T> mapper;
		
		public MappedIterator(Iterator<F> iterator, Function<F, T> mapper) {
			this.iterator = Objects.requireNonNull(iterator, "Iterator is null");
			this.mapper = Objects.requireNonNull(mapper, "Mapper is null");
		}
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public T next() {
			return mapper.apply(iterator.next());
		}
		
		@Override
		public void remove() {
			iterator.remove();
		}

		@Override
		public Iterator<T> iterator() {
			return this;
		}
	}
	
	public static <F, T> Iterator<T> map(Iterator<F> source, Function<F, T> mapper) {
		return new MappedIterator<F, T>(source, mapper);
	}

	@SuppressWarnings("unchecked")
	public static <E, T> E[] toArray(Iterator<T> iterator, E[] a) {
		int i = 0;
		while(iterator.hasNext()) {
			if(i >= a.length) return a;
			a[i] = (E) iterator.next();
			i++;
		}
		return a;
	}
}