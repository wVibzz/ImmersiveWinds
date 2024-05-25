package net.vibzz.immersivewind;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.World;
import net.vibzz.immersivewind.sound.ModSounds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class WindMod implements ModInitializer {
	public static final String MOD_ID = "windmod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static volatile int tickCount = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Wind Mod");
		WindManager.initialize();
		ModSounds.registerSoundEvent();

		// Register server lifecycle events
		ServerLifecycleEvents.SERVER_STARTED.register(server -> ServerTickEvents.START_WORLD_TICK.register(this::onWorldTick));
	}

	public void onWorldTick(World world) {
		// Update wind based on weather in each world
		tickCount += 1; // It's so horrible, but ServerTickEvents is very weird on its own
		if(tickCount%3 == 1) {
			WindManager.updateWeather(world);
		}
	}
}
