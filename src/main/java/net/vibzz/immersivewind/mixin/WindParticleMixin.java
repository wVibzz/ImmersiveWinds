package net.vibzz.immersivewind.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.vibzz.immersivewind.WindManager;
import net.vibzz.immersivewind.ParticleConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;



@Mixin(Particle.class)
public abstract class WindParticleMixin {

	@Shadow protected double x;
	@Shadow protected double y;
	@Shadow protected double z;
	@Shadow protected ClientWorld world;

	@Unique
	// Checks if particles are in fluids or if there is a block
	private boolean isUnobstructed(Vec3d particlePosition, Vec3d windDirection) {
		Vec3d checkPosition = particlePosition;
		for (int i = 0; i < 2; i++) { //test wind up to 2 blocks
			checkPosition = checkPosition.subtract(windDirection);
			BlockPos pos = new BlockPos((int) checkPosition.x, (int) checkPosition.y, (int) checkPosition.z);
			BlockState state = world.getBlockState(pos);

			if (state.isSolidBlock(world, pos) ||
					state.getFluidState().getFluid() == Fluids.WATER ||
					state.getFluidState().getFluid() == Fluids.LAVA ||
					state.isOf(Blocks.GLASS) ||
					state.isOf(Blocks.OAK_LEAVES) ||
					state.isOf(Blocks.OAK_FENCE_GATE) ||
					state.isOf(Blocks.DARK_OAK_FENCE) ||
					state.isOf(Blocks.DARK_OAK_FENCE_GATE) ||
					state.isOf(Blocks.GLASS_PANE) ||
					state.isOf(Blocks.IRON_BARS)) {
				return false;
			}
		}
		return true;
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private double modifyDx(double dx) {
		Particle self = (Particle) (Object) this;
		Vec3d windEffect = calculateWindEffect();
		Vec3d particlePos = new Vec3d(this.x, this.y, this.z);
		Vec3d windDirection = new Vec3d(Math.cos(Math.toRadians(WindManager.getWindDirection())), 0, Math.sin(Math.toRadians(WindManager.getWindDirection())));

		if (!ParticleConfig.isExcluded(self.getClass().getSimpleName()) && isUnobstructed(particlePos, windDirection)) {
			return dx + windEffect.x;
		}
		return dx;
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private double modifyDy(double dy) {
		return dy;  // Wind won't affect vertical movement but here code is here if wanted
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	private double modifyDz(double dz) {
		Particle self = (Particle) (Object) this;
		Vec3d windEffect = calculateWindEffect();
		Vec3d particlePos = new Vec3d(this.x, this.y, this.z);
		Vec3d windDirection = new Vec3d(Math.cos(Math.toRadians(WindManager.getWindDirection())), 0, Math.sin(Math.toRadians(WindManager.getWindDirection())));

		if (!ParticleConfig.isExcluded(self.getClass().getSimpleName()) && isUnobstructed(particlePos, windDirection)) {
			return dz + windEffect.z;
		}
		return dz;
	}

	@Unique
	private Vec3d calculateWindEffect() {
		float windDirectionDegrees = WindManager.getWindDirection();
		double angleRadians = Math.toRadians(windDirectionDegrees);
		double windX = Math.cos(angleRadians) * WindManager.getWindStrength() * 0.01;
		double windZ = Math.sin(angleRadians) * WindManager.getWindStrength() * 0.01;
		return new Vec3d(windX, 0, windZ);
	}
}
