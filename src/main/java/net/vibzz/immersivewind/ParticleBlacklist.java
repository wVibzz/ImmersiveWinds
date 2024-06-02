package net.vibzz.immersivewind;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ParticleBlacklist {
    private static Set<String> blacklist = new HashSet<>();

    // Get the current blacklist as a list
    public static List<String> getBlacklist() {
        return blacklist.stream().collect(Collectors.toList());
    }

    // Set the blacklist from a new list of particle names
    public static void setBlacklist(List<String> newBlacklist) {
        blacklist = new HashSet<>(newBlacklist);
    }

    // Add a particle to the blacklist
    public static void addBlacklist(String particleName) {
        blacklist.add(particleName);
    }

    // Remove a particle from the blacklist
    public static void removeBlacklist(String particleName) {
        blacklist.remove(particleName);
    }

    // Check if a particle is blacklisted
    public static boolean isBlacklisted(String particleName) {
        return blacklist.contains(particleName);
    }
}
