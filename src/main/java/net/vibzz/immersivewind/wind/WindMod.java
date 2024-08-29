package net.vibzz.immersivewind.wind;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.World;
import net.vibzz.immersivewind.ModConfig;
import net.vibzz.immersivewind.sounds.ModSounds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WindMod implements ModInitializer {
	public static final String MOD_ID = "immersivewind";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private int tickCount = 0;
	private boolean isTickEventRegistered = false;  // New flag to track event registration

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Wind Mod");
		WindManager.initialize();
		LOGGER.info("Registering sounds");
		ModSounds.registerWindSoundTicker();
		LOGGER.info("Sound Registered: {}", ModSounds.WIND_SOUND);
		ModConfig.loadConfig();
		LOGGER.info("Config loaded");

		// Register server lifecycle events
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (!isTickEventRegistered) {  // Check if the event is already registered
				ServerTickEvents.START_WORLD_TICK.register(this::onWorldTick);
				isTickEventRegistered = true;  // Set the flag to true after registration
				LOGGER.info("World tick event registered");
			}
		});
	}

	public void onWorldTick(World world) {
		// Update wind based on weather in each world
		tickCount += 1; // Increment the tick count
		if (tickCount % 3 == 1) {
			WindManager.updateWeather(world); // Pass the world instance to updateWeather
		}
	}
}
