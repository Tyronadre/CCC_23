package contest_25;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class T4 {
    public static void main(String[] args) {
        final int level = 4;
        for (int i = 0; i <= 2; i++) {

            var lines = Framework2.readFile(level, i);
            int numberOfLines = Integer.parseInt(lines.get(0));
            var output = new ArrayList<String>();
            for (int j = 1; j <= numberOfLines; j++) {
                output.add(solve(lines.get(j)) + "\n");
            }
            Framework2.writeOutput(level, i, String.join("", output));
        }
    }

    public static String solve(String line) {
        var parse = spaceStations(line);
        var movement1 = generateMovement(parse.get(0), parse.get(2)).stream().map(String::valueOf).toList();
        var movement2 = generateMovement(parse.get(1), parse.get(2)).stream().map(String::valueOf).toList();
        var res = new StringBuilder();
        res.append(String.join(" ", movement1));
        res.append("\n");
        res.append(String.join(" ", movement2));
        res.append("\n");
        return res.toString();
    }

    public static List<Integer> generateMovement(Integer nextSpaceStation, Integer timeLimit) {
        var result = new ArrayList<Integer>();
        var time = 0;
        result.add(0);
        var distance = Math.abs(nextSpaceStation);
        if (distance == 0) return result;
        var direction = nextSpaceStation < 0 ? -1 : 1;
        var pace = 5;
        while (pace > 0) {
            if (result.size() > 1000)
                System.out.println("result to big");
            if (6 - pace == distance) {
                result.add(pace * direction);
                time += pace;
                pace++;
                if (pace == 6) pace = 0;
                distance--;
                continue;
            }
            if (6 - pace < distance - 1) {
                result.add(pace * direction);
                time += pace;
                if (pace != 1) pace--;
                distance--;
                continue;
            }

            result.add(pace * direction);
            distance--;
            time += pace;
        }
        result.add(0);
        if (time > timeLimit) {
            throw new RuntimeException();
        }

        return result;
    }


    public static List<Integer> spaceStations(String line) {
        var split1 = line.split(" ");
        var split2 = split1[0].split(",");
        var res = new ArrayList<Integer>();
        res.add(Integer.valueOf((split2[0])));
        res.add(Integer.valueOf((split2[1])));
        res.add(Integer.valueOf((split1[1])));
        return res;
    }

}
