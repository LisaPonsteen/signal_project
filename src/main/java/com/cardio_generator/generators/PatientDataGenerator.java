package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for data generators.
 * Data generators generate health data for patients and send it out
 */
public interface PatientDataGenerator {

    /**
     * Generates the data for a given patient and sends it out according to the specified output strategy
     *
     * @param patientId ID of the patient
     * @param outputStrategy    Strategy for outputting the data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
