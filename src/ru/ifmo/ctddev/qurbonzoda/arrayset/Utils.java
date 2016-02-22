package ru.ifmo.ctddev.qurbonzoda.arrayset;

import java.util.Comparator;
import java.util.List;

/**
 * Created by qurbonzoda on 22.02.16.
 */
public class Utils {
    public static<E> int lowerBound(List<E> list, E target, Comparator<? super E> comparator) {
        int first = 0;
        int last = list.size();
        while (first + 1 < last) {
            int middle = (first + last) / 2;
            if (comparator.compare(list.get(middle), target) < 0) {
                first = middle;
            } else {
                last = middle;
            }
        }
        if (comparator.compare(list.get(first), target) >= 0) {
            return first;
        }
        return last;
    }

    public static<E> int upperBound(List<E> list, E target, Comparator<? super E> comparator) {
        int first = 0;
        int last = list.size();
        while (first + 1 < last) {
            int middle = (first + last) / 2;
            if (comparator.compare(list.get(middle), target) <= 0) {
                first = middle;
            } else {
                last = middle;
            }
        }
        if (comparator.compare(list.get(first), target) > 0) {
            return first;
        }
        return last;
    }
}
