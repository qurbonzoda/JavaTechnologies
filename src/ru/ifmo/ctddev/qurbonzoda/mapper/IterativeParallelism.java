package ru.ifmo.ctddev.qurbonzoda.mapper;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import ru.ifmo.ctddev.qurbonzoda.concurrent.AnyMatchChecker;
import ru.ifmo.ctddev.qurbonzoda.concurrent.MaxFinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.ifmo.ctddev.qurbonzoda.concurrent.IterativeParallelism.dealWithThreads;
import static ru.ifmo.ctddev.qurbonzoda.concurrent.IterativeParallelism.getRanges;

/**
 * This class has has several methods to process array on several threads
 *
 * @author Abduqodiri Qurbonzoda
 * @version 1.0
 * @since 22.03.2016
 */
public class IterativeParallelism implements ScalarIP {

    private ParallelMapper mapper;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }
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

        if (mapper != null) {
            Function<List<? extends T>, T> maxFunction = values -> values.stream().max(comparator).get();
            return maxFunction.apply(mapper.map(maxFunction, rangeList));
        } else {
            List<MaxFinder<T>> runnableList = rangeList.stream().map(x -> new MaxFinder<T>(x, comparator))
                    .collect(Collectors.toList());

            dealWithThreads(runnableList);
            return runnableList.stream().map(MaxFinder::getResult).max(comparator).get();
        }
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
        List< List<? extends T> > rangeList = getRanges(list, threads);
        if (mapper != null) {
            Function<List<? extends T>, Boolean> anyFunction = values -> values.stream().anyMatch(predicate);
            return mapper.map(anyFunction, rangeList).contains(Boolean.TRUE);
        } else {
            List<AnyMatchChecker<T>> runnableList = rangeList.stream().map(x -> new AnyMatchChecker<T>(x, predicate))
                    .collect(Collectors.toList());

            dealWithThreads(runnableList);
            return runnableList.stream().map(AnyMatchChecker::getResult).anyMatch(Predicate.isEqual(true));
        }
    }

}
