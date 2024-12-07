package com.chat.servidor.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class MyObjectOutputStream extends ObjectOutputStream {

    // Constructor para flujos nuevos
    public MyObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    // Constructor protegido (llamado internamente por ObjectOutputStream)
    protected MyObjectOutputStream() throws IOException, SecurityException {
        super();
    }

    // Sobrescribir el m�todo para evitar escribir el encabezado en flujos existentes
    @Override
    protected void writeStreamHeader() throws IOException {
        // No hacer nada
    }
}
