package ru.ifmo.ctddev.qurbonzoda.arrayset;

import java.util.*;

/**
 * Created by qurbonzoda on 19.02.16.
 */
public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private List<E> elements;
    private Comparator<? super E> comparator;

    private ArraySet(List<E> arraySet, Comparator<? super E> comparator) {
        elements = arraySet;
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends E> collection) {
        Set<E> set = new TreeSet<>(collection);
        elements = Collections.unmodifiableList(new ArrayList<>(set));
        comparator = (o1, o2) -> ((Comparable<E>) o1).compareTo(o2);
    }

    public ArraySet(SortedSet<? extends E> sortedSet) {
        comparator = (Comparator<? super E>) sortedSet.comparator();
        elements = Collections.unmodifiableList(new ArrayList<>(sortedSet));
    }


    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        int fromIndex = Utils.lowerBound(elements, fromElement, comparator);
        int toIndex = Utils.lowerBound(elements, toElement, comparator);
        return new ArraySet(elements.subList(fromIndex, Math.max(toIndex, fromIndex)), comparator);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int toIndex = Utils.lowerBound(elements, toElement, comparator);
        return new ArraySet(elements.subList(0, toIndex), comparator);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int fromIndex = Utils.lowerBound(elements, fromElement, comparator);
        return new ArraySet(elements.subList(fromIndex, elements.size()), comparator);
    }

    @Override
    public E first() {
        return elements.get(0);
    }

    @Override
    public E last() {
        return elements.get(elements.size() - 1);
    }

/*
    private class ArraySetIterator implements Iterator<E> {
        private final Iterator<E> iterator;
        ArraySetIterator() {
            iterator = elements.iterator();
        }
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }
        @Override
        public E next() {
            return iterator.next();
        }
    }
 */
}
