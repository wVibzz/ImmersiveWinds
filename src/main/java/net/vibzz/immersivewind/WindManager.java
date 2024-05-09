package net.vibzz.immersivewind;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.World;

public class WindManager {

    public static volatile float currentWindDirection = 0.0f; // Degrees
    public static volatile float targetWindDirection = 0.0f; // Degrees
    public static final AtomicInteger currentWindStrength = new AtomicInteger(1);
    public static final AtomicInteger targetWindStrength = new AtomicInteger(1);
    public static final Random random = new Random();

    public static final long MIN_UPDATE_INTERVAL = 1000; // Minimum interval between updates in milliseconds
    public static long lastWindChangeTime = 0;
    public static final long WIND_CHANGE_COOLDOWN = 10000; // Cooldown period in milliseconds (10 seconds)
    private static final float MAX_DIRECTION_CHANGE_PER_TICK = 5.0f; // Max degrees to change per tick
    private static final float MAX_STRENGTH_CHANGE_PER_TICK = 1f; // Max strength to change per tick

    // Store previous wind directions with their timestamps
    private static List<WindHistoryEntry> windHistory = new ArrayList<>();

    public static void initialize() {
        currentWindDirection = 0.0f; // Set initial direction to North
        currentWindStrength.set(1);  // Set initial Strength to 1
        System.out.println("Wind is initialized");
    }

    public static void updateIfNeeded(World world) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWindChangeTime >= MIN_UPDATE_INTERVAL) {
            updateWindBasedOnWeather(world);
            lastWindChangeTime = currentTime;
        }
        interpolateWind();  // Ensure wind direction and strength are being interpolated every update
    }

    public static void updateWindBasedOnWeather(World world) {
        float newDirection = calculateNewWindDirection(world);
        int newStrength = calculateNewWindStrength(world);
        setTargetWind(newDirection, newStrength);
    }

    public static void setTargetWind(float direction, int strength) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWindChangeTime < WIND_CHANGE_COOLDOWN) {
            return; // Skip the wind change because the cooldown has not elapsed.
        }
        // Record the change of wind direction with its timestamp
        windHistory.add(new WindHistoryEntry(currentTime, currentWindDirection));

        targetWindDirection = direction;
        targetWindStrength.set(strength);
        lastWindChangeTime = currentTime; // Update the last change time
        System.out.println("Setting target wind to " + direction + " degrees with strength " + strength);
    }

    public static void interpolateWind() {
        interpolateDirection();
        interpolateStrength();
    }

    private static void interpolateDirection() {
        float angleDifference = targetWindDirection - currentWindDirection;
        angleDifference -= Math.floor(angleDifference / 360 + 0.5) * 360; // Properly normalize between -180 and 180

        float change = Math.signum(angleDifference) * Math.min(Math.abs(angleDifference), MAX_DIRECTION_CHANGE_PER_TICK);
        currentWindDirection = (currentWindDirection + change + 360) % 360;
    }

    private static void interpolateStrength() {
        int currentStrength = currentWindStrength.get();
        int targetStrength = targetWindStrength.get();
        float strengthDifference = targetStrength - currentStrength;

        float change = Math.signum(strengthDifference) * Math.min(Math.abs(strengthDifference), MAX_STRENGTH_CHANGE_PER_TICK);
        int newStrength = (int) (currentStrength + change); // Cast to int if necessary, or keep as float if all system uses float
        currentWindStrength.set(newStrength);
    }

    public static float getWindDirection() {
        return currentWindDirection;
    }

    public static int getWindStrength() {
        return currentWindStrength.get();
    }

    private static float calculateNewWindDirection(World world) {
        float change = random.nextFloat() * 30 - 15; // This creates a range from -15 to +15
        float newDirection = currentWindDirection + change;
        newDirection = (newDirection + 360) % 360; // Normalize to 0-360 degrees
        return newDirection;
    }

    private static int calculateNewWindStrength(World world) {
        // Wind strength is calculated to follow this scale: "https://www.weather.gov/pqr/wind" up to 45mph or 8/Gale
        // Its not really a perfect 1:1
        if (world.isThundering()) {
            return random.nextInt(13) + 33; // 33 -> 45
        } else if (world.isRaining()) {
            return random.nextInt(20) + 14; // 14 -> 33
        } else {
            return random.nextInt(12) + 1;  // 1 -> 11
        }
    }

    // Simple class to store wind history entries
    private static class WindHistoryEntry {
        long timestamp;
        float windDirection;

        WindHistoryEntry(long timestamp, float windDirection) {
            this.timestamp = timestamp;
            this.windDirection = windDirection;
        }
    }
}
