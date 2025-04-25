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
        if (patientRecords.isEmpty()) {
            patientRecords.add(record);
        } else if (patientRecords.get(patientRecords.size()-1).getTimestamp() <= record.getTimestamp()) { //often, the record will be at the same time or later than the last record, then it can just be added
            this.patientRecords.add(record);
        } else {
            //if not, insert the record at the correct index using findTimeIndex
            //System.out.println("adding");
            patientRecords.add(findTimeIndex(record.getTimestamp(), 0, patientRecords.size()-1, false), record);
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
     * @param firstIndex    determines whether to give the first index with the timestamp or the last if multiple
     * @return  index at which the time is equal to the target time(or the smallest alternative)
     */
    /**private int findTimeIndex (long timestamp, int min, int max, boolean firstIndex) {

        int middle = min;
        boolean found=false;
        while (max >= min && !found) {
            middle = (max-min)/2;
            long middleValue = patientRecords.get(middle).getTimestamp();
            if (middleValue > timestamp) {
                max = middle -1;
            } else if (middleValue < timestamp) {
                min = middle+1;
            } else { //if its an exact match
                found = true;
            }
        }
        if (firstIndex) {
            while (patientRecords.get(middle).getTimestamp() >= timestamp) {
                middle--;
            }
            return middle+1;
        } else {
            while (patientRecords.get(middle).getTimestamp() < timestamp) {
                middle++;
            }
            return middle-1;
        }
    }
     **/
    private int findTimeIndex(long timestamp, int minIndex, int maxIndex, boolean firstIndex) {
        int mid = 0;
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
        //the while loop stopped when min>max.
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
        int startIndex = findTimeIndex(startTime, 0, patientRecords.size()-1, true);
        int endIndex = findTimeIndex(endTime, startIndex, patientRecords.size()-1, false);
        for (int i = startIndex; i <= endIndex; i++) {
            records.add(patientRecords.get(i));
        }
        return records;
    }

    public int getPatientId() {
        return patientId;
    }
}
