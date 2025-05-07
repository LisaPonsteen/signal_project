package com.data_management;

/**
 * class responsible for parsing a line into a record and adding it to a data storage.
 * It can be used by any DataReader.
 */
public class DataParser {

    /**
     * Parses a lines with a patient record to variable values, and puts it into data storage
     * Assumes that lines look like: Patient ID: 123, Timestamp: 1713345600000, Label: Saturation, Data: 78
     *
     * @param line  a line from the file
     * @param dataStorage   data storage where the data should be added to
     */
    public static void parseLine(String line, DataStorage dataStorage) {
        //variable initialization
        int patientId = -1;
        Double measurementValue = Double.NaN;
        String recordType = "";
        long timestamp = -1;

        //pre-process the line
        line = line.trim();
        line = line.replaceAll("\\s+", ""); //remove all whitespace
        line = line + ","; //put a comma to the end of the line so , is always the indicator of the end of a label-value pair

        String label = "";
        String value = "";
        boolean readValue = false;

        for (char nextChar: line.toCharArray()) {
            if (nextChar == ':') {
                readValue = true;
                //System.out.println(label);

            } else if (nextChar == ',') { //at the end of every lable-value pair, update the corresponding variable
                if (label.equals("PatientID"))
                    patientId = Integer.parseInt(value);
                else if (label.equals("Timestamp"))
                    timestamp = Long.parseLong(value.replace("L", "")); //replace L suffix that (often there to let java know it is type long, but invalid for parsing)
                else if (label.equals("Label"))
                    recordType = value;
                else if (label.equals("Data")) {

                    //if the type is an alert: if it is triggered, we will put measurementValue = 0.0. If resolved, the alert won't be pushed
                    if (recordType.equals("alert")) {
                        if (value.equals("triggered")) {
                            measurementValue = 0.0;
                        } else {
                            return;
                        }
                    } else {
                        measurementValue = Double.parseDouble(value);
                    }
                } else
                    System.out.println("Unknown data variable" + label);

                //after label-value pair is parsed, reset the label and value to ""
                label = "";
                value = "";
                readValue = false;
            } else if (readValue)
                value += nextChar;
            else
                label += nextChar;
        }

        //check if any of the variables isn't correctly set. If everything is valid, make a new record and add it to dataStorage
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
