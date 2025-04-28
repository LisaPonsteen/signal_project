package com.alerts;

import com.alerts.Decorators.RepeatedAlertDecorator;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.Stack;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private final DataStorage dataStorage;
    // Strategies
    private final AlertStrategy ecgStrategy = new ECGStrategy();
    private final AlertStrategy saturationStrategy = new SaturationStrategy();
    private final CombinedAlertStrategy combinedStrategy = new CombinedAlertStrategy();
    private final AlertStrategy systolicStrategy = new SystolicBloodPressureStrategy();
    private final AlertStrategy diastolicStrategy = new DiabolicBloodPressureStrategy();

    // Factories
    private final AlertFactory bloodPressureFactory = new BloodPressureAlertFactory();
    private final AlertFactory oxygenFactory = new BloodOxygenAlertFactory();
    private final AlertFactory ecgFactory = new ECGAlertFactory();
    private final AlertFactory combinedFactory = new CombinedAlertFactory();
    private final AlertFactory alertFactory = new AlertFactory();

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

        //check all unchecked records of the patient, from old to new.
        for (PatientRecord record : records) {
            long time = record.getTimestamp();
            double value = record.getMeasurementValue();
            String type = record.getRecordType();
            Alert alert = null;

            if ("SystolicPressure".equalsIgnoreCase(type)) {
                if (systolicStrategy.checkAlert(value, time)) {
                    alert = bloodPressureFactory.createAlert(String.valueOf(patientId), "SystolicPressure=" + value, time);
                }
                combinedStrategy.setLastSystolicPressure(value);
                if (combinedStrategy.checkAlert(value, time)) {
                    triggerAlert(combinedFactory.createAlert(String.valueOf(patientId), "Hypotensive Hypoxemia", time));
                }

            } else if ("DiastolicPressure".equalsIgnoreCase(type)) {
                if (diastolicStrategy.checkAlert(value, time)) {
                    triggerAlert(bloodPressureFactory.createAlert(String.valueOf(patientId), "DiastolicPressure=" + value, time));
                }

            } else if ("ECG".equalsIgnoreCase(type)) {
                if (ecgStrategy.checkAlert(value, time)) {
                    triggerAlert(ecgFactory.createAlert(String.valueOf(patientId), "ECG=" + value, time));
                }

            } else if ("Saturation".equalsIgnoreCase(type)) {
                if (saturationStrategy.checkAlert(value, time)) {
                    triggerAlert(oxygenFactory.createAlert(String.valueOf(patientId), "Saturation=" + value, time));
                }
                combinedStrategy.setLastSaturation(value);

            } else if ("Alert".equalsIgnoreCase(type)) {
                triggerAlert(alertFactory.createAlert(String.valueOf(patientId), "Triggered Alert", time));

            } else {
                System.out.println("Unknown record type: " + type);
            }
            if (alert != null) {
                if (alert instanceof RepeatedAlertDecorator) {
                    triggerAlert(alert);
                    //check again and recheck code here
                }
            }
        }
        dataStorage.setPatientScannedTime(patientId, records.get(records.size()-1).getTimestamp());
    }

    //method get last of type

    /**
     * method that gets the latest (already checked) records that are important for some of the strategies to those strategies
     * without having to iterate through all existing records
     * this method should be run once before evaluating unchecked records for a patient, to make sure it also gives trend alerts etc for the first unchecked records
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
                    combinedStrategy.setLastSystolicPressure(systolicStack.peek());
            } else if (record.getRecordType().equals("DiastolicPressure") && diastolicStack.size() < repetitionsForTrend) {
                diastolicStack.push(record.getMeasurementValue());
            } else if (record.getRecordType().equals("Saturation") && !satWindowFull) {
                if (saturationStack.isEmpty()) {
                    firstSatTime = record.getTimestamp();
                    combinedStrategy.setLastSaturation(record.getMeasurementValue());
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





    /*
    public void evaluateDataOld(int patientId) {
        List<PatientRecord> records = dataStorage.getRecords(patientId, Long.MIN_VALUE, Long.MAX_VALUE);
        long lastTime;
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

        //I don't really get what you expect form the condition/descriptions of the alert
        for (PatientRecord record : records) {
            lastTime = record.getTimestamp();
            String recordType = record.getRecordType();
            if (recordType.equals("SystolicPressure")) {
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
                if (systolicTrend >= 3) {
                    triggerAlert(new Alert(String.valueOf(patientId), "systolicTrend", lastTime));
                }
                lastSystolicPressure = record.getMeasurementValue();
                if (lastSystolicPressure > 180 || lastSystolicPressure < 90) {
                    triggerAlert(new Alert(String.valueOf(patientId), "systolicCriticalThreshold", lastTime));
                    if (lastSystolicPressure < 90 && saturation < 92) {
                        triggerAlert(new Alert(String.valueOf(patientId), "hypotensiveHypoxemia", lastTime));
                    }
                }
            } else if (recordType.equals("DiastolicPressure")) {
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
                        triggerAlert(new Alert(String.valueOf(patientId), "DiastolicTrend", lastTime));
                    }
                    lastDiastolicPressure = record.getMeasurementValue();
                    if (lastDiastolicPressure > 120 || lastDiastolicPressure < 60) {
                        triggerAlert(new Alert(String.valueOf(patientId), "DiastolicCriticalThreshold", lastTime));
                    }

            } else if (recordType.equals("ECG")) {
                    if (ECGSize == lastECGs.size()) {
                        if (Math.abs(record.getMeasurementValue() - averageECG) > ecgPeakThreshold) {
                            triggerAlert(new Alert(String.valueOf(patientId), "Abnormal ECG Data", lastTime));
                        }

                        //update average and dequeue
                        averageECG += record.getMeasurementValue()/ECGSize;
                        averageECG -= lastECGs.poll();
                    } //I assume we don't have to check things before the window gets full

                    lastECGs.offer(record.getMeasurementValue()); //enqueue
            } else if (recordType.equals("Alert")) {
                    triggerAlert(new Alert(String.valueOf(patientId), "Triggered Alert", lastTime));
            } else if (recordType.equals("Saturation")) {
                saturation = record.getMeasurementValue();
                lastSaturations.offer(saturation);
                lastTimes.offer(lastTime);

                if (saturation < 92) {
                    triggerAlert(new Alert(String.valueOf(patientId), "Low Saturation", lastTime));
                }
                if (lastSaturations.peek() - saturation > 5) {
                    triggerAlert(new Alert(String.valueOf(patientId), "Rapid Drop", lastTime));
                }
                if (lastTime - lastTimes.peek() > 600000) { //assume time is in milliseconds, since that is in description of the generator
                    //update the windows
                    lastTimes.poll();
                    lastSaturations.poll();
                }
            } else if (recordType.equals("Cholesterol") || recordType.equals("WhiteBloodCells") || recordType.equals("RedBloodCells")) {
                break;
            } else {
                    System.out.println("Unknown record type: " + record.getRecordType());
            }
        }
    }
    */

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        // a bit vague how, where can I reach the staff?
        System.out.println(alert.toString());
    }
}
