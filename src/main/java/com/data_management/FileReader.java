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

        if (files == null || files.length == 0) {
            System.out.println("No files in directory");
            return;
        }

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
     * parses the lines to variable values, and puts it into data storage
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
        line = line.replaceAll("\\s+", ""); //remove all whitespace
        line = line + ","; //put a , to the end of the line, so a , is always the end of a label-value pair

        String label = "";
        String value = "";
        boolean readValue = false;
        //System.out.println(line);

            for (char nextChar: line.toCharArray()) {
                if (nextChar == ':') {
                    readValue = true;
                    //System.out.println(label);
                } else if (nextChar == ',') { //so at the end of value and type pair:
                    if (label.equals("PatientID"))
                        patientId = Integer.parseInt(value);
                    else if (label.equals("Timestamp"))
                            timestamp = Long.parseLong(value.replace("L", "")); //replace L suffix that (often there to let java know it is type long, but invalid for parsing)
                    else if (label.equals("Label"))
                            recordType = value;
                    else if (label.equals("Data")) {
                        //System.out.println("in data loop");
                        if (recordType.equals("alert")) {
                            if (value.equals("triggered")) {
                                measurementValue = 0.0;
                                //if the alert is resolved, measurementValue wil be NaN and the alert won't be pushed
                            } else {
                                return;
                            }
                        } else {
                            measurementValue = Double.parseDouble(value);
                        }
                    } else
                        System.out.println("Unknown data variable" + label);

                    label = "";
                    value = "";
                    readValue = false;
                } else if (readValue)
                    value += nextChar;
                else
                    label += nextChar;
            }

        if ((Double.isNaN(measurementValue)&& !recordType.equals("alert")))
            System.out.println("did not parse measurement value correctly: " + measurementValue);
        else if (recordType.equals("none"))
            System.out.println("did not parse record type correctly: " + label);
        else if (patientId <= 0)
            System.out.println("did not parse patient ID correctly: " + patientId);
        else if (timestamp <= 0)
            System.out.println("did not parse timestamp correctly: " + timestamp);
        else
            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
    }
}
