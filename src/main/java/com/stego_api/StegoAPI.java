package com.stego_api;

import com.stego_api.entity.ThemeParkRide;
import com.stego_api.repository.ThemeParkRideRepository;
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
    public CommandLineRunner sampleData(ThemeParkRideRepository repository) {
        return (args) -> {
            repository.save(new ThemeParkRide("Rollercoaster", "Train ride that speeds you along.", 5, 3));
            repository.save(new ThemeParkRide("Log flume", "Boat ride with plenty of splashes.", 3, 2));
            repository.save(new ThemeParkRide("Teacups", "Spinning ride in a giant tea-cup.", 2, 4));
        };
    }
}
