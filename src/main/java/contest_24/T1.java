package contest_24;

import framework.Framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class T1 {
    public static void main(String[] args) {
        final int level = 1;
        for (int i = 0; i <= 5; i++) {

            var lines = Framework.readInput(level, i).split("\r\n");
            int numberOfLines = Integer.parseInt(lines[0]);
            var output = new ArrayList<List<Integer>>();
            for (int j = 1; j <= numberOfLines; j++) {
                output.add(getDirNumbers(lines[j]));
            }
            var stringBuilder = new StringBuilder();
            for (List<Integer> integers : output) {
                stringBuilder.append(integers.get(0)).append(" ").append(integers.get(1)).append(" ").append(integers.get(2)).append(" ").append(integers.get(3)).append("\n");
            }
                Framework.writeOutput(level, i, stringBuilder.toString());
        }
    }

    private static List<Integer> getDirNumbers(String line) {
        int numberW = 0;
        int numberD = 0;
        int numberS = 0;
        int numberA = 0;

        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == 'W') {
                numberW++;
            } else if (line.charAt(i) == 'A') {
                numberA++;
            } else if (line.charAt(i) == 'D') {
                numberD++;
            } else if (line.charAt(i) == 'S') {
                numberS++;
            }
        }

        return List.of(numberW, numberD, numberS, numberA);
    }
}
