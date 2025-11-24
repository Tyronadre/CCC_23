package contest_25;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class T3 {
    public static void main(String[] args) {
        final int level = 3;
        for (int i = 0; i <= 2; i++) {

            var lines = Framework2.readFile(level, i);
            int numberOfLines = Integer.parseInt(lines.get(0));
            var output = new ArrayList<String>();
            for (int j = 1; j <= numberOfLines; j++) {
                output.add(solve(lines.get(j)) + "\n");
            }
            System.out.println(output);
            Framework2.writeOutput(level, i, String.join("", output));
        }
    }

    public static String solve(String line) {
        var spaceStation = spaceStations(line).get(0);
        var timeLimit = spaceStations(line).get(1);
        var movement = generateMovement(spaceStation, timeLimit);

        return String.join(" ", movement.stream().map(String::valueOf).toList());
    }

    public static String intListToString(List<Integer> list) {
        var s = new StringBuilder();
        for (Integer i : list) {
            s.append(i).append(" ");
        }
        return s.toString();
    }

    public static List<Integer> generateMovement(Integer nextSpaceStation, Integer timeLimit) {
        var result = new ArrayList<Integer>();
        var time = 0;
        result.add(0);
        var distance = Math.abs(nextSpaceStation);
        var direction = nextSpaceStation < 0 ? -1 : 1;
        var pace = 5;
        while (pace > 0) {
            //check if the current pace with a perfect decleration hits that point
            // pace 5 -> 1
            // pace 4 -> 2
            // pace 3 -> 3
            // pace 2 -> 4
            // pace 1 -> 5
            if (6 - pace == distance) {
                result.add(pace * direction);
                time += pace;
                pace++;
                if (pace == 6) pace = 0;
                distance--;
                continue;
            }
            // if we can accelerate we need to have at least 2 more move spots
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
        System.out.println("input" + nextSpaceStation + "," + timeLimit + "," + result);
        System.out.println("Time" + time + " limit: " + timeLimit);
        if (time > timeLimit) {
            throw new RuntimeException();
        }

        return result;
    }


    public static List<Integer> spaceStations(String line) {
        return Arrays.stream(line.split(" ")).map(Integer::valueOf).toList();
    }

}
