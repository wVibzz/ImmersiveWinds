package net.vibzz.immersivewind;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class WindModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerWindEffects();
    }

    public static void registerWindEffects() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                WindManager.interpolateWind(); // Ensure wind direction and strength are interpolated
            }
        });
    }
}
