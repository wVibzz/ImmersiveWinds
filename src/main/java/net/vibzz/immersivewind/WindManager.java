package net.vibzz.immersivewind;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;
import net.vibzz.immersivewind.sound.ModSounds;

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

    private static volatile int previousWeatherState; // 0 - clear, 1 - rain, 2 - thunder
    private static final LinkedList<WindHistoryEntry> windHistory = new LinkedList<>();
    private static final int MAX_HISTORY_SIZE = 10; // Store up to 10

    private static long lastWindChangeTime = 0; // To track the last wind change time
    private static long windStrengthChangeStartTime = 0; // To track the start time of strength change
    private static int initialWindStrength = 1; // Initial strength before change

    public static void initialize() {
        previousWeatherState = -1;
        currentWindDirection = 0.0f; // Set initial direction to North
        currentWindStrength.set(1);  // Set initial Strength to 1
        lastWindChangeTime = System.currentTimeMillis();
        System.out.println("Wind is initialized");
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
        if (previousWeatherState != getCurrentWeatherState(world)) { // Check for the change in weather
            updateWindBasedOnWeather(world);
            previousWeatherState = getCurrentWeatherState(world); // Remember previous state
            lastWindChangeTime = currentTime; // Reset the wind change timer
        } else if (currentTime - lastWindChangeTime >= WIND_CHANGE_COOLDOWN) { // Check for the cooldown
            updateWindBasedOnWeather(world);
            lastWindChangeTime = currentTime; // Reset the wind change timer
        }
        interpolateWind();  // Ensure wind direction and strength are being interpolated every update
    }

    public static void updateWindBasedOnWeather(World world) {
        float newDirection = calculateNewWindDirection();
        int newStrength = calculateNewWindStrength(world);
        setTargetWind(newDirection, newStrength);
    }

    public static void setTargetWind(float direction, int strength) {
        // Record the change of wind direction with its timestamp
        addWindHistoryEntry(currentWindDirection);

        targetWindDirection = direction;
        targetWindStrength.set(strength);
        initialWindStrength = currentWindStrength.get();
        windStrengthChangeStartTime = System.currentTimeMillis();

        System.out.println("Setting target wind to " + direction + " degrees with strength " + strength);
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

    private static int calculateNewWindStrength(World world) {
        // Wind strength is calculated to follow this scale: "https://www.weather.gov/pqr/wind" up to 45mph or 8/Gale
        // It's not really a perfect 1:1
        if (world.isThundering()) {
            return random.nextInt(13) + 33; // 33 -> 46
        } else if (world.isRaining()) {
            return random.nextInt(22) + 10; // 10 -> 32
        } else {
            return random.nextInt(8) + 1;  // 1 -> 9 // world.isClear
        }
    }

    private static class WindHistoryEntry {
        float windDirection;

        WindHistoryEntry(float windDirection) {
            this.windDirection = windDirection;
        }
    }

    private static void playWindSound(World world, PlayerEntity player) {
        float volume = calculateWindVolume(world);
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        world.playSound(null, x, y, z, ModSounds.WIND_SOUND, SoundCategory.AMBIENT, volume, 1.0f);
    }

    private static float calculateWindVolume(World world) {
        if (world.isThundering()) {
            return 0.7f; // Max volume
        } else if (world.isRaining()) {
            return 0.5f; // Medium volume
        } else {
            return 0.2f; // Low volume
        }
    }
}
