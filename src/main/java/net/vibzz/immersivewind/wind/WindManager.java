package net.vibzz.immersivewind.wind;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.vibzz.immersivewind.particle.ModParticles;
import net.vibzz.immersivewind.sounds.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import static net.vibzz.immersivewind.wind.WindMod.LOGGER;

public class WindManager {

    public static volatile float currentWindDirection = 0.0f; // Degrees
    public static volatile float targetWindDirection = 0.0f; // Degrees
    public static final AtomicInteger currentWindStrength = new AtomicInteger(1);
    public static final AtomicInteger targetWindStrength = new AtomicInteger(1);

    public static final Random random = new Random();

    public static final long WIND_CHANGE_COOLDOWN = 10000; // Cooldown period in milliseconds (10 seconds)
    private static final float DIRECTION_CHANGE_DISTANCE = 35.0f; // Degrees to increment per step
    private static final float DIRECTION_TOLERANCE = 1.0f; // Degrees within which we directly set the direction
    private static final long STRENGTH_CHANGE_TIME = 8000; // Time in milliseconds over which strength changes
    private static final long SOUND_UPDATE_INTERVAL = 1000; // Interval to update sound in milliseconds (1 second)

    private static volatile int previousWeatherState; // 0 - clear, 1 - rain, 2 - thunder
    private static final LinkedList<WindHistoryEntry> windHistory = new LinkedList<>();
    private static final int MAX_HISTORY_SIZE = 10; // Store up to 10

    private static long lastWindChangeTime = 0; // To track the last wind change time
    private static long windStrengthChangeStartTime = 0; // To track the start time of strength change
    private static long lastSoundUpdateTime = 0; // To track the last sound update time
    private static int initialWindStrength = 1; // Initial strength before change

    public static boolean enableWindWisps = false;

    public static void initialize() {
        previousWeatherState = -1;
        currentWindDirection = 0.0f; // Set initial direction to North
        currentWindStrength.set(1);  // Set initial Strength to 1
        lastWindChangeTime = System.currentTimeMillis();
        lastSoundUpdateTime = System.currentTimeMillis();
        LOGGER.info("Wind is initialized");
    }

    private static boolean isNonSolidBlock(BlockState state) {
        return state.getFluidState().getFluid() == Fluids.WATER || state.getFluidState().getFluid() == Fluids.LAVA;
    }

    public static void SpawnWindParticles(World world) {
        // Ensure that we only spawn particles on the client side.
        for (PlayerEntity player : world.getPlayers()) {

            // Determine the number of particles to spawn based on wind strength
            int particleCount = (int) (currentWindStrength.get() * 0.1);
            if (particleCount == 0) {
                particleCount = 1;
            }

            // Get player's position
            double playerX = player.getX();
            double playerY = player.getY();
            double playerZ = player.getZ();

            // Spawn particles around the player
            Random random = new Random();
            for (int i = 0; i < particleCount; i++) {
                double offsetX = random.nextDouble() * 10 - 5; // Random offset between -5 and 5
                double offsetY = -2 + random.nextDouble() * 10; // Random offset between -2 and 8
                double offsetZ = random.nextDouble() * 10 - 5; // Random offset between -5 and 5

                // Spawn the particle
                if (!isNonSolidBlock(world.getBlockState(new BlockPos((int) (playerX + offsetX), (int) (playerY + offsetY), (int) (playerZ + offsetZ))))) {
                    ParticleManager particleManager = MinecraftClient.getInstance().particleManager;
                    particleManager.addParticle(ModParticles.WINDWISP_PARTICLE, playerX + offsetX, playerY + offsetY, playerZ + offsetZ, 0, 0, 0);
                }
            }
        }
    }

    private static int getCurrentWeatherState(World world) {
        if (world.isThundering()) { // Not the best implementation, but it works sequentially and it's cool
            return 2;
        } else if (world.isRaining()) {
            return 1;
        } else {
            return 0;
        }
    }

    public static void updateWeather(World world) {
        long currentTime = System.currentTimeMillis();

        if (previousWeatherState != getCurrentWeatherState(world)) {
            // Check for the change in weather
            updateWindBasedOnWeather(world);
            previousWeatherState = getCurrentWeatherState(world); // Remember previous state
            lastWindChangeTime = currentTime; // Reset the wind change timer
        } else if (currentTime - lastWindChangeTime >= WIND_CHANGE_COOLDOWN) {
            // Check for the cooldown
            updateWindBasedOnWeather(world);
            lastWindChangeTime = currentTime; // Reset the wind change timer
        }

        interpolateWind();  // Ensure wind direction and strength are being interpolated every update

        if (currentTime - lastSoundUpdateTime >= SOUND_UPDATE_INTERVAL) {
            for (PlayerEntity player : world.getPlayers()) {
                ModSounds.playWindSound(player); // Update wind sound for each player
            }
            lastSoundUpdateTime = currentTime; // Reset the sound update timer
        }


        if (enableWindWisps == true) {
            // **NEW: Spawn wind particles around the player.**
            SpawnWindParticles(world);
        }
    }

    public static void updateWindBasedOnWeather(World world) {
        int newWeather = getCurrentWeatherState(world);
        float newDirection = calculateNewWindDirection();
        int newStrength = calculateNewWindStrength(world);
        setTargetWeather(newWeather, newDirection, newStrength);
    }

    public static void setTargetWeather(float weather, float direction, int strength) {
        // Record the change of wind direction with its timestamp
        addWindHistoryEntry(currentWindDirection);

        targetWindDirection = direction;
        targetWindStrength.set(strength);
        initialWindStrength = currentWindStrength.get();
        windStrengthChangeStartTime = System.currentTimeMillis();

        LOGGER.info("Setting Target Weather {} ,Wind Direction: {} ,Wind strength {}",weather, direction, strength);
    }

    public static void interpolateWind() {
        interpolateDirection();
        interpolateStrength();
    }

    private static void interpolateDirection() {
        float angleDifference = targetWindDirection - currentWindDirection;
        angleDifference -= (float) (Math.floor(angleDifference / 360 + 0.5) * 360); // Properly normalize between -180 and 180

        if (Math.abs(angleDifference) <= DIRECTION_TOLERANCE) {
            currentWindDirection = targetWindDirection;
        } else {
            float change = Math.signum(angleDifference) * Math.min(Math.abs(angleDifference), DIRECTION_CHANGE_DISTANCE);
            currentWindDirection = (currentWindDirection + change + 360) % 360;
        }
    }

    private static void interpolateStrength() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - windStrengthChangeStartTime;

        if (elapsedTime >= STRENGTH_CHANGE_TIME) {
            // If the time has elapsed, set the strength to the target strength
            currentWindStrength.set(targetWindStrength.get());
        } else {
            // Calculate the interpolated strength based on elapsed time
            float interpolationFactor = (float) elapsedTime / STRENGTH_CHANGE_TIME;
            int interpolatedStrength = (int) (initialWindStrength + interpolationFactor * (targetWindStrength.get() - initialWindStrength));
            currentWindStrength.set(interpolatedStrength);
        }
    }

    public static float getWindDirection() {
        return currentWindDirection;
    }

    public static int getWindStrength() {
        return currentWindStrength.get();
    }

    public static void addWindHistoryEntry(float windDirection) {
        if (windHistory.size() >= MAX_HISTORY_SIZE) {
            windHistory.removeFirst(); // Remove the oldest entry to maintain size
        }
        windHistory.addLast(new WindHistoryEntry(windDirection));
    }

    private static float calculateNewWindDirection() {
        float change = random.nextFloat() * 30 - 15; // This creates a range from -15 to +15
        float newDirection = currentWindDirection + change;
        newDirection = (newDirection + 360) % 360; // Normalize to 0-360 degrees
        return newDirection;
    }

    public static int calculateNewWindStrength(World world) {
        if (world.isThundering()) {
            return random.nextInt(13) + 23; // 23 -> 36
        } else if (world.isRaining()) {
            return random.nextInt(8) + 9; // 9 -> 17
        } else {
            return random.nextInt(4) + 1;  // 1 -> 5 // world.isClear
        }
    }

    private record WindHistoryEntry(float windDirection) {
    }

    public static float calculateWindVolume() {
        // Calculate volume based on wind strength
        int strength = currentWindStrength.get();
        // Normalize strength to a volume level between 0.0 and 1.0
        return Math.min(1.0f, strength / 36.0f);
    }
}