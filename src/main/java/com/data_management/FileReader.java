package com.data_management;

import com.alerts.AlertGenerator;
import com.alerts.Alert;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FileReader implements DataReader {
    private String outputDir;
    public FileReader(String outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Reads data from an output file generated using the --output file:<output_dir> argument
     *
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the data
     */
    public void readData(DataStorage dataStorage) throws IOException {
        //parse data from the specified directory.
        //Ensure that the data read is accurately passed into the DataStorage for further processing.


        File dir = new File(outputDir);
        File[] files = dir.listFiles();

        for (File file : files) {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                parseLine(line, dataStorage);
            }
            scanner.close();
        }
    }


    /**
     * parses the lines to variable values, and puts it into the data storage
     *
     * @param line  a line from the file
     * @param dataStorage   data storage where the data should be added to
     */
    public void parseLine(String line, DataStorage dataStorage) {
        //lines look like: Patient ID: 123, Timestamp: 1713345600000, Label: heart_rate, Data: 78

        int patientId = -1;
        Double measurementValue = Double.NaN;
        String recordType = "none";
        long timestamp = -1;

        line = line.trim();
        Scanner lineScanner = new Scanner(line);

            String label = "";
            String value = "";
            boolean readValue = false;
            boolean triggered = false;

            while (lineScanner.hasNext()) {
                String nextChar = lineScanner.next();
                if (readValue)
                    value += nextChar;
                else
                    label += nextChar;
                if (nextChar.equals(":")) {
                    readValue = true;
                } else if (nextChar.equals(",")) {
                    switch (label) {
                        case "PatientID:":
                            patientId = Integer.parseInt(value);
                        case ",Timestamp:":
                            timestamp = Long.parseLong(value);
                        case ",Label:":
                            recordType = value;
                        case ",Data:":
                            if (recordType.equals("alert")) {
                                if (value.equals("triggered")) {
                                    measurementValue = 0.0;
                                    //if the alert is resolved, measurementValue wil be NaN and the alert won't be pushed
                                }
                            } else {
                                measurementValue = Double.parseDouble(value);
                            }
                        default:
                            System.out.println("Unknown data variable" + label);
                    }
                    label = "";
                    value = "";
                    readValue = false;
                }
            lineScanner.close();
        }

        if ((Double.isNaN(measurementValue)&& !recordType.equals("alert"))|| recordType.equals("none") || patientId <= 0 || timestamp <= 0) {
            System.out.println("did not find all data correctly");
        } else {
            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
        }
    }
}
