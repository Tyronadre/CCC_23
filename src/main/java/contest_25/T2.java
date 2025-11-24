package contest_25;

import java.util.ArrayList;

public class T2 {
    public static void main(String[] args) {
        final int level = 2;
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
        var place = 0;
        var time = 0;
        for (int i = 0; i < line.length(); i++) {
            if (Character.isDigit(line.charAt(i))) {
                int pace = line.charAt(i) - '0';
                time += pace;

                if (i - 1 > 0 && line.charAt(i - 1) == '-')
                    pace *= -1;

                if (pace < 0) place--;
                if (pace == 0) time += 1;
                if (pace > 0) place++;
            }
        }
        return place + " " + time;
    }
}
