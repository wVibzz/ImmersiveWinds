package net.vibzz.immersivewind;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WindMod implements ModInitializer {
	public static final String MOD_ID = "windmod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final DefaultParticleType WIND_WISP = FabricParticleTypes.simple();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Wind Mod");
		WindManager.initialize();
		Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "wind_particle"), WIND_WISP);

		// Register server lifecycle events
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			// Listen for world tick events
			WorldTickCallback.EVENT.register(this::onWorldTick);
		});
	}

	public void onWorldTick(World world) {
		// Update wind based on weather in each world
		WindManager.updateWindBasedOnWeather(world);
	}

}
