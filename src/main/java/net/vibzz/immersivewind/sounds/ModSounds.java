package net.vibzz.immersivewind.sounds;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.vibzz.immersivewind.WindMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModSounds {

    public static final SoundEvent WIND_SOUND = registerSoundEvent("wind_sound");
    private static final Map<UUID, PlayerWindSoundInstance> activeSounds = new HashMap<>();

    public static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(WindMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void playWindSound(PlayerEntity player) {
        if (player == null) {
            System.out.println("Player is null, cannot play sound.");
            return;
        }

        UUID playerUUID = player.getUuid();
        MinecraftClient client = MinecraftClient.getInstance();
        SoundManager soundManager = client.getSoundManager();

        // Check if there's already an active sound for this player
        if (activeSounds.containsKey(playerUUID)) {
            PlayerWindSoundInstance existingSound = activeSounds.get(playerUUID);
            if (!existingSound.isDone()) {
                return; // If the sound is still active, do nothing
            }
        }

        // Create and play a new instance of the wind sound
        PlayerWindSoundInstance windSoundInstance = new PlayerWindSoundInstance(player, WIND_SOUND);
        activeSounds.put(playerUUID, windSoundInstance);
        soundManager.play(windSoundInstance);
    }

    public static void registerWindSoundTicker() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                playWindSound(client.player);
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (MinecraftClient.getInstance().player != null) {
                playWindSound(MinecraftClient.getInstance().player);
            }
        });
    }
}
