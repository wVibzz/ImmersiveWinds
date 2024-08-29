package net.vibzz.immersivewind.mixin;

import net.vibzz.immersivewind.weather.CustomWeatherType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.WeatherCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

@Mixin(WeatherCommand.class)
public class WeatherCommandMixin {

    @Inject(method = "register", at = @At("HEAD"))
    private static void registerCustomWeather(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
        dispatcher.register(literal("weather")
                .then(argument("type", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (CustomWeatherType type : CustomWeatherType.values()) {
                                builder.suggest(type.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String type = StringArgumentType.getString(context, "type").toUpperCase();
                            ServerWorld world = context.getSource().getWorld();

                            try {
                                CustomWeatherType customWeather = CustomWeatherType.valueOf(type);
                                handleCustomWeather(customWeather, world, context.getSource());
                                context.getSource().sendFeedback((Supplier<Text>) Text.literal("Changed weather to " + type).formatted(Formatting.GREEN), true);
                                return 1;
                            } catch (IllegalArgumentException e) {
                                context.getSource().sendError(Text.literal("Unknown weather type: " + type).formatted(Formatting.RED));
                                return 0;
                            }
                        })
                )
        );
    }

    @Unique
    private static void handleCustomWeather(CustomWeatherType weather, ServerWorld world, ServerCommandSource source) {
        // Implement the logic to trigger the custom weather types in the world
        switch (weather) {
            case CLEAR:
                // Set the world to clear weather
                world.setWeather(0, 6000, false, false);
                break;
            case RAIN:
                world.setWeather(0, 6000, true, false);
                break;
            case THUNDER:
                world.setWeather(0, 6000, true, true);
                break;
            case PARTLY_CLOUDY:
                // Implement partly cloudy weather logic
                break;
            case CLOUDY:
                // Implement cloudy weather logic
                break;
            case FOGGY:
                // Implement foggy weather logic
                break;
            case SANDSTORM:
                // Implement sandstorm weather logic
                break;
            case SNOW:
                // Implement snow weather logic
                break;
            case HAIL:
                // Implement hail weather logic
                break;
            case SLEET:
                // Implement sleet weather logic
                break;
            case BLIZZARD:
                // Implement blizzard weather logic
                break;
            case TORNADO:
                // Implement tornado weather logic
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + weather);
        }
    }
}
