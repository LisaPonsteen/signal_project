package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/** Outputs patient data in a file */
//class names should be in UpperCamelCase, so I changed class name from fileOutputStrategy to FileOutputStrategy
public class FileOutputStrategy implements OutputStrategy {

    private String baseDirectory; //should be in lowerCamelCase, so changed from BaseDirectory to baseDirectory
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>(); //fields should be lowerCamelCase, so no underscores. So I changed file_map to fileMap

    /**
     *  Constructor that sets the location of the directory where the files will be stored
     *
     * @param baseDirectory The name of the directory
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     *  Outputs data for a single patient
     *  Creates a directory and file if not already there, and writes their data in it
     *
     * @param patientId ID of the patient
     * @param timestamp Timestamp at which the data was generated
     * @param label     Label describing the type of data
     * @param data      The data of the patient
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable
        //local variables should be in lowerCamelCase, so I changed the name FilePath to filePath
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}