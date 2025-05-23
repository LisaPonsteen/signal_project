package com.data_management;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FileReader implements DataReader {
    private String outputDir;
    public FileReader(String outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Reads data from an output file generated using the --output file:<output_dir> argument
     *
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the data
     */
    public void readData(DataStorage dataStorage) throws IOException {
        File dir = new File(outputDir);
        File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No files in directory");
            return;
        }

        for (File file : files) {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine(); //extract line with data from file
                DataParser.parseLine(line, dataStorage); //give it to data parser, that will put it into data storage
            }
            scanner.close();
        }
    }
}
