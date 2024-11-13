import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class EMTtoCSVConverter {

    /**
     * Metodo per convertire un file .emt in formato .csv.
     *
     * @param inputFilePath  il percorso del file .emt di input
     * @param outputFilePath il percorso del file .csv di output
     * @throws IOException   in caso di errore di lettura o scrittura
     */
    public void convert(String inputFilePath, String outputFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            boolean dataSection = false; // Flag per rilevare quando inizia la sezione dei dati
            boolean skipHeaderLine = true; // Flag per saltare la prima riga con "Frame"

            // Ciclo per leggere ogni riga del file .emt
            while ((line = reader.readLine()) != null) {
                // Ignora le righe di intestazione che non iniziano con "Frame"
                if (!dataSection) {
                    if (line.trim().startsWith("Frame")) {
                        dataSection = true; // Inizia la sezione della tabella
                        skipHeaderLine = true; // Imposta per saltare la prima riga di dati
                    }
                    continue; // Salta tutte le righe prima di "Frame"
                }

                // Se siamo nella sezione dei dati, salta la prima riga
                if (skipHeaderLine) {
                    skipHeaderLine = false; // Salta la riga iniziale dopo "Frame"
                    continue;
                }

                // Scrivi la riga dei dati nel file CSV, sostituendo i tab con virgole
                writer.write(line.replaceAll("\t", ",") + "\n");
            }
        }
    }
}
