package net.vibzz.immersivewind;

import java.util.ArrayList;
import java.util.LinkedList;
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

    public static long lastWindChangeTime = 0;
    public static final long WIND_CHANGE_COOLDOWN = 12000; // Cooldown period in milliseconds (10 seconds)
    private static final float MAX_DIRECTION_CHANGE_PER_TICK = 6.0f; // Max degrees to change per tick
    private static final float MAX_STRENGTH_CHANGE_PER_TICK = 1f; // Max strength to change per tick

    private static volatile int previousWeatherState // 0 - clear, 1 - rain, 2 - thunder
    private static final LinkedList<WindHistoryEntry> windHistory = new LinkedList<>();
    private static final int MAX_HISTORY_SIZE = 10; // Store up to 100 history entries

    public static void initialize() {
        previousWeatherState = -1; // Set weather to excluded state to force initial weather update
        currentWindDirection = 0.0f; // Set initial direction to North
        currentWindStrength.set(1);  // Set initial Strength to 1
        System.out.println("Wind is initialized");
    }

    private static int getCurrentWeatherState(World world) {
        if (world.isThundering()) {
            return 2;
        } else if (world.isRaining()) {
            return 1;
        } else {
            return 0;
        }
    }

    public static void updateIfNeeded(World world) {
        if (previousWeatherState != getCurrentWeatherState(world)) {
            updateWindBasedOnWeather(world);
            previousWeatherState = getCurrentWeatherState(world);
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
        addWindHistoryEntry(currentTime, currentWindDirection);

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

    public static void addWindHistoryEntry(long timestamp, float windDirection) {
        if (windHistory.size() >= MAX_HISTORY_SIZE) {
            windHistory.removeFirst(); // Remove the oldest entry to maintain size
        }
        windHistory.addLast(new WindHistoryEntry(timestamp, windDirection));
    }

    public static List<WindHistoryEntry> getWindHistory() {
        return new ArrayList<>(windHistory); // Return a copy of the history
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
            return random.nextInt(13) + 32; // 32 -> 44
        } else if (world.isRaining()) {
            return random.nextInt(20) + 12; // 12 -> 31
        } else {
            return random.nextInt(10) + 1;  // 1 -> 11
        }
    }

    private static class WindHistoryEntry {
        long timestamp;
        float windDirection;

        WindHistoryEntry(long timestamp, float windDirection) {
            this.timestamp = timestamp;
            this.windDirection = windDirection;
        }
    }
}
