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

    public static void addBlacklist(String particleClassName) {
        blacklist.add(particleClassName);
        LOGGER.info("Added to blacklist: {}", particleClassName);
    }

    public static void removeBlacklist(String particleClassName) {
        blacklist.remove(particleClassName);
        LOGGER.info("Removed from blacklist: {}", particleClassName);
    }

    public static boolean isBlacklisted(String particleClassName) {
        boolean result = blacklist.contains(particleClassName);
        //LOGGER.info("Is {} blacklisted: {}", particleClassName, result);
        return result;
    }
}