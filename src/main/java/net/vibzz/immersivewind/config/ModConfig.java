package net.vibzz.immersivewind.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.vibzz.immersivewind.sounds.PlayerWindSoundInstance;
import net.vibzz.immersivewind.wind.WindManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ModConfig {
    private static final Logger LOGGER = LogManager.getLogger("ModConfig");
    private static final Path CONFIG_PATH = Paths.get("config", "immersivewind.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = new String(Files.readAllBytes(CONFIG_PATH));
                ConfigData configData = GSON.fromJson(json, ConfigData.class);
                PlayerWindSoundInstance.enableWind = configData.enableWind;
                WindManager.enableWindWisps = configData.enableWindWisps;
                ParticleBlacklist.setBlacklist(configData.particleBlacklist);
            } catch (IOException e) {
                LOGGER.error("Failed to load config file", e);
            }
        } else {
            // Save default configuration if file does not exist
            saveConfig();
        }
    }

    public static void saveConfig() {
        try {
            ConfigData configData = new ConfigData();
            configData.enableWind = PlayerWindSoundInstance.enableWind;
            configData.enableWindWisps = WindManager.enableWindWisps;
            configData.particleBlacklist = ParticleBlacklist.getBlacklist();
            String json = GSON.toJson(configData);
            Files.write(CONFIG_PATH, json.getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to save config file", e);
        }
    }

    public static class ConfigData {
        boolean enableWind = true;
        boolean enableWindWisps = true;
        List<String> particleBlacklist;
    }
}
