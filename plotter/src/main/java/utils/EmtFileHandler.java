package utils;

import java.io.*;

public class EmtFileHandler {
    public static void convert(String inputFilePath) throws IOException {
        String outputFilePath = inputFilePath.replace(".emt", ".csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            int blanks = 0, blanksToSkip = 3;
            boolean isHeaders = true;

            while ((line = reader.readLine()) != null) {
                if ((line.isBlank() || line.isEmpty()) && blanks < blanksToSkip) {
                    blanks++;
                    continue;
                }

                if (blanks < blanksToSkip) {
                    continue;
                }

                String written = line.replaceAll("\t", ",") + "\n";
                if (isHeaders) {
                    written = capitalizeWords(written);
                    isHeaders = false;
                }
                written = written.replaceAll(" ", "");

                int lastCommaIndex = written.lastIndexOf(',');
                if (lastCommaIndex != -1) {
                    written = written.substring(0, lastCommaIndex) + written.substring(lastCommaIndex + 1);
                }
                if (written.lastIndexOf('\n') == -1) {
                    written = written + "\n";
                }

                if(written.isEmpty() || written.isBlank()) continue;

                writer.write(written);
            }
        }
    }

    private static String capitalizeWords(String input) {
        String[] words = input.trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
                result.append(" ");
            }
        }

        return result.toString();
    }
}
