package ru.ifmo.ctddev.qurbonzoda.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Generates class which implements supplied {@link Class} and locate the
 * implementation to the supplied {@link Path}. This class supports
 * <code>interfaces</code> and doesn't support <code>abstract classes</code>
 * No <code>Generics</code> supported.
 * <p>
 * The generated class has the same name as the interface with the
 * <code>Impl</code> suffix. It is not abstract and compiles without errors.
 * Methods of generated class ignore the arguments and returns the default
 * value of the return type of the method.
 *
 */
public class Implementor implements Impler, JarImpler {
    /**
     * The package of the <code>interface</code>
     */
    private String thePackage = "";

    /**
     * The {@link String} representation of declaration of the class
     */
    private String classDeclaration = "";


    /**
     * The container storing representations of the methods of the class
     */
    private HashMap<String, Boolean> methodDeclaration = new HashMap<>();


    /**
     * The root path of the class
     */
    private Path path;

    /**
     * The writer which writes the class to the file
     */
    private PrintWriter writer = null;

    /**
     * Main method, entry point of the {@code class}.
     * <p>
     * If the first arguments is "-jar", then method tries to implement
     * interface given as the second argument which is in the jar-file given
     * as the third argument. It looks like:
     * <code>-jar interface jar-file</code>
     * <p>
     * Otherwise, tries to generate class implementing interface given as the
     * first argument, and locates it in the directory given as the second
     * argument.
     *
     * @param args arguments from the command line
     */
    public static void main(String[] args) {


        if (args == null || args.length < 2) {
            System.out.println("Wrong usage!");
            printUsage();
            return;
        }

        if (args[0].equals("-jar") && args.length >= 3) {
            try {
                Class<?> clazz = Class.forName(args[1]);
                new Implementor().implementJar(clazz, Paths.get(args[2]));
            } catch (ClassNotFoundException e) {
                System.out.println("Couldn't find class: " + args[1]);
            } catch (ImplerException e) {
                System.out.println("Couldn't implement interface, reason: " + e.getMessage());
            }
        } else if (!args[0].equals("-jar")) {
            try {
                Class<?> clazz = Class.forName(args[0]);
                new Implementor().implement(clazz, Paths.get(args[1]));
            } catch (ClassNotFoundException e) {
                System.out.println("Couldn't find class: " + args[0]);
            } catch (ImplerException e) {
                System.out.println("Couldn't implement interface, reason: " + e.getMessage());
            }
        } else {
            System.out.println("Wrong usage!");
            printUsage();
        }
    }

    /**
     * Prints usage of the class
     */
    private static void printUsage() {
        System.out.println("    Usage 1: \"-jar\" <ClassName> existing <JarFile>\n" +
                "   Usage 2: <ClassName> directory <File>");
    }

    /**
     * Generates class which implements provided {@link Class} token and
     * locate the implementation to the provided {@link Path} path.
     * Class package directories will be created if they doesn't exist.
     * Generated class implements provided interface and compiles without
     * errors. Class and interface must not contain generics
     *
     * @param token the interface to be implemented by the generated class
     * @param root  the directory to locate the generated class
     * @throws ImplerException if an error occurred while
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface()) {
            throw new ImplerException("The app support only interfaces");
        }
        path = root;
        try {
            implementClass(token);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Takes a {@code Class} as a {@code clazz} parameter and tries to implement
     * it. If the the directories and package doesn't exist the method tries to
     * create them.
     *
     * @param clazz             the interface to implement
     * @throws ImplerException  if you don't have an exception occurs while
     *                          processing this method. For instance, if you
     *                          don't have required permissions to write in
     *                          the given directories
     */
    private void implementClass(Class clazz) throws ImplerException {

        thePackage = clazz.getPackage().getName();

        String outputDir = path.toString() + File.separator + thePackage.replace(".", File.separator);

        try {
            Path dir = Paths.get(outputDir);
            Files.createDirectories(dir);

            String outputFile = outputDir + File.separator + clazz.getSimpleName() + "Impl.java";
            writer = new PrintWriter(outputFile, "UTF-8");
        } catch (IOException e) {
            throw new ImplerException("Exception while trying to create writer, reason: ", e);
        }

        classDeclaration = "public class " + clazz.getSimpleName() + "Impl implements " + clazz.getCanonicalName();

        implementMethods(clazz.getMethods());

        writeClass(writer);
        writer.flush();
    }

    /**
     * This method prints generated class to the given {@code PrintWriter}.
     *
     * @param classWriter the writer where to write the class
     */
    private void writeClass(PrintWriter classWriter) {
        if (!thePackage.isEmpty()) {
            classWriter.println("package " + thePackage + ";");
        }
        classWriter.write(classDeclaration + " {");
        methodDeclaration.keySet().forEach(classWriter::println);
        classWriter.write("}");
    }

    /**
     * This method creates {@link String} representation of {@link Method}
     * {@code Object} and stores it in the {@code methodDeclaration}
     * {@link HashMap}
     *
     * @param methods the array of methods to put in the
     *                {@code methodDeclaration} {@link HashMap}
     */
    private void implementMethods(Method[] methods) {
        for (Method method : methods) {

            int methodModifiers = method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT;

            StringBuilder curMethodDeclaration = new StringBuilder(Modifier.toString(methodModifiers)).append(" ");
            curMethodDeclaration.append(method.getReturnType().getCanonicalName())
                    .append(" ")
                    .append(method.getName())
                    .append("(")
                    .append(getParameters(method.getParameterTypes()))
                    .append(") {  return ")
                    .append(anInstance(method.getReturnType()))
                    .append("; }");

            //System.out.println(curMethodDeclaration);
            methodDeclaration.put(curMethodDeclaration.toString(), Boolean.TRUE);
        }
    }

    /**
     * Returns {@link String} representation of the parameters of a method.
     *
     * @param params  array of parameters to return representation of
     * @return String the {@link String} representation of the parameters
     */
    private String getParameters(Class[] params) {
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            parameters.append(i == 0 ? "" : ", ").append(params[i].getCanonicalName()).append(" arg").append(i);
        }
        return parameters.toString();
    }

    /**
     * Returns {@link String} representation of an instance of the given
     * {@link Class}.
     *
     * @param type    the {@link Class} to return instance of
     * @return String the representation of the instance
     */
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

    /**
     * Implements given interface and puts generated file in the jar-file.
     * <p>
     * Creates temporary directory and generates implementation of the
     * interface {@code clazz} with the name "classname" + "Impl.class". Puts this implementation and {@code Manifest}
     * into given {@code jarFile}.
     *
     * @param clazz             {@code Class} to implement to
     * @param jarFile           file to print to
     * @throws ImplerException  if {@code exitCode} of compilation of the implementing class is not {@code 0}
     */
    @Override
    public void implementJar(Class<?> clazz, Path jarFile) throws ImplerException {
        Objects.requireNonNull(clazz, "Implementing class is null");
        Objects.requireNonNull(jarFile, "arFile is null");

        Path root = Paths.get(".");
        try {
            root = Files.createTempDirectory("temp_root");
        } catch (IOException e) {
            System.err.println("Couldn't create temporary directory");
        }
        implement(clazz, root);
        String name = (clazz.getPackage() != null ? clazz.getPackage().getName().replace(".", File.separator) : "")
                + File.separator + clazz.getSimpleName() + "Impl";
        int exitCode = compile(root, root.toAbsolutePath() + File.separator + name + ".java");
        if (exitCode != 0) {
            throw new ImplerException("Compilation error, exitCode = " + exitCode + ", name = " + name);
        }
        createJar(root, jarFile.toAbsolutePath().toString(), name + ".class");
    }

    /**
     * Packs the class in the given jar-file.
     * <p>
     * Puts {@code Manifest} and compiled class-file {@code classFile} in the the given {@code jarFile}
     * in the given directory {@code root}.
     *
     * @param root      directory to locate {@code jarFile}
     * @param jarFile   {@code JarFile} to write to
     * @param classFile {@code String} representing classFile of the {@code Class}
     */
    private static void createJar(Path root, String jarFile, String classFile) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        Path source = root.resolve(classFile);
        try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarFile), manifest)) {
            jarOutputStream.putNextEntry(new ZipEntry(classFile));
            Files.copy(source, jarOutputStream);
        } catch (IOException e) {
            System.err.println("== CreateJar, classFile = " + classFile + ", message = " + e.getMessage());
        }
    }

    /**
     * Returns result of the compiling.
     * <p>
     * Compiles the given {@code file} in the directory {@code root} and returns the exit code of the compilation
     *
     * @param root directory where the file is located
     * @param file name of the file to compile
     * @return int resulting code of the compilation
     */
    private static int compile(final Path root, final String file) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] args  = {file, "-cp", root + File.pathSeparator + System.getProperty("java.class.path")};
        return compiler.run(null, null, null, args);
    }
}