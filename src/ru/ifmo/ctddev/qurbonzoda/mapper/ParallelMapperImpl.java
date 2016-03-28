package ru.ifmo.ctddev.qurbonzoda.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by qurbonzoda on 28.03.16.
 */
public class ParallelMapperImpl implements ParallelMapper {

    ArrayList<Thread> threadPool;

    public ParallelMapperImpl(int threads) {
        for (int i = 0; i < threads; i++) {
            threadPool.add(new Thread());
        }
    }

    @Override
    public <T, R> List<R> run(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        ArrayList<R> results = new ArrayList<>(args.size());
        for (T arg : args) {
            results.add(f.apply(arg));
        }
        return results;
    }

    @Override
    public void close() throws InterruptedException {
        for (Thread thread : threadPool) {
            thread
            thread.interrupt();
        }
    }
}
