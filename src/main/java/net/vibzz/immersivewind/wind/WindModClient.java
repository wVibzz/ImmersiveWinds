package net.vibzz.immersivewind.wind;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import static net.vibzz.immersivewind.wind.WindMod.LOGGER;

public class WindModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerWindEffects();

        // Conditionally load ModMenu integration
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            LOGGER.info("ModMenu detected, initializing ModMenu integration.");
            loadModMenuIntegration();
        } else {
            LOGGER.info("ModMenu not detected, skipping ModMenu integration.");
        }
    }

    private void loadModMenuIntegration() {
        try {
            // Load the ModMenuIntegration class only if it exists
            Class.forName("net.vibzz.immersivewind.ModMenuIntegration");
            LOGGER.info("ModMenu integration successfully initialized.");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("ModMenu integration class not found, skipping integration.");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize ModMenu integration.", e);
        }
    }

    public static void registerWindEffects() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                WindManager.interpolateWind(); // Ensure wind direction and strength are interpolated
            }
        });
    }
}
