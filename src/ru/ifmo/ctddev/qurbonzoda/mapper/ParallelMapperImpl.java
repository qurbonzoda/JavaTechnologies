package ru.ifmo.ctddev.qurbonzoda.mapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.kgeorgiy.java.advanced.mapper.*;

public class ParallelMapperImpl implements ParallelMapper {

    private final ArrayList<Thread> threadPool;
    private final Queue<Task> taskQueue;

    /**
     * Creates {@code threads} worker threads which can be used for paralleling.
     * <p/>
     * The created thread wait appearance of tasks and executed them if one
     * added.
     *
     * @param threads the number of threads to create
     */
    public ParallelMapperImpl(int threads) {
        taskQueue = new ArrayDeque<>();
        threadPool = new ArrayList<>(threads);
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(new Worker(taskQueue));
            thread.start();
            threadPool.add(thread);
        }
    }

    /**
     * Calculates {@link Function} f of every given {@code args}
     * <p/>
     * The method adds tasks to the task queue and notify all working threads
     * that tasks appeared.
     *
     * @param f     the function to apply to every {@code args}
     * @param args  the arguments to apply {@code f} on
     * @param <T>   The arguments type
     * @param <R>   The return type of {@link Function} f
     * @return List<R>  The results of applying f on every argument
     * @throws InterruptedException If the threads were interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<Task<T, R>> tasks = args.stream().map(arg -> new Task<T, R>(f, arg)).collect(Collectors.toList());

        synchronized (taskQueue) {
            taskQueue.addAll(tasks);
            taskQueue.notifyAll();
        }

        return tasks.stream().map(Task::get).collect(Collectors.toList());
    }

    /**
     * Interrupts all worker threads.
     *
     * @throws InterruptedException if the current thread was interrupted.
     */
    @Override
    public void close() throws InterruptedException {
        threadPool.forEach(Thread::interrupt);
    }
}
