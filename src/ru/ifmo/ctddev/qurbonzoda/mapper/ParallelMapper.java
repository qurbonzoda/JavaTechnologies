package ru.ifmo.ctddev.qurbonzoda.mapper;

import java.util.List;
import java.util.function.Function;

/**
 * Created by qurbonzoda on 28.03.16.
 */
public interface ParallelMapper extends AutoCloseable {
    <T, R> List<R> run(
            Function<? super T, ? extends R> f,
            List<? extends T> args
    ) throws InterruptedException;

    @Override
    void close() throws InterruptedException;
}