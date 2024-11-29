import com.github.psambit9791.jdsp.filter.Butterworth;
import com.opencsv.exceptions.CsvValidationException;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;
public class ConfigGUI extends JFrame {

    public ConfigGUI() {
        super("Configurazione plot");

        //Panels
        JPanel panel = new JPanel();
        JPanel formPanel = new JPanel();
        JPanel feedbackPanel = new JPanel();

        //Form Labels
        JLabel multifileLabel = new JLabel("Utilizzare file multipli:");

        JLabel filePathLabel = new JLabel("Path del file unico:");

        JLabel accFilePathLabel = new JLabel("Path del file con i dati di accelerazione:");
        JLabel angFilePathLabel = new JLabel("Path del file con i dati di angolazione:");
        JLabel angVelFilePathLabel = new JLabel("Path del file con i dati di velocità angolare:");

        JLabel accColLabel = new JLabel("Nomi delle colonne contenenti le accelerazioni (ordinate XYZ)");
        JLabel angColLabel = new JLabel("Nomi delle colonne contenenti le angolazioni (ordine XYZ)");
        JLabel angVelColLabel = new JLabel("Nomi delle colonne contenenti le veloticà angolari (ordine XYZ)");
        JLabel indexColLabel = new JLabel("Nome della colonna indice (es. Frame)");

        JLabel freeLabel = new JLabel("Calcolo dei valori sul sistema Locale");

        JLabel plotXLabel = new JLabel("Rappresentazione valori asse X");
        JLabel plotYLabel = new JLabel("Rappresentazione valori asse Y");
        JLabel plotZlabel = new JLabel("Rappresentazione valori asse Z");

        //Form Input Fields
        JCheckBox multifileCheckBox = new JCheckBox();

        JTextField filePathField = new JTextField();

        JTextField accFilePathField = new JTextField();
        JTextField angFilePathField = new JTextField();
        JTextField angVelFilePathField = new JTextField();

        JTextField accXColField = new JTextField("GSensor.X");
        JTextField accYColField = new JTextField("GSensor.Y");
        JTextField accZColField = new JTextField("GSensor.Z");

        JTextField angXColField = new JTextField("GSensor.X");
        JTextField angYColField = new JTextField("GSensor.Y");
        JTextField angZColField = new JTextField("GSensor.Z");

        JTextField angVelXColField = new JTextField("GSensor.X");
        JTextField angVelYColField = new JTextField("GSensor.Y");
        JTextField angVelZColField = new JTextField("GSensor.Z");

        JTextField indexColField = new JTextField("Frame");

        JCheckBox freeCheckBox = new JCheckBox();

        JCheckBox plotXCheckBox = new JCheckBox();
        JCheckBox plotYCheckBox = new JCheckBox();
        JCheckBox plotZCheckBox = new JCheckBox();

        JButton setConfig = new JButton("Crea grafico");

        //Feedback Section
        JTextArea feedbackArea = new JTextArea();
        JScrollPane scroll = new JScrollPane(feedbackArea);

        multifileCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean set = multifileCheckBox.isSelected();

                filePathField.setEnabled(!set);

                accFilePathField.setEnabled(set);
                angFilePathField.setEnabled(set);
                angVelFilePathField.setEnabled(set);
            }
        });

        setConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Config config = new Config(
                        multifileCheckBox.isSelected(),
                        filePathField.getText(),
                        accFilePathField.getText(), angFilePathField.getText(), angVelFilePathField.getText(),
                        accXColField.getText(), accYColField.getText(), accZColField.getText(),
                        angXColField.getText(), angYColField.getText(), angZColField.getText(),
                        angVelXColField.getText(), angVelYColField.getText(), angVelZColField.getText(),
                        indexColField.getText(),
                        "","","",
                        freeCheckBox.isSelected(),
                        plotXCheckBox.isSelected(),
                        plotYCheckBox.isSelected(),
                        plotZCheckBox.isSelected()
                );

                if (config.isMultifile()
                        && (config.getAccelerationFilePath().isBlank()
                        || config.getAnglesFilePath().isBlank()
                        || config.getAngularVelocityFilePath().isBlank())) {
                    feedbackArea.setText("L'opzione per l'utilizzo di molteplici file è stata selezionata, ma almeno uno dei tre percorsi è stato lasciato vuoto.");
                    return;
                }

                if (!config.isMultifile() && config.getFilePath().isBlank()) {
                    feedbackArea.setText("L'opzione per l'utilizzo di un file singolo è stata selezionata, ma il percorso del file è stato lasciato vuoto.");
                    return;
                }

                if (config.getAccColX().isBlank() || config.getAccColY().isBlank() || config.getAccColZ().isBlank()) {
                    feedbackArea.setText("Il nome di almeno una delle colonne dell'accelerazione è stato lasciato vuoto.");
                    return;
                }
                if (config.getAngColX().isBlank() || config.getAngColY().isBlank() || config.getAngColZ().isBlank()) {
                    feedbackArea.setText("Il nome di almeno una delle colonne dell'angolazione è stato lasciato vuoto.");
                    return;
                }
                if (config.getAngVelColX().isBlank() || config.getAngVelColY().isBlank() || config.getAngVelColZ().isBlank()) {
                    feedbackArea.setText("Il nome di almeno una delle colonne della velocità angolare è stato lasciato vuoto.");
                    return;
                }

                if (config.getIndexCol().isBlank()) {
                    feedbackArea.setText("Il nome della colonna indice è stato lasciato vuoto.");
                }

                if (!config.isPlotX() && !config.isPlotY() && !config.isPlotZ()) {
                    feedbackArea.setText("Nessuno degli assi è stato selezionato, questo causerebbe un grafico vuoto!");
                    return;
                }

                try {
                    boolean filtered = true;
                    Data data = CSVInterpeter.read_dataset(config, true);

                    if (config.isFree())
                        data.makeFree();
                    if (filtered) {
                        Butterworth b = new Butterworth(100);

                        if (config.isPlotX()) {
                            data.filter(Data.Axis.X, Data.Type.ACCELERATION, b, 4, 10);
                            data.filter(Data.Axis.X, Data.Type.ANG_VELOCITY, b, 4, 10);
                        }
                        if (config.isPlotY()) {
                            data.filter(Data.Axis.Y, Data.Type.ACCELERATION, b, 4, 10);
                            data.filter(Data.Axis.Y, Data.Type.ANG_VELOCITY, b, 4, 10);
                        }
                        if (config.isPlotZ()) {
                            data.filter(Data.Axis.Z, Data.Type.ACCELERATION, b, 4, 10);
                            data.filter(Data.Axis.Z, Data.Type.ANG_VELOCITY, b, 4, 10);
                        }
                    }

                    XYSeriesCollection[] dataset = data.getDataset(config);

                    //dataset[0].addSeries(constant("-G", dataset[0].getItemCount(0), -9.80665));
                    //dataset[0].addSeries(constant("+G", dataset[0].getItemCount(0), 9.80665));
                    //dataset[1].addSeries(constant("-G", dataset[1].getItemCount(0), -9.80665));

                    AccelerometerPlot p = new AccelerometerPlot(dataset[0], dataset[1]);

                    setVisible(false);
                } catch (IOException | CsvValidationException ex) {
                    feedbackArea.setText("C'è stato un errore:\n" + ex.getMessage());
                }
            }
        });

        multifileCheckBox.setSelected(true);
        filePathField.setEnabled(false);

        plotXCheckBox.setSelected(true);
        plotYCheckBox.setSelected(true);
        plotZCheckBox.setSelected(true);

        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setPreferredSize(new Dimension(300, 600));

        add(panel);
        panel.setLayout(new BorderLayout());
        panel.add(formPanel, BorderLayout.WEST);
        panel.add(feedbackPanel, BorderLayout.EAST);

        formPanel.setLayout(new GridLayout(20, 2));
        //Multifile
        formPanel.add(multifileLabel);
        formPanel.add(multifileCheckBox);
        //single file path
        formPanel.add(filePathLabel);
        formPanel.add(filePathField);
        //multiple files paths
        formPanel.add(accFilePathLabel);
        formPanel.add(accFilePathField);
        formPanel.add(angFilePathLabel);
        formPanel.add(angFilePathField);
        formPanel.add(angVelFilePathLabel);
        formPanel.add(angVelFilePathField);
        //Acceleration columns
        formPanel.add(accColLabel);
        formPanel.add(accXColField);
        formPanel.add(new JLabel());
        formPanel.add(accYColField);
        formPanel.add(new JLabel());
        formPanel.add(accZColField);
        //Angles columns
        formPanel.add(angColLabel);
        formPanel.add(angXColField);
        formPanel.add(new JLabel());
        formPanel.add(angYColField);
        formPanel.add(new JLabel());
        formPanel.add(angZColField);
        //Angular Velocity columns
        formPanel.add(angVelColLabel);
        formPanel.add(angVelXColField);
        formPanel.add(new JLabel());
        formPanel.add(angVelYColField);
        formPanel.add(new JLabel());
        formPanel.add(angVelZColField);
        //Index column
        formPanel.add(indexColLabel);
        formPanel.add(indexColField);
        //Use free values
        formPanel.add(freeLabel);
        formPanel.add(freeCheckBox);
        //render axes
        formPanel.add(plotXLabel);
        formPanel.add(plotXCheckBox);
        formPanel.add(plotYLabel);
        formPanel.add(plotYCheckBox);
        formPanel.add(plotZlabel);
        formPanel.add(plotZCheckBox);
        //set config
        formPanel.add(new JLabel());
        formPanel.add(setConfig);

        feedbackPanel.add(scroll);

        setSize(1100, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
