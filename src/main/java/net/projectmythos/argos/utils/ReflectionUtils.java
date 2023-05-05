package net.projectmythos.argos.utils;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import net.projectmythos.argos.API;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReflectionUtils {

    /**
     * Returns a list of superclasses, including the provided class
     *
     * @param clazz subclass
     * @return superclasses
     */
    public static <T> List<Class<? extends T>> superclassesOf(Class<? extends T> clazz) {
        List<Class<? extends T>> superclasses = new ArrayList<>();
        while (clazz.getSuperclass() != Object.class) {
            superclasses.add(clazz);
            clazz = (Class<? extends T>) clazz.getSuperclass();
        }

        superclasses.add(clazz);
        return superclasses;
    }

    public static <T> Set<Class<? extends T>> subTypesOf(Class<T> superclass, String... packages) {
        return getClasses(packages, subclass -> {
            if (!Utils.canEnable(subclass))
                return false;

            if (superclass.isInterface())
                return subclass.implementsInterface(superclass);
            else
                return subclass.extendsSuperclass(superclass);
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<Class<? extends T>> typesAnnotatedWith(Class<? extends Annotation> annotation, String... packages) {
        try (var scan = scanPackages(packages).scan()) {
            return scan.getClassesWithAnnotation(annotation).stream()
                    .filter(Utils::canEnable)
                    .map(ClassInfo::loadClass)
                    .map(clazz -> (Class<? extends T>) clazz)
                    .collect(Collectors.toSet());
        }
    }

    public static Set<Method> methodsAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotation) {
        return new HashSet<>() {{
            for (Method method : getAllMethods(clazz)) {
                method.setAccessible(true);
                if (method.getAnnotation(annotation) != null)
                    add(method);
            }
        }};
    }

    private static ClassGraph scanPackages(String... packages) {
        final ClassGraph scanner = new ClassGraph()
                .acceptPackages(packages)
                .enableClassInfo()
                .enableAnnotationInfo()
                .initializeLoadedClasses();

        if (API.get().getClassLoader() != null)
            scanner.overrideClassLoaders(API.get().getClassLoader());

        return scanner;
    }

    @SuppressWarnings("unchecked")
    private static <T> Set<Class<? extends T>> getClasses(String[] packages, Predicate<ClassInfo> filter) {
        try (var scan = scanPackages(packages).scan()) {
            return scan.getAllClasses().stream()
                    .filter(filter)
                    .map(ClassInfo::loadClass)
                    .map(clazz -> (Class<? extends T>) clazz)
                    .collect(Collectors.toSet());
        }
    }

    private static Set<Method> getAllMethods(Class<?> clazz) {
        return new HashSet<>(new HashMap<String, Method>() {{
            for (Class<?> clazz : Utils.reverse(superclassesOf(clazz)))
                for (Method method : clazz.getDeclaredMethods())
                    put(getMethodKey(method), method);
        }}.values());
    }

    @NotNull
    private static String getMethodKey(Method method) {
        final String params = Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getType().getSimpleName())
                .collect(Collectors.joining(","));

        return "%s(%s)".formatted(method.getName(), params);
    }

}
