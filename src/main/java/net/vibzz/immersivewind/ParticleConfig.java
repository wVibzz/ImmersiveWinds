package net.vibzz.immersivewind;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ParticleConfig {
    private static final Set<String> excludedParticles = new HashSet<>();

    static {
        try {
            loadExcludedParticles();
        } catch (IOException e) {
            System.err.println("Error loading excluded particles configuration: " + e.getMessage());
        }
    }

    private static void loadExcludedParticles() throws IOException {
        InputStream inputStream = ParticleConfig.class.getClassLoader().getResourceAsStream("excluded_particles.txt");
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                excludedParticles.addAll(reader.lines().map(String::trim).collect(Collectors.toSet()));
                System.out.println("Loaded excluded particles: " + excludedParticles);  // Debug print
            }
        } else {
            System.err.println("Excluded particles config file not found.");
        }
    }
}
