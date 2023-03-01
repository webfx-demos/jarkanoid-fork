package eu.hansolo.fx.jarkanoid;

import java.util.ArrayDeque;
import java.util.List;
import java.util.stream.Collectors;


public class FIFO<T> extends ArrayDeque<T> {
    private final int capacity;


    // ******************** Constructors **************************************
    public FIFO(final int capacity) {
        super(capacity);
        this.capacity = capacity;
    }


    // ******************** Methods *******************************************
    @Override public boolean add(final T item) {
        if (this.size() == this.capacity) { remove(); }
        return super.add(item);
    }

    public List<T> getItems() { return stream().collect(Collectors.toList()); }

    public T getItemAt(final int index) {
        if (index < 0 || index > size() - 1) { throw new IllegalArgumentException("Index out of bounds (0-" + (capacity - 1) + ")"); }
        return stream().collect(Collectors.toList()).get(index);
    }

    @Override public String toString() {
        final StringBuilder msgBuilder = new StringBuilder(capacity);
        getItems().stream().forEach(item -> msgBuilder.append(item.toString()).append(","));
        msgBuilder.setLength(msgBuilder.length() - 1);
        return msgBuilder.toString();
    }
}
