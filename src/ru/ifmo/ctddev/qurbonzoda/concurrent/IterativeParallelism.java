package ru.ifmo.ctddev.qurbonzoda.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
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
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        List<Pair<Integer, Integer>> rangeList = getRanges(list, threads);
        List<MaxFinder<T>> runnableList = new ArrayList<>();

        for (Pair<Integer, Integer> range : rangeList) {
            MaxFinder<T> maxFinder = new MaxFinder<>(list.subList(range.getKey(), range.getValue()), comparator);
            runnableList.add(maxFinder);
        }
        dealWithThreads(runnableList);

        List<T> maximums = runnableList.stream().map(MaxFinder::getResult).collect(Collectors.toList());
        MaxFinder<T> maxFinder = new MaxFinder<>(maximums, comparator);
        maxFinder.run();
        return maxFinder.getResult();
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
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
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
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
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
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<Pair<Integer, Integer>> rangeList = getRanges(list, threads);
        List<AnyMatchChecker<T>> runnableList = new ArrayList<>();

        for (Pair<Integer, Integer> range : rangeList) {
            AnyMatchChecker<T> anyMatchChecker = new AnyMatchChecker<>(list.subList(range.getKey(), range.getValue()), predicate);
            runnableList.add(anyMatchChecker);
        }

        dealWithThreads(runnableList);

        for (AnyMatchChecker<T> runnable : runnableList) {
            if (runnable.getResult()) {
                return true;
            }
        }
        return false;
    }

    private <T> List<Pair<Integer, Integer>> getRanges(List<? extends T> list, int ranges) {
        int elementsLeft = list.size();
        List<Pair<Integer, Integer>> rangeList = new ArrayList<>();
        int leftIndex = 0;
        while (ranges > 0) {
            int rightIndex = leftIndex + elementsLeft / ranges;
            rangeList.add(new Pair<>(leftIndex, rightIndex));

            elementsLeft -= rightIndex - leftIndex;
            leftIndex = rightIndex;
            ranges--;
        }
        return rangeList;
    }

    private <T> void dealWithThreads(List<? extends Runnable> runnableList) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
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
