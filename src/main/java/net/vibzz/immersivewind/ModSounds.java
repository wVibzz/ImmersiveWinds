package net.vibzz.immersivewind;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public class ModSounds {
    // Define sound events as public static fields
    public static final SoundEvent WIND_WEAK = new SoundEvent(new Identifier("windmod", "wind_weak"));
    public static final SoundEvent WIND_MEDIUM = new SoundEvent(new Identifier("windmod", "wind_medium"));
    public static final SoundEvent WIND_STRONG = new SoundEvent(new Identifier("windmod", "wind_strong"));

    // Static initializer block to register sound events
    static {
        Registry.register(Registry.SOUNT_EVENT, new Identifier("windmod", "wind_weak"), WIND_WEAK);
        Registry.register(Registry.SOUND_EVENT, new Identifier("windmod", "wind_medium"), WIND_MEDIUM);
        Registry.register(Registry.SOUND_EVENT, new Identifier("windmod", "wind_strong"), WIND_STRONG);
    }
}
