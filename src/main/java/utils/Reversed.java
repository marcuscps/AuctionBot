package utils;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Reversed<T> implements Iterable<T> {
    private final List<T> original;

    public Reversed(List<T> original) {
        this.original = original;
    }

    public Iterator<T> iterator() {
        final ListIterator<T> it = original.listIterator(original.size());

        return new Iterator<T>() {
            public boolean hasNext() { return it.hasPrevious(); }
            public T next() { return it.previous(); }
            public void remove() { it.remove(); }
        };
    }
}
