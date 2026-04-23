package com.josoft.collections;

import java.util.AbstractCollection;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * CircBuffer defines a so called circular buffer, that overwrites the oldest
 * element when a new element is added when the maximal size is reached.
 *
 * @param <T> The type of the elements
 * @author Joost de Bruin
 * @version 1.0
 */
public class CircBuffer<T> extends AbstractCollection<T> {
    private final int _capacity;
    private int _size = 0;
    private int _newest = -1;
    private int _oldest = 0;
    private final Object[] _data;
    private boolean _chkOffsets = true;

    /**
     * Creates an empty CircBuffer with the maximal number of elements specified
     * in the capacity argument
     *
     * @param capacity The maximal number of elements in this circular buffer
     */
    public CircBuffer(int capacity) {
        _capacity = capacity;
        _data = new Object[_capacity];
    }

    /**
     * Creates an empty CircBuffer with the maximal number of elements specified
     * in the capacity argument, and specifies whether methods manipulating elements
     * using offsets should check whether these offsets are valid (throws
     * ArrayIndexOutOfBoundsException if not).
     *
     * @param capacity            The maximal number of elements in this circular buffer
     * @param chkOffsetsValidness If true, throws ArrayIndexOutOfBoundsException if offset is
     *                            not valid.
     */
    public CircBuffer(int capacity, boolean chkOffsetsValidness) {
        _capacity = capacity;
        _data = new Object[_capacity];
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
        _newest++;
        if (_size < _capacity) {
            _size++;
        } else {
            _oldest = _newest + 1;
        }
        if (_newest == _capacity) {
            _newest = 0;
        }
        if (_oldest == _capacity) {
            _oldest = 0;
        }
        _data[_newest] = value;

        // Should return true if collection changes, collection always changes, hence always true
        return true;
    }

    /**
     * Sets the nth-to-newest element.
     *
     * @param offset The distance from the newest element
     * @param value  The value assigned to the element
     */
    public void setFromNewest(int offset, T value) {
        _data[offsetToIndexInData(offset, true, _chkOffsets)] = value;

    }

    /**
     * Sets the nth-to-oldest element.
     *
     * @param offset The distance from the oldest element
     * @param value  The value assigned to the element
     */
    public void setFromOldest(int offset, T value) {
        _data[offsetToIndexInData(offset, false, _chkOffsets)] = value;

    }

    /**
     * Gets the nth-to-newest element.
     *
     * @param offset The distance from the newest element
     * @return The value of the nth-to-newest element
     */
    @SuppressWarnings("unchecked")
    public T getFromNewest(int offset) {
        return (T) _data[offsetToIndexInData(offset, true, _chkOffsets)];
    }

    /**
     * Gets the nth-to-oldest element.
     *
     * @param offset The distance from the oldest element
     * @return The value of the nth-to-oldest element
     */
    @SuppressWarnings("unchecked")
    public T getFromOldest(int offset) {
        return (T) _data[offsetToIndexInData(offset, false, _chkOffsets)];
    }

    /**
     * Removes the nth-to-newest element. If performance is a concern, keep in
     * mind that removal requires shifting n elements.
     *
     * @param offset The distance from the newest element
     */
    public void removeFromNewest(int offset) {
        if (_chkOffsets && !isValidOffset(offset)) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // 'Side' to be shifted can be chosen (move up 'newest' index by one, or move down 'oldest'
        // index by one). Chose the side that requires the least amount of elements to be shifted.
        // Removal at one of both ends will not cause shifting at all.
        if (offset <= _size/2) {
            for (int i = offset; i >= 1; i--) {
                setFromNewest(i, getFromNewest(i - 1));
            }
            popNewest();
        } else {
            removeFromOldest(_size - offset - 1);
        }
    }

    /**
     * Removes the nth-to-oldest element. If performance is a concern, keep in
     * mind that removal requires shifting n elements.
     *
     * @param offset The distance from the oldest element
     */
    public void removeFromOldest(int offset) {
        if (_chkOffsets && !isValidOffset(offset)) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // 'Side' to be shifted can be chosen (move up 'newest' index by one, or move down 'oldest'
        // index by one). Chose the side that requires the least amount of elements to be shifted.
        // Removal at one of both ends will not cause shifting at all.
        if (offset < _size/2) {
            for (int i = offset; i >= 1; i--) {
                setFromOldest(i, getFromOldest(i - 1));
            }
            popOldest();
        } else {
            removeFromNewest(_size - offset - 1);
        }
    }

    /**
     * Inserts an element at the specified offset from the newest element. If
     * performance is a concern, keep in mind that insertion requires shifting
     * elements.
     *
     * @param offset The distance from the oldest element
     * @param value  The value to be inserted
     */
    public void insertFromNewest(int offset, T value) {
        if (_chkOffsets && !isValidOffset(offset)) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // 'Side' to be shifted can be chosen (move up 'newest' index by one, or move down 'oldest'
        // index by one). Chose the side that requires the least amount of elements to be shifted.
        // Inserting at one of both ends will not cause shifting at all.
        if (offset <= _size/2) {
            // 'Duplicate' the newest element, oldest element will automatically be overwritten.
            add(getFromNewest(0));
            for (int i = _size - 2; i >= _size - offset; i--) {
                int iToChange = offsetToIndexInData(i, false, true);
                int iToChangeTo = offsetToIndexInData(i - 1, false, false);
                _data[iToChange] = _data[iToChangeTo];
            }
            _data[offsetToIndexInData(_size - offset - 1, false, false)] = value;
        } else {
            insertFromOldest(_size - offset - 1, value);
        }
    }

    /**
     * Inserts an element at the specified offset from the oldest element. If
     * performance is a concern, keep in mind that insertion requires shifting
     * elements.
     *
     * @param offset The distance from the oldest element
     * @param value  The value to be inserted
     */
    public void insertFromOldest(int offset, T value) {
        if (_chkOffsets && !isValidOffset(offset)) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // 'Side' to be shifted can be chosen (move up 'newest' index by one, or move down 'oldest'
        // index by one). Chose the side that requires the least amount of elements to be shifted.
        // Inserting at one of both ends will not cause shifting at all.
        if (offset < _size/2) {
            // If capacity has not been reached yet, move the oldest element down by one, and the
            // pointer to oldest, and increase size (effectively 'duplicating' the oldest element,
            // or 'adding' at the oldest side of the buffer).
            if (_size < _capacity) {
                int prevOldest = _oldest;
                _oldest--;
                if (_oldest < 0) {
                    _oldest = _capacity - 1;
                }
                _data[_oldest] = _data[prevOldest];
                _size++;
            }

            for (int i = 0; i < offset; i++) {
                int iToChange = offsetToIndexInData(i, false, true);
                int iToChangeTo = offsetToIndexInData(i + 1, false, false);
                _data[iToChange] = _data[iToChangeTo];
            }
            _data[offsetToIndexInData(offset, false, false)] = value;
        } else {
            insertFromNewest(_size - offset - 1, value);
        }
    }


    /**
     * Returns and removes the newest element.
     *
     * @return The newest element
     */
    @SuppressWarnings("unchecked")
    public T popNewest() {
        if (_size == 0) {
            throw new NegativeArraySizeException();
        }
        T toReturn = (T) _data[_newest];

        _newest--;
        if (_newest < 0) {
            _newest = _capacity - 1;
        }
        _size--;
        if (_size == 0) {
            clear();
        }

        return toReturn;
    }

    /**
     * Returns and removes the oldest element.
     *
     * @return The oldest element
     */
    @SuppressWarnings("unchecked")
    public T popOldest() {
        T toReturn = (T) _data[_oldest];

        if (_size > 0) {
            _oldest++;
        } else {
            throw new NegativeArraySizeException();
        }
        if (_oldest > _capacity - 1) {
            _oldest = 0;
        }
        _size--;
        if (_size == 0) {
            clear();
        }

        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
        _newest = -1;
        _oldest = 0;
        _size = 0;
    }

    /**
     * Checks if offset is within limits of this buffer
     *
     * @param offset The offset
     * @return True if within limits, false if not
     */
    public boolean isValidOffset(int offset) {
        return offset >= 0 && offset < size();
    }

    /**
     * Translates offset to an index in the internal array containing the
     * actual elements.
     *
     * @param offset     Offset to translate
     * @param fromNewest True if offset is from the newest element, false if from oldest
     * @return Index in internal array _data
     */
    private int offsetToIndexInData(int offset, boolean fromNewest, boolean chkOffsets) {
        if (chkOffsets && !isValidOffset(offset)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int iInData;
        if (fromNewest) {
            iInData = _newest - offset;
            while (iInData < 0) {
                iInData += _capacity;
            }
        } else {
            iInData = _oldest + offset;
            while (iInData > _capacity - 1) {
                iInData -= _capacity;
            }
        }

        return iInData;
    }

    /**
     * Returns the number of elements currently in this circular buffer.
     *
     * @return The number of elements in this buffer
     * @see AbstractCollection#size()
     */
     @Override
    public int size() {
        return _size;
    }

    /**
     * Get the maximal number of elements for this buffer
     *
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
    public ListIterator<T> iterator() {
        return new CircBufferIterator(this);
    }

    /**
     * Returns an iterator over the elements in this buffer in proper sequence,
     * starting from the index supplied (counting from the oldest element).
     *
     * @param index Index at where to start iterating
     *
     * @return A ListIterator of the elements in this buffer
     * @see AbstractCollection#iterator()
     */
    public ListIterator<T> iterator(int index) {
        return new CircBufferIterator(this, index);
    }

    /**
     * Basic iterator for CircBuffer collection
     *
     * @author Joost
     * @version 1.0
     *
     */
    class CircBufferIterator implements ListIterator<T> {
        private final CircBuffer<T> _buffer;
        private int _iPresent = -1;

        /**
         * Initializes iterator, specifying CircBuffer to iterate over.
         *
         */
        public CircBufferIterator(CircBuffer<T> buffer) {
            _buffer = buffer;
        }

        /**
         * Constructs a new iterator for the specified circular buffer, starting at the given index
         */
        public CircBufferIterator(CircBuffer<T> buffer, int index) {
            _buffer = buffer;
            _iPresent = index;
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
                throw new NoSuchElementException();
            } else {
                return _buffer.getFromOldest(_iPresent);
            }
        }

        @Override
        public boolean hasPrevious() {
            return _buffer.isValidOffset(_iPresent);
        }

        @Override
        public T previous() {
            _iPresent--;

            if (_buffer._chkOffsets && !_buffer.isValidOffset(_iPresent + 1)) {
                throw new NoSuchElementException();
            } else {
                return _buffer.getFromOldest(_iPresent + 1);
            }
        }

        @Override
        public int nextIndex() {
            return _iPresent + 1;
        }

        @Override
        public int previousIndex() {
            return _iPresent;
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

        @Override
        public void set(T t) {
            _buffer.setFromOldest(_iPresent, t);
        }

        @Override
        public void add(T t) {
            _buffer.insertFromOldest(_iPresent, t);
            _iPresent++;
        }

        public T peek(int offset) {
            if (_buffer.isValidOffset(_iPresent + offset)) {
                return _buffer.getFromOldest(_iPresent + offset);
            } else {
                return null;
            }
        }
    }

}