package ru.ifmo.ctddev.qurbonzoda.arrayset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Created by qurbonzoda on 22.02.16.
 */
public class Tester {
    public static void main(String[] args) {
        TreeSet<Integer> set = new TreeSet<>((o1, o2) -> (o2.compareTo(o1)));
        set.add(1);
        set.add(4);
        set.add(5);
        set.add(2);
        set.add(3);
        set.add(4);

        ArraySet<Integer> arraySet = new ArraySet<Integer>(set);

        System.out.println(arraySet);
        System.out.println(arraySet.headSet(3));
        System.out.println(arraySet.tailSet(3));
        System.out.println(arraySet.subSet(4, 3));
    }
}
