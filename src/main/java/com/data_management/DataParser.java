package com.data_management;

/**
 * this class is responsible for parsing a line into a record before adding it to the data storage.
 * It can be used by any DataReader.
 */
public class DataParser {

    /**
     * Parses a lines with a patient record to variable values, and puts it into data storage
     * Assumes that lines look like:
     * Patient ID: 123, Timestamp: 1713345600000, Label: Saturation, Data: 78
     *
     * @param line  a line from the file
     * @param dataStorage   data storage where the data should be added to
     */
    public static void parseLine(String line, DataStorage dataStorage) {

        //variable initialization with invalid data
        int patientId = -1;
        double measurementValue = Double.NaN;
        String recordType = null;
        long timestamp = -1;

        //pre-process the line
        line = line.trim();
        line = line.replaceAll("\\s+", ""); //remove all whitespace
        line = line + ","; //put a comma to the end of the line so , is always the indicator of the end of a label-value pair

        StringBuilder label = new StringBuilder();
        StringBuilder value = new StringBuilder();
        boolean readValue = false;

        for (char nextChar: line.toCharArray()) {
            if (nextChar == ':') {
                readValue = true;
            } else if (nextChar == ',') { //at the end of every label-value pair, update the corresponding variable
                if (label.toString().equals("PatientID")) {
                    try {
                        patientId = Integer.parseInt(value.toString());
                    } catch (NumberFormatException e) {
                        System.out.println("could not parse patient id: " + value);
                    }
                }else if (label.toString().equals("Timestamp")) {
                    try {
                        timestamp = Long.parseLong(value.toString().replace("L", "")); //replace L suffix that (often there to let java know it is type long, but invalid for parsing)
                    } catch (NumberFormatException e) {
                        System.out.println("could not parse timestamp: " + value);
                    }
                }else if (label.toString().equals("Label"))
                    recordType = value.toString();
                else if (label.toString().equals("Data")) {

                    //if the type is an alert: if it is triggered, we will put measurementValue = 0.0. If resolved, the alert won't be pushed
                    if (value.toString().equals("triggered")) {
                            measurementValue = 0.0;
                    } else if (value.toString().equals("resolved")) {
                            return;
                    } else {
                        try {
                            measurementValue = Double.parseDouble(value.toString());
                        } catch (NumberFormatException e) {
                            System.out.println("could not parse measurement value: " + value);
                        }
                    }
                } else
                    System.out.println("Unknown data variable" + label);

                //after label-value pair is parsed, reset the label and value to ""
                label.delete(0, label.length());
                value.delete(0, value.length());
                readValue = false;
            } else if (readValue)
                value.append(nextChar);
            else
                label.append(nextChar);
        }
        //System.out.println("ended parsing");

        //check if any of the variables isn't correctly set. If everything is valid, make a new record and add it to dataStorage

        if (checkValidity(recordType, measurementValue, patientId, timestamp, dataStorage)) {
            //System.out.println("adding to data storage: patient ID: " + patientId + " measurement value: " + measurementValue+ " record type: " + recordType + " timestamp: " + timestamp);
            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
            //System.out.println("added to data storage");
        } else {
            System.out.println(" Line - " + line);
        }
    }
    private static boolean checkValidity(String recordType, double measurementValue, int patientId, long timestamp, DataStorage dataStorage) {
        if (recordType == null) {
            System.out.print("did not parse record type correctly.");
            return false;
        }else if ((Double.isNaN(measurementValue) && !recordType.equals("alert"))) {
            System.out.print("did not parse measurement value correctly.");
            return false;
        }else if (patientId <= 0) {
            System.out.print("did not parse patient ID correctly.");
            return false;
        }else if (timestamp <= 0) {
            System.out.print("did not parse timestamp correctly.");
            return false;
        }
        return true;
    }
}
