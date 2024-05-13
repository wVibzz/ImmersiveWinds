package net.vibzz.immersivewind.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.vibzz.immersivewind.WindManager;
import org.spongepowered.asm.mixin.Final;
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
	@Final
	@Shadow protected ClientWorld world;

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private double modifyDx(double dx) {
        Vec3d windEffect = calculateWindEffect();
		Vec3d particlePos = new Vec3d(this.x, this.y, this.z);
		Vec3d windDirection = new Vec3d(Math.cos(Math.toRadians(WindManager.getWindDirection())), 0, Math.sin(Math.toRadians(WindManager.getWindDirection())));

		double windInfluenceFactor = getWindInfluenceFactor(particlePos, windDirection);
		return dx + windEffect.x * windInfluenceFactor;
	}

	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private double modifyDy(double dy) {
		return dy;  // Return the adjusted dy
	}

	// Method in Particle class to modify the dz component based on wind influence
	@ModifyVariable(method = "move(DDD)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	private double modifyDz(double dz) {
        Vec3d windEffect = calculateWindEffect(); // Ensure this method provides the wind vector based on wind direction and strength
		Vec3d particlePos = new Vec3d(this.x, this.y, this.z);
		Vec3d windDirection = new Vec3d(Math.cos(Math.toRadians(WindManager.getWindDirection())), 0, Math.sin(Math.toRadians(WindManager.getWindDirection())));

		double windInfluenceFactor = getWindInfluenceFactor(particlePos, windDirection);
		return dz + windEffect.z * windInfluenceFactor; // Apply wind influence factor to the z-direction
	}

	@Unique
	private double getWindInfluenceFactor(Vec3d particlePosition, Vec3d windDirection) {
		int range = 5; // Define how far back in the direction from which the wind comes we should check
		Vec3d invertedWindDirection = windDirection.multiply(-1); // Invert wind direction for checking

		for (int i = 1; i <= range; i++) {
			Vec3d checkPosition = particlePosition.add(invertedWindDirection.multiply(i));
			BlockPos pos = new BlockPos((int) checkPosition.x, (int) checkPosition.y, (int) checkPosition.z);
			BlockState state = world.getBlockState(pos);

			// If there is any clear path from the direction the wind is coming from, return full wind influence
			if (state.isAir() || isNonSolidBlock(state)) {
				return 1; // Full influence if wind exposure is confirmed
			}
		}

		// If no clear path is found within the range, no wind influence
		return 0.0;
	}

	@Unique
	private boolean isNonSolidBlock(BlockState state) {
		return state.isOf(Blocks.GLASS) || state.isOf(Blocks.OAK_LEAVES) || state.isOf(Blocks.IRON_BARS) ||
				state.getFluidState().getFluid() == Fluids.WATER || state.getFluidState().getFluid() == Fluids.LAVA;
	}

	@Unique
	private Vec3d calculateWindEffect() {
		double angleRadians = Math.toRadians(WindManager.getWindDirection());
		double windX = Math.cos(angleRadians) * WindManager.getWindStrength() * 0.01;
		double windZ = Math.sin(angleRadians) * WindManager.getWindStrength() * 0.01;
		Vec3d initialWindEffect = new Vec3d(windX, 0, windZ);

		BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
		if (checkForWallInteraction(pos)) {
			return adjustWindFlow(initialWindEffect, pos, windX, windZ);
		}

		return initialWindEffect;
	}

	@Unique
	private boolean checkForWallInteraction(BlockPos particlePos) {
		for (Direction dir : Direction.values()) {
			BlockState state = world.getBlockState(particlePos.offset(dir));
			if (state.isSolidBlock(world, particlePos.offset(dir))) {
				return true; // There is a wall interacting with the wind
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

		// Incorporating randomness to simulate more natural wind behavior
		deflectedWindX += randomizeDeflection(incidenceAngle);
		deflectedWindZ += randomizeDeflection(incidenceAngle);

		return new Vec3d(deflectedWindX, 0, deflectedWindZ);
	}

	@Unique
	private double randomizeDeflection(double incidenceAngle) {
		// Introducing a small random component based on the angle to simulate chaotic airflow
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
		return Direction.EAST; // default, should not happen
	}

	@Unique
	private Direction getWallFacingDirection(BlockPos pos, Direction windDirection) {
		// Check which wall the wind is primarily hitting
		for (Direction dir : Direction.values()) {
			BlockState state = world.getBlockState(pos.offset(dir));
			if (state.isSolidBlock(world, pos.offset(dir)) && dir.getAxis().isHorizontal()) {
				return dir;
			}
		}
		return windDirection; // return windDirection if no wall interaction is dominant
	}

	@Unique
	private double calculateIncidenceAngle(Direction windDirection, Direction wallDirection) {
		int windAngle = directionToAngle(windDirection);
		int wallAngle = directionToAngle(wallDirection);
		int angleDifference = Math.abs(windAngle - wallAngle);

		// Normalize the angle difference to the range [0, 180]
		if (angleDifference > 180) {
			angleDifference = 360 - angleDifference;
		}

		return angleDifference;
	}

	@Unique
	private int directionToAngle(Direction direction) {
		return switch (direction) {
			case NORTH -> 180;
			case SOUTH -> 0;
			case WEST -> 270;
			case EAST -> 90;
			default -> 0; // default to South if something goes wrong
		};
	}

	@Unique
	private double calculateDeflectionFactor(double incidenceAngle, double windX, double windZ) {
		double baseDeflection = 0.01; // base deflection factor
		double velocityFactor = Math.sqrt(windX * windX + windZ * windZ) * 0.01; // scaling deflection based on velocity
		double angleFactor = Math.cos(Math.toRadians(incidenceAngle)); // reduce deflection with sharper angles
		return baseDeflection * angleFactor * velocityFactor;
	}

	@Unique
	private boolean checkForLaminarFlow(double incidenceAngle) {
		// Check if the flow should be laminar based on the angle of incidence
		return incidenceAngle < 45; // Simplified check for laminar flow condition
	}

	@Unique
	private Vec3d adjustWindFlow(Vec3d windEffect, BlockPos pos, double windX, double windZ) {
		Direction windDirection = getWindDirection(windX, windZ);
		Direction wallDirection = getWallFacingDirection(pos, windDirection);
		double incidenceAngle = calculateIncidenceAngle(windDirection, wallDirection);

		if (checkForLaminarFlow(incidenceAngle)) {
			// Laminar flow: wind slides along the wall
			return slideWindAlongWall(windEffect, wallDirection);
		} else {
			// Turbulent flow or deflection
			return deflectWind(windX, windZ, pos);
		}
	}

	@Unique
	private Vec3d slideWindAlongWall(Vec3d windEffect, Direction wallDirection) {
		// Depending on the wall's orientation, adjust the wind's direction to slide along the wall
        return switch (wallDirection) {
            case NORTH, SOUTH ->
                    new Vec3d(windEffect.x, windEffect.y, 0); // Eliminate Z-component for North/South walls
            case EAST, WEST -> new Vec3d(0, windEffect.y, windEffect.z); // Eliminate X-component for East/West walls
            default -> windEffect; // No adjustment if no clear wall interaction
        };
	}

}
