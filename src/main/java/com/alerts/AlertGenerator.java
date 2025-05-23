package com.alerts;

import com.alerts.AlertFactories.*;
import com.alerts.AlertStrategies.*;
import com.alerts.Decorators.PriorityAlertDecorator;
import com.alerts.Decorators.RepeatedAlertDecorator;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private final DataStorage dataStorage;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    //Define and make instance of all strategies
    private final AlertStrategy ecgStrategy = new ECGStrategy();
    private final AlertStrategy saturationStrategy = new SaturationStrategy();
    private final CombinedAlertStrategy combinedStrategy = new CombinedAlertStrategy();
    private final AlertStrategy systolicStrategy = new SystolicBloodPressureStrategy();
    private final AlertStrategy diastolicStrategy = new DiabolicBloodPressureStrategy();
    private final AlertStrategy triggeredStrategy = new TriggeredAlertStrategy();

    //define all record types (got replaced by RecordType class)
    //private final String[] recordTypes = new String[]{"SystolicPressure", "DiastolicPressure", "ECG", "Saturation", "Alert"};

    //assign one or more alert strategies to every record type
    private final Map<String, AlertStrategy[]> strategies = Map.of(
            RecordType.SYSTOLIC, new AlertStrategy[]{systolicStrategy, combinedStrategy},
            RecordType.DIASTOLIC, new AlertStrategy[]{diastolicStrategy},
            RecordType.ECG, new AlertStrategy[]{ecgStrategy},
            RecordType.SATURATION, new AlertStrategy[]{saturationStrategy, combinedStrategy},
            RecordType.ALERT, new AlertStrategy[]{triggeredStrategy}
    );

    //assign a factory to every alert strategy
    private final Map<AlertStrategy, AlertFactory> factories = Map.of(systolicStrategy, new BloodPressureAlertFactory(),
            diastolicStrategy, new BloodPressureAlertFactory(), ecgStrategy, new ECGAlertFactory(), saturationStrategy, new BloodOxygenAlertFactory(),
            combinedStrategy, new CombinedAlertFactory(), triggeredStrategy, new AlertFactory());

    //define for which strategies an alertDecorator should be used
    private final List<AlertStrategy> priorityStrategies;
    private final List<AlertStrategy> repeatedStrategies;

    //constants for the AlertStrategies
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

        //make saturation alerts use a priority and repeated decorator
        priorityStrategies = List.of(saturationStrategy);
        repeatedStrategies = List.of(saturationStrategy);
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
            evaluateRecordForAlerts(record);
        }
        //update the scanned records as checked
        dataStorage.setPatientScannedTime(patientId, records.get(records.size()-1).getTimestamp());
    }

    /**
     * checks for a single record if an alert should be triggered.
     * Helper method for evaluateData()
     * @param record    the PatientRecord to be checked
     */
    private void evaluateRecordForAlerts(PatientRecord record) {
        String recordType = record.getRecordType();
        double value = record.getMeasurementValue();
        long time = record.getTimestamp();
        int patientId = record.getPatientId();

        //for all strategies that should be checked for the record type, check and make alert if necessary
        for (AlertStrategy strategy : strategies.get(recordType)) {
            if (strategy instanceof CombinedAlertStrategy) {
                ((CombinedAlertStrategy) strategy).setLastData(value, recordType);
            }
            if (strategy.checkAlert(value, time)) {
                AlertFactory factory = factories.get(strategy);
                assert factory != null: "No factory defined for strategy: " + strategy;

                Alert alert = factory.createAlert(String.valueOf(patientId), recordType + "=" + value, time);
                useDecorator(recordType, alert, strategy);
            }
        }
    }

    /**
     * checks how an alert should be triggered
     * @param recordType    record type that caused the alert
     * @param alert the alert
     * @param strategy strategy that caused the alert
     */
    private void useDecorator(String recordType, Alert alert, AlertStrategy strategy) {
        boolean triggered = false;
        if (priorityStrategies.contains(strategy)) {
            triggerAlert(new PriorityAlertDecorator(alert));
            triggered = true;
        }
        if (repeatedStrategies.contains(strategy)) {
            triggerAlert(new RepeatedAlertDecorator(alert, strategy, recordType));
        } else if (!triggered) {
            triggerAlert(alert);
        }
    }


    /**
     * Method that passes relevant old, already checked, data to the alert strategies, so they can see the old trends if needed
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

            if (record.getRecordType().equals(RecordType.ECG) && ecgStack.size() < EcgWindowSize) {
                ecgStack.push(record.getMeasurementValue());
            } else if (record.getRecordType().equals(RecordType.SYSTOLIC) && systolicStack.size() < repetitionsForTrend) {
                systolicStack.push(record.getMeasurementValue());
                if (systolicStack.size() == 1)
                    combinedStrategy.setLastData(systolicStack.peek(), RecordType.SYSTOLIC);
            } else if (record.getRecordType().equals(RecordType.DIASTOLIC) && diastolicStack.size() < repetitionsForTrend) {
                diastolicStack.push(record.getMeasurementValue());
            } else if (record.getRecordType().equals(RecordType.SATURATION) && !satWindowFull) {
                if (saturationStack.isEmpty()) {
                    firstSatTime = record.getTimestamp();
                    combinedStrategy.setLastData(record.getMeasurementValue(), RecordType.SATURATION);
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
        assert saturationTimes.size() == saturationStack.size(): "Saturation times and values size don't match";
        while (!saturationStack.isEmpty()) {
            saturationStrategy.checkAlert(saturationStack.pop(), saturationTimes.pop());
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
     * Puts a task into a scheduler, so it will get done repeatedly at their intended time
     *
     * @param task  Task that has to be executed
     * @param period    Period after which the task has to be executed again
     * @param timeUnit  The time unit of the period
     */
    private void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(task, 3, period, timeUnit);
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
        assert alert != null;
        System.out.println(alert);

        if (alert instanceof RepeatedAlertDecorator) {
            int id = Integer.parseInt(alert.getPatientId());
            String recordType = ((RepeatedAlertDecorator) alert).getRecordType();

            //schedule to recheck the alert 3 times
            for (int i = 1; i <= 3; i++) {
                int delay = i * 3; // after 3s, 6s, 9s
                scheduler.schedule(() -> {
                    PatientRecord latestRecord = dataStorage.getLastRecordOfType(id, recordType);
                    System.out.println("rechecking on value" + latestRecord.getMeasurementValue());
                    if (((RepeatedAlertDecorator) alert).getAlertStrategy()
                            .checkAlert(latestRecord.getMeasurementValue(), latestRecord.getTimestamp())) {
                        System.out.println(alert + ", repeated");
                    }
                }, delay, TimeUnit.SECONDS);
            }
        }
    }
}
