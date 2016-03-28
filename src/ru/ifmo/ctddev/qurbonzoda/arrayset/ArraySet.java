package ru.ifmo.ctddev.qurbonzoda.arrayset;

import com.sun.istack.internal.NotNull;

import java.util.*;

/**
 * Created by qurbonzoda on 19.02.16.
 */
public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private List<E> elements;
    private Comparator<? super E> comparator;
    private boolean naturalOrdering = false;

    public ArraySet() {
        elements = Collections.unmodifiableList(new ArrayList<>());
        naturalOrdering = true;
        comparator = (o1, o2) -> ((Comparable<? super E>) o1).compareTo(o2);
    }

    public ArraySet(Comparator<? super E> comparator) {
        elements = Collections.unmodifiableList(new ArrayList<>());
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends E> collection) {
        this();
        Set<E> set = new TreeSet<>(collection);
        elements = Collections.unmodifiableList(new ArrayList<>(set));
    }

    public ArraySet(SortedSet<E> sortedSet) {
        this(Collections.unmodifiableList(new ArrayList<>(sortedSet)), sortedSet.comparator());
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        Set<E> set = new TreeSet<>(comparator);
        set.addAll(collection);

        elements = Collections.unmodifiableList(new ArrayList<>(set));
        this.comparator = comparator;
    }

    private ArraySet(List<E> arraySet, Comparator<? super E> comparator) {
        elements = arraySet;
        this.comparator = comparator;
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
        return (naturalOrdering ? null : comparator);
    }

    @Override
    public boolean contains(Object o) {
        @SuppressWarnings("unchecked")
        E e = (E) o;
        int index = lowerBound(elements, e, comparator());
        return (index < size() && comparator.compare(elements.get(index), e) == 0);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        int fromIndex = lowerBound(elements, fromElement, comparator);
        int toIndex = lowerBound(elements, toElement, comparator);
        return new ArraySet<>(elements.subList(fromIndex, Math.max(toIndex, fromIndex)), comparator);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int toIndex = lowerBound(elements, toElement, comparator);
        return new ArraySet<>(elements.subList(0, toIndex), comparator);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int fromIndex = lowerBound(elements, fromElement, comparator);
        return new ArraySet<>(elements.subList(fromIndex, elements.size()), comparator);
    }

    @Override
    public E first() {
        if (isEmpty())
            throw new NoSuchElementException("ArraySet is empty");
        return elements.get(0);
    }

    @Override
    public E last() {
        if (isEmpty())
            throw new NoSuchElementException("ArraySet is empty");
        return elements.get(elements.size() - 1);
    }

    private int lowerBound(List<E> elements, E key, Comparator<? super E> comparator) {
        int index = Collections.binarySearch(elements, key, comparator);
        if (index < 0) {
            index = -index - 1;
        }
        return index;
    }
}
