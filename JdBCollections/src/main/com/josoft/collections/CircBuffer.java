package com.josoft.collections;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * CircBuffer defines a so called circular buffer, that overwrites the oldest
 * element when a new element is added when the maximal size is reached.
 * 
 * @author Joost de Bruin
 * @version 1.0
 * @param <T>
 *            The type of the elements
 */
public class CircBuffer<T> extends AbstractCollection<T> {
	private int _capacity;
	private int _size = 0;
	private int _start = -1;
	private int _end = 0;
	private ArrayList<T> _data;
	private boolean _chkOffsets = true;

	/**
	 * Creates an empty CircBuffer with the maximal number of elements specified
	 * in the size argument
	 * 
	 * @param size
	 *            The maximal number of elements in this circular buffer
	 */
	public CircBuffer(int size) {
		_capacity = size;
		_data = new ArrayList<T>(_capacity);
		for (int iEl = 0; iEl < _capacity; iEl++) {
			_data.add(null);
		}
	}

	/**
	 * Creates an empty CircBuffer with the maximal number of elements specified
	 * in the size argument, and specifies whether methods manipulating elements
	 * using offsets should check whether these offsets are valid (throws
	 * ArrayIndexOutOfBoundsException if not).
	 * 
	 * @param size
	 *            The maximal number of elements in this circular buffer
	 * @param chkOffsetsValidness
	 *            If true, throws ArrayIndexOutOfBoundsException if offset is
	 *            not valid.
	 */
	public CircBuffer(int size, boolean chkOffsetsValidness) {
		_capacity = size;
		_data = new ArrayList<T>(_capacity);
		for (int iEl = 0; iEl < _capacity; iEl++) {
			_data.add(null);
		}
		_chkOffsets = chkOffsetsValidness;
	}

	/**
	 * Adds element to buffer, overwriting the oldest element if the buffer is
	 * full.
	 * 
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	@Override
	public boolean add(T value) {
		_start++;
		if (_size < _capacity) {
			_size++;
		} else {
			_end = _start;
		}
		if (_start == _capacity) {
			_start = 0;
		}
		if (_end == _capacity) {
			_end = 0;
		}
		_data.set(_start, value);

		return true;
	}

	/**
	 * Sets the nth-to-newest element.
	 * 
	 * @param offset
	 *            The distance from the newest element
	 * @param value
	 *            The value assigned to the element
	 * @return This circular buffer, for method chaining
	 */
	public CircBuffer<T> setFromNewest(int offset, T value) {
		_data.set(offsetToIndexInData(offset, true), value);

		return this;
	}

	/**
	 * Sets the nth-to-oldest element.
	 * 
	 * @param offset
	 *            The distance from the oldest element
	 * @param value
	 *            The value assigned to the element
	 * @return This circular buffer, for method chaining
	 */
	public CircBuffer<T> setFromOldest(int offset, T value) {
		_data.set(offsetToIndexInData(offset, false), value);

		return this;
	}

	/**
	 * Gets the nth-to-newest element.
	 * 
	 * @param offset
	 *            The distance from the newest element
	 * @return The value of the nth-to-newest element
	 */
	public T getFromNewest(int offset) {
		return _data.get(offsetToIndexInData(offset, true));
	}

	/**
	 * Gets the nth-to-oldest element.
	 * 
	 * @param offset
	 *            The distance from the oldest element
	 * @return The value of the nth-to-oldest element
	 */
	public T getFromOldest(int offset) {
		return _data.get(offsetToIndexInData(offset, false));
	}

	/**
	 * Removes the nth-to-newest element. If performance is a concern, keep in
	 * mind that removal requires shifting n elements.
	 * 
	 * @param offset
	 *            The distance from the newest element
	 * @return This buffer for method chaining
	 */
	public CircBuffer<T> removeFromNewest(int offset) {
		if (_chkOffsets && !isValidOffset(offset)) {
			throw new ArrayIndexOutOfBoundsException();
		}
		for (int i = offset; i >= 1; i--) {
			setFromNewest(i, getFromNewest(i - 1));
		}
		popNewest();

		return this;
	}

	/**
	 * Removes the nth-to-oldest element. If performance is a concern, keep in
	 * mind that removal requires shifting n elements.
	 * 
	 * @param offset
	 *            The distance from the oldest element
	 * @return This buffer for method chaining
	 */
	public CircBuffer<T> removeFromOldest(int offset) {
		if (_chkOffsets && !isValidOffset(offset)) {
			throw new ArrayIndexOutOfBoundsException();
		}
		for (int i = offset; i >= 1; i--) {
			setFromOldest(i, getFromOldest(i - 1));
		}
		popOldest();

		return this;
	}

	/**
	 * Returns and removes the newest element.
	 * 
	 * @return The newest element
	 */
	public T popNewest() {
		if (_size == 0) {
			throw new NegativeArraySizeException();
		}
		T toReturn = _data.get(_start);

		_start--;
		if (_start < 0) {
			_start = _capacity - 1;
		}
		_size--;

		return toReturn;
	}

	/**
	 * Returns and removes the oldest element.
	 * 
	 * @return The oldest element
	 */
	public T popOldest() {
		T toReturn = _data.get(_end);

		if (_size > 0) {
			_end++;
		} else {
			throw new NegativeArraySizeException();
		}
		if (_end > _capacity - 1) {
			_end = 0;
		}
		_size--;

		return toReturn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	@Override
	public void clear() {
		_start = -1; _end = 0;
		_size = 0;
	}

	/**
	 * Checks if offset is within limits of this buffer
	 * 
	 * @param offset
	 *            The offset
	 * @return True if within limits, false if not
	 */
	public boolean isValidOffset(int offset) {
		return offset >= 0 && offset < size();
	}

	/**
	 * Translates offset to an index in the internal ArrayList<T> containing the
	 * actual elements.
	 * 
	 * @param offset
	 *            Offset to translate
	 * @param fromNewest
	 *            True if offset is from newest element, false if from oldest
	 * @return Index in internal ArrayList<T> _data
	 */
	private int offsetToIndexInData(int offset, boolean fromNewest) {
		int iInData = 0;
		if (_chkOffsets && !isValidOffset(offset)) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (fromNewest) {
			iInData = _start - offset;
			while (iInData < 0) {
				iInData += _capacity;
			}
		} else {
			iInData = _end + offset;
			while (iInData > _capacity - 1) {
				iInData -= _capacity;
			}
		}

		return iInData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return _size;
	}

	/**
	 * @return The maximal size of this buffer
	 */
	public int capacity() {
		return _capacity;
	}

	public boolean isEmpty() {
		return _size == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new CircBufferIterator(this);
	}

	/**
	 * Basic iterator for CircBuffer collection
	 * 
	 * @author Joost
	 * @version 1.0
	 * 
	 */
	class CircBufferIterator implements Iterator<T> {
		private CircBuffer<T> _buffer;
		private int _iPresent = -1;

		/**
		 * Initializes iterator, specifying CircBuffer to iterate over.
		 * 
		 * @param buffer
		 */
		public CircBufferIterator(CircBuffer<T> buffer) {
			_buffer = buffer;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return _buffer.isValidOffset(_iPresent + 1);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next() {
			_iPresent++;

			if (_buffer._chkOffsets && !_buffer.isValidOffset(_iPresent)) {
				return null;
			} else {
				return _buffer.getFromOldest(_iPresent);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			_buffer.removeFromOldest(_iPresent);
		}
	}

}