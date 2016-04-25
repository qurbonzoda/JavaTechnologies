package ru.ifmo.ctddev.qurbonzoda.concurrent;

import java.util.List;
import java.util.function.Predicate;

/**
 * This class is used to check if any of the elements in the list match the predicate
 * This class implements Runnable interface, so it may be used to start new thread
 *
 * @author Abduqodiri Qurbonzoda
 *
 * @version 1.0
 * @since 22.03.2016
 * @param <T> The generic type
 */
public class AnyMatchChecker<T> implements Runnable {

    private final List<? extends T> list;
    private boolean result = false;
    private Predicate<? super T> predicate;

    /**
     * The constructor of this class
     * @param list The list to check elements of
     * @param predicate The predicate to use for checking
     */
    public AnyMatchChecker(List<? extends T> list, Predicate<? super T> predicate) {
        this.list = list;
        this.predicate = predicate;
    }

    /**
     * This method checks if any of the elements of the list matches the predicate.
     * Just writes answer to the result field
     */
    @Override
    public void run() {
        result = list.stream().anyMatch(predicate);
    }

    /**
     * This is used to get result of the work of run method
     * @return boolean result
     */
    public boolean getResult() {
        return result;
    }
}
