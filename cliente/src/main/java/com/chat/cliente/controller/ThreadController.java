package com.chat.cliente.controller;

import java.util.Map;

public class ThreadController {

    /**
     * Método estático para mostrar los hilos activos en la aplicación.
     * Puede invocarse en cualquier parte del código.
     */
    public static void printActiveThreads() {
        // Obtener el grupo raíz de hilos
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }

        // Crear un array para almacenar los hilos activos
        int threadCount = rootGroup.activeCount();
        Thread[] threads = new Thread[threadCount];
        rootGroup.enumerate(threads, true);

        // Imprimir información de cada hilo
        System.out.println("===== HILOS ACTIVOS =====");
        System.out.printf("Número total de hilos activos: %d%n", threadCount);

        for (Thread t : threads) {
            if (t != null) {
                System.out.printf("Hilo: %s | ID: %d | Estado: %s | Prioridad: %d | Daemon: %b%n",
                        t.getName(), t.getId(), t.getState(), t.getPriority(), t.isDaemon());
            }
        }

        // Mostrar los hilos específicos que se ejecutan en el sistema actual
        System.out.println("=========================");
    }
}
