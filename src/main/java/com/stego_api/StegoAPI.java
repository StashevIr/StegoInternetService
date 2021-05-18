package com.stego_api;

import com.stego_api.entity.SteganographicEntity;
import com.stego_api.repository.SteganographicRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StegoAPI {
    public static void main(String[] args) {
        SpringApplication.run( StegoAPI.class);
    }

    @Bean
    public CommandLineRunner sampleData(SteganographicRepository repository) {
        return (args) -> {
            repository.save(new SteganographicEntity("Rollercoaster", "Train ride that speeds you along.", 5, 3));
            repository.save(new SteganographicEntity("Log flume", "Boat ride with plenty of splashes.", 3, 2));
            repository.save(new SteganographicEntity("Teacups", "Spinning ride in a giant tea-cup.", 2, 4));
        };
    }
}
