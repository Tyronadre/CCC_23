package util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;

public class Framework {
    public static final String folder = "run/ccc_25";
    private static final BiFunction<Integer, Integer, String> fileName = (level, task) -> folder + "/level" + level + "/level" + level + "_" + task;

    public static List<String> readFile(int level, int task) {
        try {
            return Files.readAllLines(new File(fileName.apply(level, task) + ".in").toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading file " + fileName.apply(level, task));
            throw new RuntimeException(e);
        }
    }

    public static String readFileString(int level, int task) {
        try {
            return Files.readString(new File(fileName.apply(level, task) + ".in").toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading file " + fileName.apply(level, task));
            throw new RuntimeException(e);
        }
    }

    public static void writeOutput(int level, int task, String output) {
        try {
            Files.writeString(new File(fileName.apply(level, task) + ".out").toPath(), output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error writing file " + fileName.apply(level, task));
            throw new RuntimeException(e);
        }

    }
}
