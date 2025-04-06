package com.cardio_generator.generators;

import java.util.Random; //removed empty lines between the imports
import com.cardio_generator.outputs.OutputStrategy;

/** Generates and processes alert states for patients */
public class AlertGenerator implements PatientDataGenerator {

    //if it doesn't conflict with other code, I suggest making it a private field instead of public.
    private static final Random randomGenerator = new Random();

    //fields should be lowerCamelCase, so I changed AlertStates to alertStates
    private boolean[] alertStates; // false = resolved, true = pressed

    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates an alert for a patient and outputs the alert
     * An alert is resolved or pressed based on probabilities
     *
     * @param patientId       ID of the patient
     * @param outputStrategy  The strategy for outputting data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                //local variables should be lowerCamelCase, so changed Lambda to lambda
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
