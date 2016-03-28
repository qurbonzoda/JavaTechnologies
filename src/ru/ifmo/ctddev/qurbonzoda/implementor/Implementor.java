package ru.ifmo.ctddev.qurbonzoda.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by qurbonzoda on 30.11.15.
 */
public class Implementor implements Impler {
    private String thePackage;
    private String classDeclaration;
    private List<StringBuilder> methodDeclaration;
    private Path path;
    private PrintWriter writer;

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        init(root);
        try {
            implementClass(token);
        } catch (Exception e) {
            throw new ImplerException("Error occured" + e.getMessage(), e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void init(Path root) {
        thePackage = "";
        classDeclaration = "";
        methodDeclaration = new ArrayList<>();
        path = root;
        writer = null;
    }

    private void implementClass(Class clazz) throws IOException, ImplerException {

        thePackage = clazz.getPackage().getName();

        String outputDir = path.toString() + File.separator + thePackage.replace(".", File.separator);

        try {
            Path dir = Paths.get(outputDir);
            Files.createDirectories(dir);

            String outputFile = outputDir + File.separator + clazz.getSimpleName() + "Impl.java";
            writer = new PrintWriter(outputFile, "UTF-8");
        } catch (IOException e) {
            throw new ImplerException("write", e);
        }

        classDeclaration = "public class " + clazz.getSimpleName() + "Impl implements " + clazz.getCanonicalName();

        List<Method> methods = Arrays.asList(clazz.getMethods());

        implementMethods(methods);

        try {
            writeClass(writer);
            writer.flush();
        } catch (Exception e) {
            throw new ImplerException("write", e);
        }
    }

    private void writeClass(PrintWriter classWriter) {
        if (!thePackage.isEmpty()) {
            classWriter.println("package " + thePackage + ";");
        }
        classWriter.write(classDeclaration + " {");
        methodDeclaration.forEach(classWriter::println);
        classWriter.write("}");
    }

    private void implementMethods(List<Method> methods) {
        for (Method method : methods) {

            int methodModifiers = method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT;

            StringBuilder curMethodDeclaration = new StringBuilder(Modifier.toString(methodModifiers)).append(" ");
            curMethodDeclaration.append(method.getReturnType().getCanonicalName())
                    .append(" ")
                    .append(method.getName())
                    .append("(")
                    .append(writeParams(method.getParameterTypes()))
                    .append(") {  return ")
                    .append(anInstance(method.getReturnType()))
                    .append("; }");

            //System.out.println(curMethodDeclaration);
            methodDeclaration.add(curMethodDeclaration);
        }
    }

    private String writeParams(Class[] params) {
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            Class param = params[i];
            parameters.append(i == 0 ? "" : ", ").append(param.getCanonicalName()).append(" arg").append(i);
        }
        return parameters.toString();
    }

    private String anInstance(Class type) {
        if (type.equals(Boolean.TYPE)) {
            return "false";
        } else if (type.equals(Void.TYPE)) {
            return "";
        } else if (type.isPrimitive()) {
            return "0";
        } else {
            return "null";
        }
    }
}