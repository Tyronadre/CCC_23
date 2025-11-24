package graphLib.alg;

import graphLib.base.Graph;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class AlgorithmFactory {
    private static final List<Class<?>> algorithms;

    static {
        algorithms = new ArrayList<>();
        algorithms.add(AStar.class);
    }

    private AlgorithmFactory() {
    }

    public static List<Class<?>> getAllAlgorithms() {
        return algorithms;
    }

    public static <T extends Comparable<T>> Algorithm<T, ?> createAlgorithm(Class<?> algo, Graph<T> graph) {
        try {
            return (Algorithm<T, ?>) algo.getConstructor(graph.getClass()).newInstance(graph);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
