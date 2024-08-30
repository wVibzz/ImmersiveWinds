package net.vibzz.immersivewind.sounds;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.vibzz.immersivewind.wind.WindManager;
import static net.vibzz.immersivewind.wind.WindMod.LOGGER;

public class PlayerWindSoundInstance extends MovingSoundInstance {

    public static boolean enableWind = true; // Add this line to define the enableWind field
    private final PlayerEntity player;
    private float targetVolume;
    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
    public void setZ(float z) {
        this.z = z;
    }

    public PlayerWindSoundInstance(PlayerEntity player, SoundEvent soundEvent) {
        super(soundEvent, SoundCategory.AMBIENT, Random.create());
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = WindManager.calculateWindVolume();
        this.targetVolume = this.volume;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.attenuationType = AttenuationType.LINEAR;
    }

    @Override
    public void tick() {
        if (player.isRemoved()) {
            this.setDone();
            return;
        }

        // Kind of a weird way to do this, but it works as it "disables the sound"
        if (!enableWind) {
            this.volume = 0.0f;
            return;
        }

        this.x = (float) player.getX();
        this.y = (float) player.getY();
        this.z = (float) player.getZ();

        // Update the dimension handling if necessary
        if (this.player.getWorld().getRegistryKey() == World.OVERWORLD) {
            // For example, adjust the volume based on dimension
            this.targetVolume = WindManager.calculateWindVolume();
        } else {
            this.targetVolume = 0.1f * WindManager.calculateWindVolume(); // Different wind in other dimensions
        }

        // Interpolate the volume smoothly towards the target volume
        if (Math.abs(this.volume - this.targetVolume) > 0.01) {
            this.volume = this.volume + (this.targetVolume - this.volume) * 0.1f; // Adjust the interpolation speed as needed
        } else {
            this.volume = this.targetVolume;
        }
    }
}
