package edu.oregonstate.fastqcompressor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Created by tanner on 4/12/17.
 */
public class GUI extends JFrame {

    private JLabel labelOperation;
    private JLabel labelInput;
    private JLabel labelOutput;
    private JLabel labelStatus;

    private JButton buttonSelectInput;
    private JButton buttonExecute;
    private JCheckBox checkboxGzip;

    public GUI() {
        setContentPane(buildContentPanel());
        addActionListeners();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JPanel buildContentPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(northPanel(), BorderLayout.NORTH);
        panel.add(centerPanel(), BorderLayout.CENTER);
        panel.add(southPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel northPanel() {
        final JPanel panel = new JPanel();
        panel.add(new JLabel("FASTQ Compression Utility"));
        return panel;
    }

    private JPanel centerPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        labelOperation = new JLabel("Operation: ");
        panel.add(labelOperation);

        labelInput = new JLabel("Input: ");
        panel.add(labelInput);

        labelOutput = new JLabel("Output: ");
        panel.add(labelOutput);

        labelStatus = new JLabel("Status:");
        panel.add(labelStatus);

        return panel;
    }

    private JPanel southPanel() {
        final JPanel panel = new JPanel();

        buttonSelectInput = new JButton("Select input file");
        panel.add(buttonSelectInput);

        buttonExecute = new JButton("Execute");
        buttonExecute.setEnabled(false);
        panel.add(buttonExecute);

        checkboxGzip = new JCheckBox("gzip");
        panel.add(checkboxGzip);

        return panel;
    }

    private void addActionListeners() {
        buttonSelectInput.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "FASTQ & Compressed FASTQ files", "fastq", "fuc1", "fuc2", "fuc3", "fuc4", "gfuc1", "gfuc2", "gfuc3", "gfuc4");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(buttonSelectInput);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                if(chooser.getSelectedFile().getName().endsWith(".fastq")) {
                    ApplicationState.setOperation(ApplicationState.Operation.COMPRESS);
                    labelOperation.setText("Operation: Compress");

                    checkboxGzip.setEnabled(true);
                    checkboxGzip.setSelected(false);
                    ApplicationState.setGzipEnabled(false);

                    ApplicationState.setInputFile(chooser.getSelectedFile().getAbsolutePath());
                } else {
                    ApplicationState.setOperation(ApplicationState.Operation.DECOMPRESS);
                    labelOperation.setText("Operation: Decompress");

                    ApplicationState.setInputFile(fixInputFile(chooser.getSelectedFile().getAbsolutePath()));
                    System.out.println(ApplicationState.getInputFile());

                    checkboxGzip.setEnabled(false);
                    checkboxGzip.setSelected(ApplicationState.getInputFile().endsWith(".gfuc"));
                    ApplicationState.setGzipEnabled(ApplicationState.getInputFile().endsWith(".gfuc"));
                }

                labelInput.setText("Input: " + new File(ApplicationState.getInputFile()).getName());

                ApplicationState.determineOutputFile();
                labelOutput.setText("Output: " + new File(ApplicationState.getOutputFile()).getName());

                labelStatus.setText("Status: Ready");

                buttonExecute.setEnabled(true);
            }
        });

        buttonExecute.addActionListener(e -> {
            labelStatus.setText("Status: Running");
            buttonExecute.setEnabled(false);

            try {
                OperationExecutor.run();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            labelStatus.setText("Status: Done");
        });

        checkboxGzip.addActionListener(e -> {
            ApplicationState.setGzipEnabled(checkboxGzip.isSelected());

            ApplicationState.determineOutputFile();
            labelOutput.setText("Output: " + new File(ApplicationState.getOutputFile()).getName());
        });
    }

    private static String fixInputFile(final String file) {
        if(!file.matches(".*[1-4]"))
            throw new RuntimeException("bad input file selected");
        return file.substring(0, file.length() - 1);
    }

}
