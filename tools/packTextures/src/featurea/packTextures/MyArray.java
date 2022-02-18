package featurea.packTextures;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MyArray<T> implements Iterable<T> {

    public T[] items;
    public int size;
    private boolean ordered;
    private ArrayIterator<T> iterator;

    public MyArray() {
        this(true, 16);
    }

    public MyArray(boolean ordered, int capacity) {
        this.ordered = ordered;
        items = (T[]) new Object[capacity];
    }

    public MyArray(boolean ordered, int capacity, Class<T> arrayType) {
        this.ordered = ordered;
        items = (T[]) java.lang.reflect.Array.newInstance(arrayType, capacity);
    }

    public MyArray(MyArray array) {
        this(array.ordered, array.size, (Class<T>) array.items.getClass().getComponentType());
        size = array.size;
        System.arraycopy(array.items, 0, items, 0, size);
    }

    public void add(T value) {
        T[] items = this.items;
        if (size == items.length) items = resize(Math.max(8, (int) (size * 1.75f)));
        items[size++] = value;
    }

    public T get(int index) {
        if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
        return items[index];
    }

    public T removeIndex(int index) {
        if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
        T[] items = this.items;
        T value = (T) items[index];
        size--;
        if (ordered) {
            System.arraycopy(items, index + 1, items, index, size - index);
        } else {
            items[index] = items[size];
        }
        items[size] = null;
        return value;
    }

    public void clear() {
        T[] items = this.items;
        for (int i = 0, n = size; i < n; i++) {
            items[i] = null;
        }
        size = 0;
    }

    private T[] resize(int newSize) {
        T[] items = this.items;
        T[] newItems = (T[]) java.lang.reflect.Array.newInstance(items.getClass().getComponentType(), newSize);
        System.arraycopy(items, 0, newItems, 0, Math.min(items.length, newItems.length));
        this.items = newItems;
        return newItems;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        if (iterator == null) {
            iterator = new ArrayIterator<>(this);
        }
        iterator.index = 0;
        return iterator;
    }

    private static class ArrayIterator<T> implements Iterator<T> {

        private final MyArray<T> array;
        private int index;

        public ArrayIterator(MyArray<T> array) {
            this.array = array;
        }

        public boolean hasNext() {
            return index < array.size;
        }

        public T next() {
            if (index >= array.size) {
                throw new NoSuchElementException(String.valueOf(index));
            }
            return array.items[index++];
        }

        public void remove() {
            index--;
            array.removeIndex(index);
        }

    }


}
