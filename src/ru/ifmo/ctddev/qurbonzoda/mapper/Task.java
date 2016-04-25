package ru.ifmo.ctddev.qurbonzoda.mapper;

import java.util.function.Function;

class Task<T, R> {
    private final Function<? super T, ? extends R> f;
    private final T arg;
    private R result;
    private boolean executed = false;

    /**
     * Constructor taking {@link Function} {@code f} and argument of this
     * function {@code arg}.
     *
     * @param f     the {@link Function} to apply
     * @param arg   the argument to apply {@code f} on
     */
    Task(Function<? super T, ? extends R> f, T arg) {
        this.f = f;
        this.arg = arg;
    }

    /**
     * Applies the {@link Function} on the argument.
     * <p/>
     * The result of the application can be accessed by method {@code get}.
     *
     * @see #get
     */
    synchronized void execute() {
        result = f.apply(arg);
        executed = true;
        this.notifyAll();
    }

    /**
     * Returns the result of the application of the {@link Function} on argument
     * which was calculated by method {@code execute}.
     *
     * @return R the result of the calculation.
     * @see #execute
     */
    synchronized R get() {
        while (!executed) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                return null;
            }
        }
        return result;
    }
}
