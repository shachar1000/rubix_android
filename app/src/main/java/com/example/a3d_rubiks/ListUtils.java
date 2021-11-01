package com.example.a3d_rubiks;

import java.util.Iterator;

public class ListUtils {
    public static class EnumeratedItem<T> {
        public T item;
        public int index;
        private EnumeratedItem(T item, int index) {
            this.item = item; this.index = index;
        }
    }
    private static class ListEnumerator<T> implements Iterable<EnumeratedItem<T>> {
        private Iterable<T> target;
        private int start;
        public ListEnumerator(Iterable<T> target, int start) {
            this.target = target; this.start = start;
        }
        @Override
        public Iterator<EnumeratedItem<T>> iterator() {
            final Iterator<T> targetIterator = target.iterator();
            return new Iterator<EnumeratedItem<T>>() {
                int index = start;
                @Override
                public boolean hasNext() {
                    return targetIterator.hasNext();
                }
                @Override
                public EnumeratedItem<T> next() {
                    EnumeratedItem<T> nextIndexedItem = new EnumeratedItem<T>(targetIterator.next(), index);
                    index++; return nextIndexedItem;
                }
            };
        }
    }
    public static <T> Iterable<EnumeratedItem<T>> enumerate(Iterable<T> iterable, int start) {
        return new ListEnumerator<T>(iterable, start);
    }
    public static <T> Iterable<EnumeratedItem<T>> enumerate(Iterable<T> iterable) {
        return enumerate(iterable, 0);
    }
}