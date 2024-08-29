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
	public static volatile int tickCount = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Wind Mod");
		WindManager.initialize();
		registerSounds();
		ModConfig.loadConfig();

		// Register server lifecycle events
		ServerLifecycleEvents.SERVER_STARTED.register(server -> ServerTickEvents.START_WORLD_TICK.register(this::onWorldTick));
	}


	private void registerSounds() {
		LOGGER.info("Registering sounds");
		ModSounds.registerWindSoundTicker();
		LOGGER.info("Sound Registered: {}", ModSounds.WIND_SOUND);
	}

	public void onWorldTick(World world) {
		// Update wind based on weather in each world
		tickCount += 1; // Increment the tick count
		if (tickCount % 3 == 1) {
			WindManager.updateWeather(world); // Pass the world instance to updateWeather
		}
	}
}
