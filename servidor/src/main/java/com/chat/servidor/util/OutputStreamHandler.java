package com.chat.servidor.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class OutputStreamHandler {
    private ObjectOutputStream outputStream;

    public void writeObjectToFile(File file, Object object) throws IOException {
        boolean append = file.exists() && file.length() > 0;

        try (FileOutputStream fileOut = new FileOutputStream(file, true)) {
            if (append) {
                // Usar MyObjectOutputStream para evitar escribir encabezados
                outputStream = new MyObjectOutputStream(fileOut);
            } else {
                // Usar ObjectOutputStream est�ndar si es un archivo nuevo
                outputStream = new ObjectOutputStream(fileOut);
            }
            outputStream.writeObject(object);
            outputStream.flush();
        }
    }
}
