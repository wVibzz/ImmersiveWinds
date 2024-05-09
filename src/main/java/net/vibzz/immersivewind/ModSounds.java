package net.vibzz.immersivewind;

import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;

public class ModSounds {
    // Define sound events as public static fields
    public static final SoundEvents WIND_WEAK = new SoundEvents();
    public static final SoundEvents WIND_MEDIUM = new SoundEvents();
    public static final SoundEvents WIND_STRONG = new SoundEvents();

    // Static initializer block to register sound events
    static {
        Registry.register(Registries.SOUND_EVENT, new Identifier("windmod", "wind_weak"), WIND_WEAK);
        Registry.register(Registries.SOUND_EVENT, new Identifier("windmod", "wind_medium"), WIND_MEDIUM);
        Registry.register(Registries.SOUND_EVENT, new Identifier("windmod", "wind_strong"), WIND_STRONG);
    }
}
