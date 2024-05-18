package net.vibzz.immersivewind.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
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
}
