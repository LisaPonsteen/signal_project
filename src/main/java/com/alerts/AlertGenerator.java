package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        List<PatientRecord> records = patient.getRecords(Long.MIN_VALUE, Long.MAX_VALUE);
        long lastTime = records.getFirst().getTimestamp();

        double lastDiastolicPressure = -1.0;
        int diastolicTrend = 0;
        boolean diastolicIncrease = false;
        boolean diastolicDecrease = false;

        double lastSystolicPressure = 1000;
        int systolicTrend = 0;
        boolean systolicIncrease = false;
        boolean systolicDecrease = false;

        Queue<Double> lastSaturations = new LinkedList<>();
        double saturation = 1000;
        Queue<Long> lastTimes = new LinkedList<>();

        Queue<Double> lastECGs = new LinkedList<>();
        int ECGSize = 5;
        int ecgPeakThreshold = 30;
        double averageECG = 0;

        for (PatientRecord record : records) {
            lastTime = record.getTimestamp();
            switch (record.getRecordType()) {
                case "SystolicPressure":
                    if (record.getMeasurementValue() - lastSystolicPressure > 10) {
                        if (!systolicIncrease) {
                            systolicTrend = 0;
                            systolicIncrease = true;
                            systolicDecrease = false;
                        }
                        systolicTrend++;
                    } else if (lastSystolicPressure - record.getMeasurementValue() > 10) {
                        if (!systolicDecrease) {
                            systolicTrend = 0;
                            systolicIncrease = false;
                            systolicDecrease = true;
                        }
                        systolicTrend++;
                    } else {
                        systolicIncrease = false;
                        systolicDecrease = false;
                        systolicTrend = 0;
                    }
                    if (diastolicTrend >= 3) {
                        triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "systolicTrend", lastTime));
                    }
                    lastSystolicPressure = record.getMeasurementValue();
                    if (lastSystolicPressure > 180 || lastSystolicPressure < 90) {
                        triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "systolicCriticalThreshold", lastTime));
                        if (lastSystolicPressure < 90 && saturation < 92) {
                            triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "hypotensiveHypoxemia", lastTime));
                        }
                    }
                case "DiastolicPressure":
                    if (record.getMeasurementValue() - lastDiastolicPressure > 10) {
                        if (!diastolicIncrease) {
                            diastolicTrend = 0;
                            diastolicIncrease = true;
                            diastolicDecrease = false;
                        }
                        diastolicTrend++;
                    } else if (lastDiastolicPressure - record.getMeasurementValue() > 10) {
                        if (!diastolicDecrease) {
                            diastolicTrend = 0;
                            diastolicIncrease = false;
                            diastolicDecrease = true;
                        }
                        diastolicTrend++;
                    } else {
                        diastolicIncrease = false;
                        diastolicDecrease = false;
                        diastolicTrend = 0;
                    }
                    if (diastolicTrend >= 3) {
                        triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "diastolicTrend", lastTime));
                    }
                    lastDiastolicPressure = record.getMeasurementValue();
                    if (lastDiastolicPressure > 120 || lastDiastolicPressure < 60) {
                        triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "diastolicCriticalThreshold", lastTime));
                    }

                case "ECG":
                    if (ECGSize == lastECGs.size()) {
                        if (Math.abs(record.getMeasurementValue() - averageECG) > ecgPeakThreshold) {
                            triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "AbnormalEcgData", lastTime));
                        }

                        //update average and dequeue
                        averageECG += record.getMeasurementValue()/ECGSize;
                        averageECG -= lastECGs.poll();
                    } //I assume we don't have to check things before the window gets full

                    lastECGs.offer(record.getMeasurementValue()); //enqueue

                case "Cholesterol":
                    break;
                case "WhiteBloodCells":
                    break;
                case "RedBloodCells":
                case "Alert":
                    triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "triggered alert", lastTime));
                case "Saturation":
                    saturation = record.getMeasurementValue();
                    lastSaturations.offer(saturation);
                    lastTimes.offer(lastTime);

                    if (saturation < 92) {
                        triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "lowSaturation", lastTime));
                    }
                    if (lastSaturations.peek() - saturation > 5) {
                        triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "lowSaturation", lastTime));
                    }
                    if (lastTime-lastTimes.peek() > 600000) { //assume time is in miliseconds, since that is in discription of the generator
                        //update the windows
                        lastTimes.poll();
                        lastSaturations.poll();
                    }
                default:
                    System.out.println("Unknown record type: " + record.getRecordType());
                    break;
            }
        }
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        // Implementation might involve logging the alert or notifying staff
    }
}
