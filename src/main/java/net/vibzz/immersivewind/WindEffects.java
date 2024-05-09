package net.vibzz.immersivewind;
import net.vibzz.immersivewind.ModSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import java.util.Random;
import net.minecraft.world.World;

public class WindEffects implements ClientModInitializer {
    private static final Random random = new Random();
    private static long lastSpawnTime = -1;
    private static final long SPAWN_INTERVAL_TICKS = 5; // corresponds to 1 second if 1 tick = 50 ms
    private static final double SPREAD_RADIUS = 40.0;

    @Override
    public void onInitializeClient() {
        registerWindEffects();
        ModSounds.registerSounds();
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
            playWindSound(world); // Play wind sound based on current conditions
            lastSpawnTime = currentTime;
        }
    }

    private static void playWindSound(World world) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float windStrength = WindManager.getWindStrength();
        if (windStrength <= 10) {
            mc.player.playSound(ModSounds.WIND_WEAK, 1.0F, 1.0F);
        } else if (windStrength <= 20) {
            mc.player.playSound(ModSounds.WIND_MEDIUM, 1.0F, 1.0F);
        } else {
            mc.player.playSound(ModSounds.WIND_STRONG, 1.0F, 1.0F);
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

            mc.world.addParticle(ParticleTypes.WHITE_ASH, posX, posY, posZ, motionX, 0, motionZ);
        }
    }

    private static int mapWindStrengthToParticleCount(int windStrength) {
        return windStrength * 20; // Example scaling for particle count based on wind strength
    }

    private static double mapWindStrengthToSpeed(int windStrength) {
        // Simple linear mapping of wind strength to particle speed
        return windStrength * 0.1;
    }
}
