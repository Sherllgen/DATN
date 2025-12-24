package com.project.evgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
public class EvgoApplication {

    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(EvgoApplication.class, args);
    }

    public static void loadEnvFile() {
		try {
			File envFile = new File(".env");
			if (envFile.exists()) {
				Properties props = new Properties();
				props.load(new FileInputStream(envFile));

				props.forEach((key, value) -> {
					System.setProperty(key.toString(), value.toString());
				});

				System.out.println("Loaded " + props.size() + " properties from .env file");
			}
		} catch (IOException e) {
			System.err.println("Could not load .env file: " + e.getMessage());
		}
	}
}
