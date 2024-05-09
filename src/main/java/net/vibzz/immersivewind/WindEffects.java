package net.vibzz.immersivewind;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*; // Grabs all particles
import net.minecraft.world.World;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.util.Random;

public class WindEffects implements ClientModInitializer {
    private static final Random random = new Random();
    private static long lastSpawnTime = -1;
    private static final long SPAWN_INTERVAL_TICKS = 2; // Corresponds to 100 ms
    private static final double SPREAD_RADIUS = 100.0; // Increased particle spread

    @Override
    public void onInitializeClient() {
        registerWindEffects();
        ParticleFactoryRegistry.getInstance().register(WindMod.WIND_WISP, AshParticle.Factory::new);
    }

    public static void registerWindEffects() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                WindManager.interpolateWind(); // Ensure wind direction and strength are interpolated
                handleVisualEffects(client.world);
            }
        });
    }

    private static void handleVisualEffects(World world) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime >= SPAWN_INTERVAL_TICKS * 50) { // Adjusted gap between spawns
            spawnWindParticles(world);
            lastSpawnTime = currentTime;
        }
    }

    public static void spawnWindParticles(World world) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        float windDirection = WindManager.getWindDirection();
        int windStrength = WindManager.getWindStrength();
        int particleCount = mapWindStrengthToParticleCount(windStrength);

        for (int i = 0; i < particleCount; i++) {
            double posX = mc.player.getX() + (random.nextDouble() * 2 - 1) * SPREAD_RADIUS;
            double posY = mc.player.getEyeY() + (random.nextDouble() * 2 - 1) * SPREAD_RADIUS;
            double posZ = mc.player.getZ() + (random.nextDouble() * 2 - 1) * SPREAD_RADIUS;

            double motionX = Math.cos(Math.toRadians(windDirection)) * mapWindStrengthToSpeed(windStrength);
            double motionZ = Math.sin(Math.toRadians(windDirection)) * mapWindStrengthToSpeed(windStrength);

            mc.world.addParticle(WindMod.WIND_WISP, posX, posY, posZ, motionX, 0.0, motionZ);
        }
    }

    private static int mapWindStrengthToParticleCount(int windStrength) {
        // Reduce the number of particles spawned
        return Math.max(1, windStrength * 5); // Reduced scaling factor
    }

    private static double mapWindStrengthToSpeed(int windStrength) {
        // Linear mapping of wind strength to particle speed
        return windStrength * 0.1;
    }
}
