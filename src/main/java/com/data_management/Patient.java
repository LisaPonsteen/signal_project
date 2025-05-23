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
    private long scannedUpTo;

    public long getScannedUpTo() {
        return scannedUpTo;
    }
    public void setScannedUpTo(long scannedUpTo) {
        this.scannedUpTo = scannedUpTo;
    }

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
     * checks if the newRecord has a duplicate regarding timestamp and record type.
     * if so, it removes the duplicate record and adds the new one (so updates the measurement value)
     * @param newRecord the new record
     * @return returns true if a record is updated
     */
    public boolean checkDuplicate(PatientRecord newRecord) {
        int i = findTimeIndex(newRecord.getTimestamp(), 0, patientRecords.size()-1, true); //get the first index with the specific timestamp
        while (i<patientRecords.size() && patientRecords.get(i).getTimestamp() == newRecord.getTimestamp()) {
            if (patientRecords.get(i).getRecordType().equals(newRecord.getRecordType())) {
                patientRecords.remove(i);
                patientRecords.add(i, newRecord);
                return true;
            }
            i++;
        }
        return false;
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
        if (patientRecords.isEmpty()) {
            patientRecords.add(record);
            return;
        }

        //check if there are records with the same timestamp and record type. if so, edit their measurement value
        if (checkDuplicate(record))
            return;
        //else add record to patientRecords (at right index)

        //if the timestamp is later than the last record, then it can be added to the end of the list
        if (patientRecords.get(patientRecords.size()-1).getTimestamp() < record.getTimestamp()) {
            this.patientRecords.add(record);
        } else {
            //else insert the record at the correct index by using findTimeIndex
            patientRecords.add(findTimeIndex(record.getTimestamp(), 0, patientRecords.size()-1, false), record);
        }
    }


    /**
     * Finds the index in the list of records such that the time at that index is equal to the given time(or the smallest alternative)
     * Assumes patientRecords is correctly sorted based on timestamps
     * uses a binary search approach
     *
     * @param timestamp target time to search for
     * @param minIndex   minimal index it could be at (often 0)
     * @param maxIndex   maximal index it could be at (often size of list)
     * @param firstIndex    determines whether to give the first index with the timestamp or the last if multiple
     * @return  index at which the time is equal to the target time(or the smallest alternative)
     */
    private int findTimeIndex(long timestamp, int minIndex, int maxIndex, boolean firstIndex) {
        int mid;
        int min = minIndex;
        int max = maxIndex;
        boolean found = false;
        int foundIndex = 0;

        while (min <= max && !found) {
            mid = min + (max - min) / 2;
            long thisTimestamp = patientRecords.get(mid).getTimestamp();

            if (thisTimestamp == timestamp) {
                foundIndex = mid;
                found = true;
            } else if (thisTimestamp < timestamp) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }

        //if an index is found with target timestamp, check to find the first/last index with that target time
        if (found) {
            while (0 <= foundIndex && foundIndex <=maxIndex && patientRecords.get(foundIndex).getTimestamp() == timestamp) {
                if (firstIndex) {
                    foundIndex--;
                } else {
                    foundIndex++;
                }
            }
            if (firstIndex) {
                return Math.max(0,Math.min(foundIndex+1, maxIndex));
            } else {
                return Math.max(0,foundIndex-1);
            }
        }

        // if no exact match found, return the smallest index > timestamp (for firstIndex)
        //(because the while loop stopped when min>max)
        if (firstIndex) {
            return Math.max(0,max);
        }
        // or largest index < timestamp (for !firstIndex)
        return Math.min(maxIndex,min);
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
        int startIndex = findTimeIndex(startTime, 0, patientRecords.size()-1, true); //get the first index with the specific timestamp
        int endIndex = findTimeIndex(endTime, startIndex, patientRecords.size()-1, false); //get the last index with the specific timestamp
        for (int i = startIndex; i <= endIndex; i++) {
            records.add(patientRecords.get(i));
        }
        return records;
    }

    public int getPatientId() {
        return patientId;
    }
}
