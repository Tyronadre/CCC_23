package contest_25;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Framework2 {

    public static List<String> readFile(int level, int task) {
        String folder = "run/ccc_25";
        var file = new File(folder + "/level" + level + "/level" + level + "_" + task + ".in");
        try {
            return Files.readAllLines(Path.of(file.getPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeOutput(int level, int task, String output) {
        String folder = "run/ccc_25";
        var file = new File(folder + "/level" + level + "/level" + level + "_" + task + ".out");
        try (var writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
