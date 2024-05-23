package net.vibzz.immersivewind.sound;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.vibzz.immersivewind.WindManager;
import net.vibzz.immersivewind.WindMod;

public class ModSounds {

    public static final SoundEvent WIND_SOUND = registerSoundEvent("wind_sound");

    private  static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(WindMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        WindMod.LOGGER.info("Sounds are initialized");
    }

    public static void playWindSound(World world, PlayerEntity player) {
        if (player == null) {
            System.out.println("Player is null, cannot play sound.");
            return;
        }

        float volume = WindManager.calculateWindVolume(world);
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        System.out.println("Attempting to play wind sound at: " + x + ", " + y + ", " + z + " with volume: " + volume);
        world.playSound(null, x, y, z, WIND_SOUND, SoundCategory.AMBIENT, volume, 1.0f);
        System.out.println("Wind sound played for player: " + player.getName().getString());
    }

}
