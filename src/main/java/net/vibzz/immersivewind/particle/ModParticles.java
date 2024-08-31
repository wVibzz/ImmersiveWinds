package net.vibzz.immersivewind.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.vibzz.immersivewind.wind.WindMod;

public class ModParticles {
    public static final DefaultParticleType WINDWISP_PARTICLE = FabricParticleTypes.simple();

    public static void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(WindMod.MOD_ID, "windwisp"), WINDWISP_PARTICLE);
    }
}
