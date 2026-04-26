package com.CVRgram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CVRgram {

    public static void main(String[] args) {
        SpringApplication.run(CVRgram.class, args);
        System.out.println("\n===========================================");
        System.out.println("   CVRgram Backend Started Successfully!");
        System.out.println("===========================================");
        System.out.println("Server running on: http://localhost:8080");
        System.out.println("H2 Console: http://localhost:8080/h2-console");
        System.out.println("WebSocket endpoint: ws://localhost:8080/ws");
        System.out.println("===========================================\n");
    }
}
