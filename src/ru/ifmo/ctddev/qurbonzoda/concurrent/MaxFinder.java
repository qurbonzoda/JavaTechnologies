package ru.ifmo.ctddev.qurbonzoda.concurrent;

import java.util.Comparator;
import java.util.List;


/**
 * This class is used to find maximum element in the list using the given comparator
 * This class implements Runnable interface, so it may be used to start new thread
 *
 * @author Abduqodiri Qurbonzoda
 *
 * @version 1.0
 * @since 22.03.2016
 * @param <T> The generic type
 */
public class MaxFinder<T> implements Runnable {

    private final List<? extends T> list;
    private T result = null;
    private Comparator<? super T> comparator;


    /**
     * The constructor of this class. Just initializes fields
     * @param list The list to find maximum element of
     * @param comparator The comparator to use
     */
    public MaxFinder(List<? extends T> list, Comparator<? super T> comparator) {
        this.list = list;
        this.comparator = comparator;
    }



    /**
     * This method checks if any of the elements of the list matches the predicate.
     * Just writes answer to the result field
     */
    @Override
    public void run() {
        // result = list.stream().max(comparator);
        for (T element : list) {
            if (result == null) {
                result = element;
            } else if (comparator.compare(result, element) < 0) {
                result = element;
            }
        }
    }

    /**
     * This is used to get result of the work of run method
     * @return T result
     */
    public T getResult() {
        return result;
    }
}
