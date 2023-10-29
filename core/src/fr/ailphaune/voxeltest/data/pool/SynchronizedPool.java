package fr.ailphaune.voxeltest.data.pool;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

/**
 * The exact same code as {@link Pool} but synchronized for concurrent access
 */
public abstract class SynchronizedPool<T> extends Pool<T> implements Disposable {

	public SynchronizedPool() {
		this(16, Integer.MAX_VALUE);
	}
	
	public SynchronizedPool(int initialCapacity) {
		this(initialCapacity, Integer.MAX_VALUE);
	}
	
	public SynchronizedPool(int initialCapacity, int maxCapacity) {
		super(0, 0);
		freeObjects = new Array<>(false, initialCapacity);
		this.max = maxCapacity;
	}

	protected Array<T> freeObjects;
	public final int max;
	
	protected abstract T newObject();

	public synchronized T obtain() {
		synchronized(freeObjects) {
			return freeObjects.size == 0 ? newObject() : freeObjects.pop();
		}
	}

	public synchronized void free(T object) {
		if (object == null) throw new IllegalArgumentException("object cannot be null.");
		synchronized(freeObjects) {
			if (freeObjects.size < max) {
				freeObjects.add(object);
				peak = Math.max(peak, freeObjects.size);
				reset(object);
			} else
				discard(object);
		}
	}

	public synchronized void fill(int size) {
		synchronized(freeObjects) {
			for (int i = 0; i < size; i++)
				if (freeObjects.size < max) freeObjects.add(newObject());
			peak = Math.max(peak, freeObjects.size);
		}
	}

	protected synchronized void reset(T object) {
		synchronized(object) {
			if (object instanceof Poolable) ((Poolable)object).reset();
		}
	}

	protected synchronized void discard(T object) {
		reset(object);
	}

	public synchronized void freeAll(Array<T> objects) {
		synchronized(this.freeObjects) {
			if (objects == null) throw new IllegalArgumentException("objects cannot be null.");
			Array<T> freeObjects = this.freeObjects;
			int max = this.max;
			for (int i = 0, n = objects.size; i < n; i++) {
				T object = objects.get(i);
				if (object == null) continue;
				if (freeObjects.size < max) {
					freeObjects.add(object);
					reset(object);
				} else {
					discard(object);
				}
			}
			peak = Math.max(peak, freeObjects.size);
		}
	}

	public synchronized void clear() {
		synchronized(this.freeObjects) {	
			Array<T> freeObjects = this.freeObjects;
			for (int i = 0, n = freeObjects.size; i < n; i++)
				discard(freeObjects.get(i));
			freeObjects.clear();
		}
	}

	public synchronized int getFree() {
		synchronized(freeObjects) {
			return freeObjects.size;
		}
	}
	
	@Override
	public synchronized void dispose() {
		clear();
	}
}