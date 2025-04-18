package com.data_management;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a patient and manages their medical records.
 * This class stores patient-specific data, allowing for the addition and
 * retrieval
 * of medical records based on specified criteria.
 */
public class Patient {
    private int patientId;
    private List<PatientRecord> patientRecords;

    /**
     * Constructs a new Patient with a specified ID.
     * Initializes an empty list of patient records.
     *
     * @param patientId the unique identifier for the patient
     */
    public Patient(int patientId) {
        this.patientId = patientId;
        this.patientRecords = new ArrayList<>();
    }

    /**
     * Adds a new record to this patient's list of medical records.
     * The record is created with the specified measurement value, record type, and
     * timestamp.
     *
     * @param measurementValue the measurement value to store in the record
     * @param recordType       the type of record, e.g., "HeartRate",
     *                         "BloodPressure"
     * @param timestamp        the time at which the measurement was taken, in
     *                         milliseconds since UNIX epoch
     */
    public void addRecord(double measurementValue, String recordType, long timestamp) {
        PatientRecord record = new PatientRecord(this.patientId, measurementValue, recordType, timestamp);
        if (patientRecords.getLast().getTimestamp() < record.getTimestamp()) {
            this.patientRecords.add(record);
        } else {
            //insert the record at the correct index using findTimeIndex
            patientRecords.add(findTimeIndex(record.getTimestamp(), 0, patientRecords.size()), record);
        }
    }


    /**
     * Finds the index in the list of records such that the time at that index is equal to the given time(or the smallest alternative)
     * Assumes patientRecords is correctly sorted based on timestamps
     * uses a binary search approach
     *
     * @param timestamp target time to search for
     * @param min   minimal index it could be at (often 0)
     * @param max   maximal index it could be at (often size of list)
     * @return  index at which the time is equal to the target time(or the smallest alternative)
     */
    private int findTimeIndex (long timestamp, int min, int max) {
        int middle = (max-min)/2;
        long middleValue = patientRecords.get(middle).getTimestamp();

        while (middleValue != timestamp || max - min == 2) { //min 1 middle 2 max3
            if (middleValue > timestamp) {
                min = middle;
            } else {
                max = middle;
            }
            middle = (max-min)/2;
            middleValue = patientRecords.get(middle).getTimestamp();
        }
        return middle;
    }

    /**
     * Retrieves a list of PatientRecord objects for this patient that fall within a
     * specified time range.
     *
     * @param startTime the start of the time range, in milliseconds since UNIX
     *                  epoch
     * @param endTime   the end of the time range, in milliseconds since UNIX epoch
     * @return a list of PatientRecord objects that fall within the specified time
     *         range
     */
    public List<PatientRecord> getRecords(long startTime, long endTime) {
        List<PatientRecord> records = new ArrayList<>();
        int startIndex = findTimeIndex(startTime, 0, patientRecords.size());
        int endIndex = findTimeIndex(endTime, startIndex, patientRecords.size());
        for (int i = startIndex; i <= endIndex; i++) {
            records.add(patientRecords.get(i));
        }
        return records;
    }

    public int getPatientId() {
        return patientId;
    }
}
