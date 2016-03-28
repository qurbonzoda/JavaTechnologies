package ru.ifmo.ctddev.qurbonzoda.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;

import java.nio.file.Paths;

/**
 * Created by qurbonzoda on 12.03.16.
 */
public class Tester {
    public static void main(String[] args) {
        Impler impler = new Implementor();
        try {
            Class<?> clazz = Class.forName("javax.management.Descriptor");
            impler.implement(clazz, Paths.get("/home/qurbonzoda/Programming/ideaProjects/JavaTechnologies/src/"));
        } catch (Exception e) {
            System.out.println("exception happened: " + e.getMessage());
        }
    }
}
