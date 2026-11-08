package com.mission;

import org.springframework.boot.SpringApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobMissionApplication {
    public static void main(String[] args) {
        try { Files.createDirectories(Path.of("data")); }
        catch (IOException e) { throw new IllegalStateException("Cannot create local data directory", e); }
        SpringApplication.run(JobMissionApplication.class, args);
    }
}
