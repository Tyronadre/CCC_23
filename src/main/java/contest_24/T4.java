package contest_24;

import contest_24.dataClasses.Lawn;
import contest_24.dataClasses.Path;

import java.util.ArrayList;
import java.util.Arrays;

public class T4 {
    public static void main(String[] args) {
        final int level = 4;
        for (int i = 0; i <= 0; i++) {
            var lines = framework.Framework.readInput(level, i).split("\r\n");
            int numberOfLines = Integer.parseInt(lines[0]);

            var output = new ArrayList<String>();
            lines = Arrays.copyOfRange(lines, 1, lines.length);
            //get an array with only lawn data for this subinput
            for (int subInput = 0; subInput < 1; subInput++) {
                var lawnSize = lines[0].split(" ");
                var lawn = new Lawn(Integer.parseInt(lawnSize[0]), Integer.parseInt(lawnSize[1]));
                var lawnData = Arrays.copyOfRange(lines, 1, 1 + Integer.parseInt(lawnSize[1]));
                lawn.parse(lawnData);
                //createPath
                Path p = Path.createPath(lawn);

                System.out.println("Path: " + p.toString());
                System.out.println("Path: " + p.getPathString());
                System.out.println("Lawn: " + lawn);
                System.out.println("val path: " + p.validate());
                System.out.println("val lawn: " + lawn.validatePath(p));

                if (p.validate() && lawn.validatePath(p)) {
                    output.add(p.getPathString());
                } else {
                    output.add("INVALID");
                    System.err.println("INVALID: \n" + p.getPathString() + "\n" + lawn + "\n" + p.validate() + "\n" + lawn.validatePath(p) + "\n\n");

                }

            }


            var stringBuilder = new StringBuilder();
            for (var path : output) {
                stringBuilder.append(path).append("\n");
            }
            framework.Framework.writeOutput(level, i, stringBuilder.toString());
        }
    }
}
