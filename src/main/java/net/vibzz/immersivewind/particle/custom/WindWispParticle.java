package net.vibzz.immersivewind.particle.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.particle.DefaultParticleType;
import net.vibzz.immersivewind.wind.WindManager;

import static net.vibzz.immersivewind.wind.WindMod.LOGGER;

public class WindWispParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    private final double radius;
    private final PlayerEntity player;

    protected WindWispParticle(ClientWorld level, double xCoord, double yCoord, double zCoord, SpriteProvider spriteProvider, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.setPos(x, y, z);

        this.scale = 0.5F;
        this.age = 0;
        this.maxAge = 27;

        // Get the current wind direction and strength
        float windDirection = WindManager.getWindDirection();
        float windStrength = WindManager.getWindStrength();

        // Set the velocity based on the wind direction and strength
        this.velocityX = Math.cos(Math.toRadians(windDirection)) * windStrength;
        this.velocityZ = Math.sin(Math.toRadians(windDirection)) * windStrength;

        this.radius = 15;
        this.player = MinecraftClient.getInstance().player;

        this.spriteProvider = spriteProvider;
        this.setSprite(this.spriteProvider);
    }

    @Override
    public void tick() {
        this.age++;
        this.setSpriteForAge(this.spriteProvider);

        double distance = player.squaredDistanceTo(this.x, this.y, this.z);

        // If the particle exceeds its max age or the distance threshold, mark it as dead
        if (this.age >= this.maxAge || distance > radius * radius) {
            this.markDead();
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(DefaultParticleType particleType, ClientWorld level, double x, double y, double z, double dx, double dy, double dz) {
            return new WindWispParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
