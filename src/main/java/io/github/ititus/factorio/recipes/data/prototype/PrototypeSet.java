package io.github.ititus.factorio.recipes.data.prototype;

import io.github.ititus.commons.data.ImmutableIterator;

import java.util.*;

public final class PrototypeSet<T extends Prototype> implements SortedSet<T> {

    private final Class<T> type;
    private final Map<String, T> map;
    private final SortedSet<T> sorted;

    private boolean locked;

    public PrototypeSet(Class<T> type) {
        this.type = type;
        this.map = new HashMap<>();
        this.sorted = new TreeSet<>();

        this.locked = false;
    }

    public PrototypeSet(Class<T> type, Collection<? extends T> c) {
        this(type);
        addAll(c);
    }

    public PrototypeSet<T> lock() {
        locked = true;
        return this;
    }

    public boolean isLocked() {
        return locked;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public boolean add(Prototype p) {
        if (locked) {
            throw new IllegalStateException();
        } else if (!type.isInstance(p)) {
            throw new IllegalArgumentException();
        }

        T cast = type.cast(p);

        T before = map.get(p.getName());
        if (before != null) {
            throw new RuntimeException();
        }

        map.put(p.getName(), cast);
        sorted.add(cast);
        return true;
    }

    @Override
    public boolean contains(Object o) {
        if (!locked) {
            throw new IllegalStateException();
        }

        if (!(o instanceof Prototype)) {
            return false;
        }
        return map.containsKey(((Prototype) o).getName());
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    public T get(String name) {
        if (!locked) {
            throw new IllegalStateException();
        }

        return map.get(name);
    }

    public SortedSet<String> keySet() {
        if (!locked) {
            throw new IllegalStateException();
        }

        SortedSet<String> keySet = new TreeSet<>(Comparator.comparing(name -> map.get(name).getOrder()));
        keySet.addAll(map.keySet());
        return Collections.unmodifiableSortedSet(keySet);
    }

    public SortedSet<T> values() {
        if (!locked) {
            throw new IllegalStateException();
        }

        return Collections.unmodifiableSortedSet(sorted);
    }

    @Override
    public int size() {
        if (!locked) {
            throw new IllegalStateException();
        }

        return sorted.size();
    }

    @Override
    public boolean isEmpty() {
        if (!locked) {
            throw new IllegalStateException();
        }

        return sorted.isEmpty();
    }

    @Override
    public Object[] toArray() {
        if (!locked) {
            throw new IllegalStateException();
        }

        return sorted.toArray();
    }

    @Override
    public <E> E[] toArray(E[] a) {
        if (!locked) {
            throw new IllegalStateException();
        }

        return sorted.toArray(a);
    }

    @Override
    public Comparator<? super T> comparator() {
        return sorted.comparator();
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (!locked) {
            throw new IllegalStateException();
        }

        return Collections.unmodifiableSortedSet(sorted.subSet(fromElement, toElement));
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        if (!locked) {
            throw new IllegalStateException();
        }

        return Collections.unmodifiableSortedSet(sorted.headSet(toElement));
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        if (!locked) {
            throw new IllegalStateException();
        }

        return Collections.unmodifiableSortedSet(sorted.tailSet(fromElement));
    }

    @Override
    public T first() {
        if (!locked) {
            throw new IllegalStateException();
        }

        return sorted.first();
    }

    @Override
    public T last() {
        if (!locked) {
            throw new IllegalStateException();
        }

        return sorted.last();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        if (!locked) {
            throw new IllegalStateException();
        }
        return new ImmutableIterator<>(sorted.iterator());
    }

    @Override
    public Spliterator<T> spliterator() {
        if (!locked) {
            throw new IllegalStateException();
        }

        return Spliterators.spliterator(sorted,
                Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
    }

    @Override
    public boolean equals(Object o) {
        if (!locked) {
            throw new IllegalStateException();
        }

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrototypeSet<?> that = (PrototypeSet<?>) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        if (!locked) {
            throw new IllegalStateException();
        }

        return map.hashCode();
    }

    @Override
    public String toString() {
        if (!locked) {
            throw new IllegalStateException();
        }

        return sorted.toString();
    }
}
