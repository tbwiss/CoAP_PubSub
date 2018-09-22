/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wiss.thom.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author thomas
 */
public class MeasurementWriter {

    private final String filename;
    private BufferedWriter bufferedWriter = null;
    private FileWriter fileWriter = null;

    public MeasurementWriter(String filename) {
        this.filename = filename;
    }

    public void writeContent(String content) {

        try {
            File file = new File(filename);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            fileWriter = new FileWriter(file.getAbsoluteFile(), true);
            bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(content);
            bufferedWriter.newLine();

        } catch (IOException e) {
            System.err.println("SEVERE: Problems writing content to file");
            System.err.println(e);
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }

    }
}
