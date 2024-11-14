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

        //Labels
        JPanel panel = new JPanel();

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

        //Input Fields
        JCheckBox multifileCheckBox = new JCheckBox();

        JTextField filePathField = new JTextField();

        JTextField accFilePathField = new JTextField();
        JTextField angFilePathField = new JTextField();
        JTextField angVelFilePathField = new JTextField();

        JTextField accXColField = new JTextField();
        JTextField accYColField = new JTextField();
        JTextField accZColField = new JTextField();

        JTextField angXColField = new JTextField();
        JTextField angYColField = new JTextField();
        JTextField angZColField = new JTextField();

        JTextField angVelXColField = new JTextField();
        JTextField angVelYColField = new JTextField();
        JTextField angVelZColField = new JTextField();

        JTextField indexColField = new JTextField();

        JCheckBox freeCheckBox = new JCheckBox();

        JCheckBox plotXCheckBox = new JCheckBox();
        JCheckBox plotYCheckBox = new JCheckBox();
        JCheckBox plotZCheckBox = new JCheckBox();

        JButton setConfig = new JButton("Crea grafico");

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

                try {
                    setVisible(false);

                    XYSeriesCollection[] dataset = CSVInterpeter.read_dataset(config);

                    AccelerometerPlot p = new AccelerometerPlot(dataset[0], dataset[1]);
                } catch (IOException | CsvValidationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        accFilePathField.setEnabled(false);
        angFilePathField.setEnabled(false);
        angVelFilePathField.setEnabled(false);

        add(panel);

        panel.setLayout(new GridLayout(20, 2));
        //Multifile
        panel.add(multifileLabel);
        panel.add(multifileCheckBox);
        //single file path
        panel.add(filePathLabel);
        panel.add(filePathField);
        //multiple files paths
        panel.add(accFilePathLabel);
        panel.add(accFilePathField);
        panel.add(angFilePathLabel);
        panel.add(angFilePathField);
        panel.add(angVelFilePathLabel);
        panel.add(angVelFilePathField);
        //Acceleration columns
        panel.add(accColLabel);
        panel.add(accXColField);
        panel.add(new JLabel());
        panel.add(accYColField);
        panel.add(new JLabel());
        panel.add(accZColField);
        //Angles columns
        panel.add(angColLabel);
        panel.add(angXColField);
        panel.add(new JLabel());
        panel.add(angYColField);
        panel.add(new JLabel());
        panel.add(angZColField);
        //Angular Velocity columns
        panel.add(angVelColLabel);
        panel.add(angVelXColField);
        panel.add(new JLabel());
        panel.add(angVelYColField);
        panel.add(new JLabel());
        panel.add(angVelZColField);
        //Index column
        panel.add(indexColLabel);
        panel.add(indexColField);
        //Use free values
        panel.add(freeLabel);
        panel.add(freeCheckBox);
        //render axes
        panel.add(plotXLabel);
        panel.add(plotXCheckBox);
        panel.add(plotYLabel);
        panel.add(plotYCheckBox);
        panel.add(plotZlabel);
        panel.add(plotZCheckBox);
        //set config
        panel.add(new JLabel());
        panel.add(setConfig);

        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
