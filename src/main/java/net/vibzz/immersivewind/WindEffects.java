package net.vibzz.immersivewind;

import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.util.Random;
import net.minecraft.util.Identifier;

public class WindEffects implements ClientModInitializer {
    private static final Random random = new Random();
    private static long lastSpawnTime = -1;
    private static final long SPAWN_INTERVAL_TICKS = 5; // corresponds to 1 second if 1 tick = 50 ms
    private static final double SPREAD_RADIUS = 40.0; //Particle Spread
    private static Object wind_wisp;

    @Override
    public void onInitializeClient() {
        registerWindEffects();
        ModParticles.registerParticles();
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
        if (currentTime - lastSpawnTime >= SPAWN_INTERVAL_TICKS * 50) { // Ensure there's a gap between spawns
            spawnWindParticles(world);
            lastSpawnTime = currentTime;
        }
    }

    public class ModParticles {
        public static final DefaultParticleType WIND_WISP = new ParticleType(true);

        public static void registerParticles() {
            Registry.register(Registries.PARTICLE_TYPE, new Identifier("windmod", "wisp_particle"), WIND_WISP);
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

            mc.world.addParticle(ModParticles.WIND_WISP, posX, posY, posZ, 0.0, 0.0, 0.0);
        }
    }


    private static int mapWindStrengthToParticleCount(int windStrength) {
        return windStrength * 20; // Scaling for particle count based on wind strength
    }

    private static double mapWindStrengthToSpeed(int windStrength) {
        // Linear mapping of wind strength to particle speed
        return windStrength * 0.1;
    }
}
