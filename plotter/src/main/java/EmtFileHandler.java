import java.io.*;

public class EmtFileHandler {
    public static void convert(String inputFilePath) throws IOException {
        String outputFilePath = inputFilePath.replace(".emt", ".csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            int blanks = 0, blanksToSkip = 3;

            while ((line = reader.readLine()) != null) {
                if ((line.isBlank() || line.isEmpty()) && blanks < blanksToSkip) {
                    blanks++;
                    continue;
                }

                if (blanks < blanksToSkip) {
                    continue;
                }

                String written = line.replaceAll("\t", ",") + "\n";
                written = written.replaceAll(" ", "");

                if(written.isEmpty() || written.isBlank()) continue;

                writer.write(written);
            }
        }
    }
}
