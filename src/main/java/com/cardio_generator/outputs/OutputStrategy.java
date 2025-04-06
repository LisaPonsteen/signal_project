package com.cardio_generator.outputs;

/**
 * An interface that determines the output strategies for patient data
 * Strategies can be delivering the data in the terminal, a file, etc.
 */
public interface OutputStrategy {

    /**
     *  Outputs data for a single patient
     *
     * @param patientId ID of the patient
     * @param timestamp Timestamp at which the data was generated
     * @param label     Label describing the type of data
     * @param data      The data of the patient
     */
    void output(int patientId, long timestamp, String label, String data);
}
