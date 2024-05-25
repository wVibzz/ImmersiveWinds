package net.vibzz.immersivewind.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.vibzz.immersivewind.WindMod;

public class ModSounds {

    public static final SoundEvent WIND_SOUND = registerSoundEvent();

    public static SoundEvent registerSoundEvent() {
        Identifier id = new Identifier(WindMod.MOD_ID, "wind_sound");
        WindMod.LOGGER.info("Registering sound event: " + id);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void playSound(SoundEvent WIND_SOUND) {
        if (MinecraftClient.getInstance().player != null) {
            WindMod.LOGGER.info("Playing sound: " + WIND_SOUND.getId().toString());
            assert MinecraftClient.getInstance().world != null;
            MinecraftClient.getInstance().getSoundManager().play(
                    new EntityTrackingSoundInstance(WIND_SOUND, SoundCategory.AMBIENT, 1.0F, 1.0F, MinecraftClient.getInstance().player, MinecraftClient.getInstance().world.getRandom().nextLong()));
        }
    }
}
