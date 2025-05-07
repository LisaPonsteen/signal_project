package com.alerts;

import com.alerts.AlertFactories.*;
import com.alerts.AlertStrategies.*;
import com.alerts.Decorators.RepeatedAlertDecorator;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private final DataStorage dataStorage;

    //Define all strategies
    private final AlertStrategy ecgStrategy = new ECGStrategy();
    private final AlertStrategy saturationStrategy = new SaturationStrategy();
    private final CombinedAlertStrategy combinedStrategy = new CombinedAlertStrategy();
    private final AlertStrategy systolicStrategy = new SystolicBloodPressureStrategy();
    private final AlertStrategy diastolicStrategy = new DiabolicBloodPressureStrategy();
    private final AlertStrategy triggeredStrategy = new TriggeredAlertStrategy();

    //define all record types
    private final String[] recordTypes = new String[]{"SystolicPressure", "DiastolicPressure", "ECG", "Saturation", "Alert"};

    //assign one or more alert strategies to every record type
    private final Map<String, AlertStrategy[]> strategies = Map.of(recordTypes[0], new AlertStrategy[]{systolicStrategy, combinedStrategy},
            recordTypes[1], new AlertStrategy[]{diastolicStrategy},
            recordTypes[2], new AlertStrategy[]{ecgStrategy},
            recordTypes[3], new AlertStrategy[]{saturationStrategy, combinedStrategy},
            recordTypes[4], new AlertStrategy[]{triggeredStrategy});

    //assign a factory to every alert strategy
    private final Map<AlertStrategy, AlertFactory> factories = Map.of(systolicStrategy, new BloodPressureAlertFactory(),
            diastolicStrategy, new BloodPressureAlertFactory(), ecgStrategy, new ECGAlertFactory(), saturationStrategy, new BloodOxygenAlertFactory(),
            combinedStrategy, new CombinedAlertFactory(), triggeredStrategy, new AlertFactory());

    //constants
    private final int repetitionsForTrend = ((SystolicBloodPressureStrategy) systolicStrategy).getRepetitionsForTrend();
    private final int EcgWindowSize = ((ECGStrategy) ecgStrategy).getECGSize();
    private final long saturationWindowSize = ((SaturationStrategy) saturationStrategy).getWindowSize();

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
     * {@link #triggerAlert} method.
     *
     * @param patientId the patient to evaluate for alert conditions
     */
    public void evaluateData(int patientId) {
        long evaluateFrom = dataStorage.getPatientScannedTime(patientId);
        List<PatientRecord> evaluatedRecords = dataStorage.getRecords(patientId, Long.MIN_VALUE, evaluateFrom);
        prepAlertStrategies(evaluatedRecords);

        List<PatientRecord> records = dataStorage.getRecords(patientId, evaluateFrom, Long.MAX_VALUE);

        //checks all unchecked records of the patient, from old to new.
        for (PatientRecord record : records) {
            long time = record.getTimestamp();
            double value = record.getMeasurementValue();
            String recordType = record.getRecordType();

            //for all strategies that should be checked for the record type, check and make alert if necessary
            for (int i = 0; i < strategies.get(recordType).length; i++) {
                AlertStrategy strategy = strategies.get(recordType)[i];
                if (strategy == null) {
                    System.out.println("no strategy for record type: " + recordType);
                    break;
                }
                if (strategy instanceof CombinedAlertStrategy) {
                    ((CombinedAlertStrategy) strategy).setLastData(value, recordType);
                }
                if (strategy.checkAlert(value, time)) {
                    if (factories.get(strategy)==null)
                        System.out.println("no factory defined for used strategy for record type: " + recordType);
                    else
                        triggerAlert(factories.get(strategy).createAlert(String.valueOf(patientId), recordType + "=" + value, time));
                }
            }
        }
        //update the scanned records as checked
        dataStorage.setPatientScannedTime(patientId, records.get(records.size()-1).getTimestamp());
    }

    /**
     * Method that passes relevant old, already checked, data to the alert strategies
     * It does so without having to iterate through all existing records
     * This method should be run once before evaluating unchecked records for a patient, to make sure it also gives trend alerts etc. for the first unchecked records
     *
     * @param evaluatedRecords list of already evaluated records
     */
    private void prepAlertStrategies (List<PatientRecord> evaluatedRecords) {
        Stack<Double> systolicStack = new Stack<>();
        Stack<Double> diastolicStack = new Stack<>();
        Stack<Double> ecgStack = new Stack<>();
        Stack<Double> saturationStack = new Stack<>();
        Stack<Long> saturationTimes = new Stack<>();
        long firstSatTime = 0;
        boolean satWindowFull = false;

        for (int i = evaluatedRecords.size() - 1; i >= 0; i--) {
            PatientRecord record = evaluatedRecords.get(i);

            if (record.getRecordType().equals("ECG") && ecgStack.size() < EcgWindowSize) {
                ecgStack.push(record.getMeasurementValue());
            } else if (record.getRecordType().equals("SystolicPressure") && systolicStack.size() < repetitionsForTrend) {
                systolicStack.push(record.getMeasurementValue());
                if (systolicStack.size() == 1)
                    combinedStrategy.setLastData(systolicStack.peek(), "SystolicPressure");
            } else if (record.getRecordType().equals("DiastolicPressure") && diastolicStack.size() < repetitionsForTrend) {
                diastolicStack.push(record.getMeasurementValue());
            } else if (record.getRecordType().equals("Saturation") && !satWindowFull) {
                if (saturationStack.isEmpty()) {
                    firstSatTime = record.getTimestamp();
                    combinedStrategy.setLastData(record.getMeasurementValue(), "Saturation");
                } else if (saturationWindowSize <= firstSatTime -record.getTimestamp()){
                    satWindowFull = true;
                }
                saturationStack.push(record.getMeasurementValue());
                saturationTimes.push(record.getTimestamp());
            }
            if (ecgStack.size() == EcgWindowSize && satWindowFull && systolicStack.size() == repetitionsForTrend && diastolicStack.size() == repetitionsForTrend) {
                break; // stop early if everything that is needed is collected
            }
        }
        if (saturationTimes.size() != saturationStack.size()) {
            System.out.println("Saturation times and values size don't match");
        } else {
            while (!saturationStack.isEmpty()) {
                saturationStrategy.checkAlert(saturationStack.pop(), saturationTimes.pop());
            }
        }
        while (!systolicStack.isEmpty()){
            systolicStrategy.checkAlert(systolicStack.pop(), null);
        }
        while (!diastolicStack.isEmpty()) {
            diastolicStrategy.checkAlert(diastolicStack.pop(), null);
        }
        while (!ecgStack.isEmpty()){
            ecgStrategy.checkAlert(ecgStack.pop(), null);
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
        if (alert != null) {
            System.out.println(alert.toString());

            if (alert instanceof RepeatedAlertDecorator) {
                int id = Integer.parseInt(alert.getPatientId());
                String recordType = ((RepeatedAlertDecorator) alert).getRecordType();

                //recheck the alert 3 times
                int repeatedCount = 0;
                for (int i = 0; i < 3; i++) {
                    PatientRecord latestRecord = dataStorage.getLastRecordOfType(id, recordType);
                    if (((RepeatedAlertDecorator) alert).getAlertStrategy().checkAlert(latestRecord.getMeasurementValue(), latestRecord.getTimestamp())) {
                        repeatedCount++;
                        System.out.println(alert.toString() + ", repeated " + repeatedCount + " times");
                    }
                    try {
                        Thread.sleep(3000); // 3 seconds between rechecks
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
