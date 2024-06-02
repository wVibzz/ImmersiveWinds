package net.vibzz.immersivewind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParticleBlacklist {
    private static final Logger LOGGER = LogManager.getLogger("ParticleBlacklist");
    private static Set<String> blacklist = new HashSet<>();

    public static List<String> getBlacklist() {
        return new ArrayList<>(blacklist);
    }

    public static void setBlacklist(List<String> newBlacklist) {
        blacklist = new HashSet<>(newBlacklist);
        LOGGER.info("Blacklist set to: {}", blacklist);
    }

    public static void addBlacklist(String particleName) {
        blacklist.add(particleName);
        LOGGER.info("Added to blacklist: {}", particleName);
    }

    public static void removeBlacklist(String particleName) {
        blacklist.remove(particleName);
        LOGGER.info("Removed from blacklist: {}", particleName);
    }

    public static boolean isBlacklisted(String particleName) {
        boolean result = blacklist.contains(particleName);
        //LOGGER.info("Is {} blacklisted: {}", particleName, result);
        return result;
    }
}
