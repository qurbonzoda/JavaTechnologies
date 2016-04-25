package ru.ifmo.ctddev.qurbonzoda.mapper;

import java.util.List;
import java.util.Queue;
import java.util.function.Function;

/**
 * Created by qurbonzoda on 31.03.16.
 */
class Worker implements Runnable {
    private final Queue<Task> taskQueue;

    /**
     * The constructor taking task queue.
     *
     * @param taskQueue The container where tasks appear.
     */
    public Worker(Queue<Task> taskQueue) {
        this.taskQueue = taskQueue;
    }

    /**
     * Takes a task from task queue and executes it.
     * <p/>
     * If the task queue is empty the threads waits the notification
     * The method ends if the current thread is interrupted
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Task task;
            synchronized (taskQueue) {
                while (taskQueue.isEmpty()) {
                    try {
                        taskQueue.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                task = taskQueue.poll();
            }
            task.execute();
        }
    }
}
