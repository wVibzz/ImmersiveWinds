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
import net.minecraft.world.World;
import net.vibzz.immersivewind.wind.WindMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.vibzz.immersivewind.wind.WindMod.LOGGER;

public class ModSounds {

    public static final SoundEvent WIND_SOUND = registerSoundEvent("wind_sound");
    private static final Map<UUID, PlayerWindSoundInstance> activeSounds = new HashMap<>();
    private static final Map<UUID, World> playerWorldMap = new HashMap<>();

    public static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(WindMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void playWindSound(PlayerEntity player) {
        if (player == null) {
            LOGGER.warn("Player is null, cannot play sound.");
            return;
        }

        UUID playerUUID = player.getUuid();
        MinecraftClient client = MinecraftClient.getInstance();
        SoundManager soundManager = client.getSoundManager();

        // If there's already an active sound for this player, do nothing
        if (activeSounds.containsKey(playerUUID) && !activeSounds.get(playerUUID).isDone()) {
            return;
        }

        // Create and play a new sound instance
        PlayerWindSoundInstance windSoundInstance = new PlayerWindSoundInstance(player, WIND_SOUND);
        activeSounds.put(playerUUID, windSoundInstance);
        soundManager.play(windSoundInstance);
    }

    public static void registerWindSoundTicker() {
        // Register an event for when the player joins a world to start the wind sound
        ClientPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (MinecraftClient.getInstance().player != null) {
                // Stop all existing sounds to avoid audio bugs
                stopAllWindSounds();
                // Play the wind sound when the game is loaded
                playWindSound(MinecraftClient.getInstance().player);
            }
        });

        // Register an event to play wind sound when the player switches dimensions
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                PlayerEntity player = client.player;
                UUID playerUUID = player.getUuid();
                World currentWorld = player.getWorld();

                // Check if the player has switched dimensions
                if (!playerWorldMap.containsKey(playerUUID) || playerWorldMap.get(playerUUID) != currentWorld) {
                    // Stop all existing sounds to avoid conflicts
                    stopAllWindSounds();
                    // Play the wind sound again in the new dimension
                    playWindSound(player);
                }

                // Update the player's current world in the map
                playerWorldMap.put(playerUUID, currentWorld);
            }
        });
    }

    private static void stopAllWindSounds() {
        MinecraftClient client = MinecraftClient.getInstance();
        SoundManager soundManager = client.getSoundManager();

        // Stop all active wind sounds
        for (PlayerWindSoundInstance soundInstance : activeSounds.values()) {
            if (soundInstance != null && !soundInstance.isDone()) {
                soundManager.stop(soundInstance);
            }
        }

        // Clear the active sounds map
        activeSounds.clear();
    }
}
