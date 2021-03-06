package ru.ifmo.ctddev.qurbonzoda.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class has has several methods to process array on several threads
 *
 * @author Abduqodiri Qurbonzoda
 * @version 1.0
 * @since 22.03.2016
 */
public class IterativeParallelism implements ScalarIP {

    /**
     * This method is used to find maximum element in the list using several threads using the given comparator
     *
     * @param <T>        The generic type
     * @param threads    This is the number if threads to be used.
     * @param list       This is the list to find maximum from
     * @param comparator This is the comparator to use to find maximum
     * @return T This returns maximum element in the list
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator)
            throws InterruptedException {
        List< List<? extends T> > rangeList = getRanges(list, Math.min(threads, list.size()));

        List<MaxFinder<T>> runnableList = new ArrayList<>(rangeList.size());
        rangeList.forEach(range -> runnableList.add(new MaxFinder<T>(range, comparator)));

        dealWithThreads(runnableList);
        return runnableList.stream().map(MaxFinder::getResult).max(comparator).get();
    }

    /**
     * This method is used to find minimum element in the list using several threads using the given comparator
     *
     * @param <T>        The generic type
     * @param threads    This is the number if threads to be used.
     * @param list       This is the list to find minimum from
     * @param comparator This is the comparator to use to find minimum
     * @return T This returns minimum element in the list according to the comparator
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator)
            throws InterruptedException {
        return maximum(threads, list, comparator.reversed());
    }


    /**
     * This method is used to to check if all elements in the list match the predicate
     * using several threads using the given comparator
     *
     * @param <T>       The generic type
     * @param threads   This is the number of threads to be used.
     * @param list      This is the list to check elements of
     * @param predicate This is the predicate to check for matching
     * @return true - if all elements match the predicate
     * false - otherwise
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate)
            throws InterruptedException {
        return !any(threads, list, predicate.negate());
    }

    /**
     * This method is used to to check if any of elements in the list match the predicate
     * using several threads using the given comparator
     *
     * @param <T>       The generic type
     * @param threads   This is the number of threads to be used.
     * @param list      This is the list to check elements of
     * @param predicate This is the predicate to check for matching
     * @return true - if any of elements match the predicate
     * false - otherwise
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate)
            throws InterruptedException {
        List<List<? extends T>> rangeList = getRanges(list, threads);

        List<AnyMatchChecker<T>> runnableList = new ArrayList<>(rangeList.size());
        rangeList.forEach(range -> runnableList.add(new AnyMatchChecker<T>(range, predicate)));

        dealWithThreads(runnableList);
        return runnableList.stream().map(AnyMatchChecker::getResult).anyMatch(Predicate.isEqual(true));

    }

    public static  <T> List< List<? extends T> > getRanges(List<? extends T> list, int ranges) {
        int elementsLeft = list.size();
        List<List<? extends T>> rangeList = new ArrayList<>();
        int leftIndex = 0;
        while (ranges > 0) {
            int rightIndex = leftIndex + elementsLeft / ranges;
            rangeList.add(list.subList(leftIndex, rightIndex));

            elementsLeft -= rightIndex - leftIndex;
            leftIndex = rightIndex;
            ranges--;
        }
        return rangeList;
    }

    public static <T> void dealWithThreads(List<? extends Runnable> runnableList) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
        runnableList.forEach(runnable -> threadList.add(new Thread(runnable)));
        for (Runnable runnable : runnableList) {
            Thread currentThread = new Thread(runnable);
            threadList.add(currentThread);
            currentThread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
    }
}
