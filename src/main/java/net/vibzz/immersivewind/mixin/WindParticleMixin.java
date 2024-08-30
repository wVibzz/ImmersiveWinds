package net.vibzz.immersivewind.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vibzz.immersivewind.wind.WindManager;
import net.vibzz.immersivewind.ParticleBlacklist;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;

import static net.vibzz.immersivewind.wind.WindMod.LOGGER;

@Mixin(Particle.class)
public abstract class WindParticleMixin {

	@Shadow protected double x;
	@Shadow protected double y;
	@Shadow protected double z;
	@Final
	@Shadow protected ClientWorld world;

	@Unique
	private double heatValue = 0.0;

    @ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private double modifyDx(double dx) {
		String particleName = ParticleBlacklist.formatParticleName(this.getClass().getSimpleName());
		if (ParticleBlacklist.isBlacklisted(particleName)) {
			return dx;
		}

		Vec3d windEffect = calculateWindEffect();
		Vec3d particlePos = new Vec3d(this.x, this.y, this.z);
		Vec3d windDirection = new Vec3d(Math.cos(Math.toRadians(WindManager.getWindDirection())), 0, Math.sin(Math.toRadians(WindManager.getWindDirection())));

		double windInfluenceFactor = getWindInfluenceFactor(particlePos, windDirection);
		updateHeatValue(particlePos);
		return dx + windEffect.x * windInfluenceFactor;
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private double modifyDy(double dy) {
		return dy;
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	private double modifyDz(double dz) {
		String particleName = ParticleBlacklist.formatParticleName(this.getClass().getSimpleName());
		if (ParticleBlacklist.isBlacklisted(particleName)) {
			return dz;
		}

		Vec3d windEffect = calculateWindEffect();
		Vec3d particlePos = new Vec3d(this.x, this.y, this.z);
		Vec3d windDirection = new Vec3d(Math.cos(Math.toRadians(WindManager.getWindDirection())), 0, Math.sin(Math.toRadians(WindManager.getWindDirection())));

		double windInfluenceFactor = getWindInfluenceFactor(particlePos, windDirection);
		updateHeatValue(particlePos);
		return dz + windEffect.z * windInfluenceFactor;
	}

	@Unique
	private double getWindInfluenceFactor(Vec3d particlePosition, Vec3d windDirection) {
		int range = 5; // Define how far back in the direction from which the wind comes we should check
		Vec3d invertedWindDirection = windDirection.multiply(-1); // Invert wind direction for checking

		for (int i = 1; i <= range; i++) {
			Vec3d checkPosition = particlePosition.add(invertedWindDirection.multiply(i));
			BlockPos pos = new BlockPos((int) checkPosition.getX(), (int) checkPosition.getY(), (int) checkPosition.getZ());
			BlockState state = world.getBlockState(pos);

			if (state.isAir()) {
				// Check if particle is within 0.5 blocks away from a fluid block
				BlockPos fluidPos = getFluidBlockNearby(pos);
				if (fluidPos != null) {
					Vec3d fluidVec = new Vec3d(fluidPos.getX(), fluidPos.getY(), fluidPos.getZ());
					double distance = particlePosition.distanceTo(fluidVec);
					if (distance < 0.5) {
						return 0.0; // No influence if within 0.5 blocks away from fluid
					}
				}
				return 1; // Full influence if wind exposure is confirmed
			} else if (isNonSolidBlock(state)) {
				return 0.0; // No influence if in water or lava
			}
		}
		return 0.0;
	}

	// New method to get the fluid block nearby
	@Unique
	private BlockPos getFluidBlockNearby(BlockPos pos) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos nearbyPos = pos.add(x, y, z);
					BlockState nearbyState = world.getBlockState(nearbyPos);
					if (isNonSolidBlock(nearbyState)) {
						return nearbyPos;
					}
				}
			}
		}
		return null;
	}

	@Unique
	private boolean isNonSolidBlock(BlockState state) {
		return state.getFluidState().getFluid() == Fluids.WATER || state.getFluidState().getFluid() == Fluids.LAVA;
	}

	@Unique
	private Vec3d calculateWindEffect() {
		if (!world.getRegistryKey().equals(World.OVERWORLD)) {
			return new Vec3d(0, 0, 0); // Return 0 wind strength if not in over-world
		}

		double angleRadians = Math.toRadians(WindManager.getWindDirection());
		double windX = Math.cos(angleRadians) * WindManager.getWindStrength() * 0.01;
		double windZ = Math.sin(angleRadians) * WindManager.getWindStrength() * 0.01;
		Vec3d initialWindEffect = new Vec3d(windX, 0, windZ);

		BlockPos pos = new BlockPos((int) this.x, (int) this.y, (int) this.z);
		return calculateRealisticWindFlow(initialWindEffect, pos);
	}

	@Unique
	private boolean checkForWallInteraction(BlockPos particlePos) {
		for (Direction dir : Direction.values()) {
			BlockState state = world.getBlockState(particlePos.offset(dir));
			if (state.isSolidBlock(world, particlePos.offset(dir))) {
				return true;
			}
		}
		return false;
	}

	@Unique
	private Vec3d deflectWind(double windX, double windZ, BlockPos pos) {
		Direction windDirection = getWindDirection(windX, windZ);
		Direction wallDirection = getWallFacingDirection(pos, windDirection);
		double incidenceAngle = calculateIncidenceAngle(windDirection, wallDirection);
		double deflectionFactor = calculateDeflectionFactor(incidenceAngle, windX, windZ);

		double deflectedWindX = windX * deflectionFactor;
		double deflectedWindZ = windZ * deflectionFactor;

		deflectedWindX += randomizeDeflection(incidenceAngle);
		deflectedWindZ += randomizeDeflection(incidenceAngle);

		return new Vec3d(deflectedWindX, 0, deflectedWindZ);
	}

	@Unique
	private double randomizeDeflection(double incidenceAngle) {
		return Math.random() * Math.cos(Math.toRadians(incidenceAngle)) * 0.05;
	}

	@Unique
	private Direction getWindDirection(double windX, double windZ) {
		double angle = Math.toDegrees(Math.atan2(windZ, windX));
		if (angle < 0) angle += 360;
		if (angle <= 45 || angle > 315) return Direction.EAST;
		if (angle > 45 && angle <= 135) return Direction.SOUTH;
		if (angle > 135 && angle <= 225) return Direction.WEST;
		if (angle > 225) return Direction.NORTH;
		return Direction.EAST;
	}

	@Unique
	private Direction getWallFacingDirection(BlockPos pos, Direction windDirection) {
		for (Direction dir : Direction.values()) {
			BlockState state = world.getBlockState(pos.offset(dir));
			if (state.isSolidBlock(world, pos.offset(dir)) && dir.getAxis().isHorizontal()) {
				return dir;
			}
		}
		return windDirection;
	}

	@Unique
	private double calculateIncidenceAngle(Direction windDirection, Direction wallDirection) {
		int windAngle = directionToAngle(windDirection);
		int wallAngle = directionToAngle(wallDirection);
		int angleDifference = Math.abs(windAngle - wallAngle);

		if (angleDifference > 180) {
			angleDifference = 360 - angleDifference;
		}

		return angleDifference;
	}

	@Unique
	private int directionToAngle(Direction direction) {
		return switch (direction) {
			case NORTH -> 180;
			case WEST -> 270;
			case EAST -> 90;
			default -> 0; // SOUTH
		};
	}

	@Unique
	private double calculateDeflectionFactor(double incidenceAngle, double windX, double windZ) {
		double baseDeflection = 0.01;
		double velocityFactor = Math.sqrt(windX * windX + windZ * windZ) * 0.01;
		double angleFactor = Math.cos(Math.toRadians(incidenceAngle));
		return baseDeflection * angleFactor * velocityFactor;
	}

	@Unique
	private boolean checkForLaminarFlow(double incidenceAngle) {
		return incidenceAngle < 45;
	}

	@Unique
	private Vec3d adjustWindFlow(Vec3d windEffect, BlockPos pos, double windX, double windZ) {
		Direction windDirection = getWindDirection(windX, windZ);
		Direction wallDirection = getWallFacingDirection(pos, windDirection);
		double incidenceAngle = calculateIncidenceAngle(windDirection, wallDirection);

		if (checkForLaminarFlow(incidenceAngle)) {
			return slideWindAlongWall(windEffect, wallDirection);
		} else {
			return deflectWind(windX, windZ, pos);
		}
	}

	@Unique
	private Vec3d slideWindAlongWall(Vec3d windEffect, Direction wallDirection) {
		return switch (wallDirection) {
			case NORTH, SOUTH -> new Vec3d(windEffect.x, windEffect.y, 0);
			case EAST, WEST -> new Vec3d(0, windEffect.y, windEffect.z);
			default -> windEffect;
		};
	}

	@Unique
	private Vec3d funnelWindAroundStructure(Vec3d windEffect, BlockPos pos) {
		Direction windDirection = getWindDirection(windEffect.x, windEffect.z);
		Direction wallDirection = getWallFacingDirection(pos, windDirection);
		double incidenceAngle = calculateIncidenceAngle(windDirection, wallDirection);

		if (incidenceAngle >= 45 && incidenceAngle <= 135) {
			double funnelFactor = 1.0 + (1.0 - Math.cos(Math.toRadians(incidenceAngle))) * 0.5;
			return windEffect.multiply(funnelFactor);
		}

		return windEffect;
	}

	@Unique
	private boolean isNearTunnel(BlockPos pos) {
		int airCount = 0;
		int solidCount = 0;

		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos checkPos = pos.add(dx, dy, dz);
					BlockState state = world.getBlockState(checkPos);

					if (state.isAir()) {
						airCount++;
					} else if (state.isSolidBlock(world, checkPos)) {
						solidCount++;
					}
				}
			}
		}

		return airCount >= 15 && solidCount >= 10;
	}

	@Unique
	private Vec3d adjustForTunnelAttraction(Vec3d windEffect, BlockPos pos) {
		if (isNearTunnel(pos)) {
			double attractionFactor = 1.5;
			return windEffect.multiply(attractionFactor);
		}
		return windEffect;
	}

	@Unique
	private Vec3d calculateRealisticWindFlow(Vec3d windEffect, BlockPos pos) {
		if (checkForWallInteraction(pos)) {
			windEffect = adjustWindFlow(windEffect, pos, windEffect.x, windEffect.z);
		}
		windEffect = funnelWindAroundStructure(windEffect, pos);
		return adjustForTunnelAttraction(windEffect, pos);
	}

	@Unique
	private void updateHeatValue(Vec3d particlePos) {
		BlockPos pos = new BlockPos((int) particlePos.x, (int) particlePos.y, (int) particlePos.z);
		double maxHeatInfluenceDistance = 5.0;
		double heatValueIncrement = 0.05;
		//double heatValueDecrement = 0.01;

		// Reset heat value for the new tick
		heatValue = 0.0;

		BlockPos.Mutable checkPos = new BlockPos.Mutable();
		checkPos.set(pos);

		for (; checkPos.getY() >= 0; checkPos.move(Direction.DOWN)) {
			BlockState state = world.getBlockState(checkPos);
			if (isHeatSource(state)) {
				double distance = particlePos.distanceTo(new Vec3d(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5));
				if (distance <= maxHeatInfluenceDistance) {
					double influence = (maxHeatInfluenceDistance - distance) / maxHeatInfluenceDistance;
					heatValue += heatValueIncrement * influence;
                    // Max heat that can be accumulated
                    double maxHeatValue = 1.0;
                    heatValue = Math.min(heatValue, maxHeatValue); // Cap the heat value to maxHeatValue
					break; // Break after finding the first heat source for performance
				}
			}
		}

		if (heatValue > 0) {
			double motionY = calculateLift();
			setMotion(motionY);
		} else {
			setMotion(0);
		}
	}

	@Unique
	private double calculateLift() {
		return heatValue * 0.1; // Multiply heatValue by a factor to determine lift strength
	}

	@Unique
	private boolean isHeatSource(BlockState state) {
		return state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE) || state.isOf(Blocks.TORCH) || state.isOf(Blocks.CAMPFIRE);
	}

	@Unique
	private void setMotion(double motionY) {
		this.y += motionY;
	}
}
