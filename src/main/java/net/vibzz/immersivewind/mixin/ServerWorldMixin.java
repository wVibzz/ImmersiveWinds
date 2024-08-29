package net.vibzz.immersivewind.mixin;

import net.vibzz.immersivewind.weather.CustomWeatherType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Unique
    public abstract ServerWorldProperties getLevelProperties();

    @Unique
    public abstract World getWorld();

    @Unique
    private CustomWeatherType currentCustomWeather = CustomWeatherType.CLEAR;
    @Unique
    private int customWeatherTime = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickCustomWeather(CallbackInfo ci) {
        if (customWeatherTime > 0) {
            customWeatherTime--;
            // Implement custom weather behavior depending on the current custom weather type
            switch (currentCustomWeather) {
                case PARTLY_CLOUDY:
                    handlePartlyCloudyWeather();
                    break;
                case CLOUDY:
                    handleCloudyWeather();
                    break;
                case FOGGY:
                    handleFoggyWeather();
                    break;
                case SANDSTORM:
                    handleSandstormWeather();
                    break;
                case SNOW:
                    handleSnowWeather();
                    break;
                case HAIL:
                    handleHailWeather();
                    break;
                case SLEET:
                    handleSleetWeather();
                    break;
                case BLIZZARD:
                    handleBlizzardWeather();
                    break;
                case TORNADO:
                    handleTornadoWeather();
                    break;
                default:
                    break;
            }
        } else {
            currentCustomWeather = CustomWeatherType.CLEAR;
        }
    }

    @Unique
    public void setCustomWeather(CustomWeatherType weatherType, int duration) {
        this.currentCustomWeather = weatherType;
        this.customWeatherTime = duration;
        // Resetting vanilla weather
        this.getLevelProperties().setClearWeatherTime(0);
        this.getLevelProperties().setRainTime(0);
        this.getLevelProperties().setThunderTime(0);
        this.getLevelProperties().setRaining(false);
        this.getLevelProperties().setThundering(false);
    }

    @Unique
    private void handlePartlyCloudyWeather() {
        // Example: Adjust light level, spawn some clouds
        // Code to simulate partly cloudy weather
    }

    @Unique
    private void handleCloudyWeather() {
        // Example: Darken the sky, increase cloud density
        // Code to simulate cloudy weather
    }

    @Unique
    private void handleFoggyWeather() {
        // Example: Reduce visibility, add fog particles
        // Code to simulate foggy weather
    }

    @Unique
    private void handleSandstormWeather() {
        // Example: Add sand particles, reduce visibility, simulate wind
        // Code to simulate sandstorm weather
    }

    @Unique
    private void handleSnowWeather() {
        // Example: Spawn snow particles, lower temperature
        // Code to simulate snow weather
    }

    @Unique
    private void handleHailWeather() {
        // Example: Spawn hail particles, damage entities
        // Code to simulate hail weather
    }

    @Unique
    private void handleSleetWeather() {
        // Example: Mix of rain and snow particles, slippery ground
        // Code to simulate sleet weather
    }

    @Unique
    private void handleBlizzardWeather() {
        // Example: Heavy snow, strong winds, very low visibility
        // Code to simulate blizzard weather
    }

    @Unique
    private void handleTornadoWeather() {
        // Example: High winds, moving debris, damage to blocks and entities
        // Code to simulate tornado weather
    }
}
