package net.vibzz.immersivewind;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.text.Text;
import net.minecraft.registry.Registries;
import net.vibzz.immersivewind.sounds.PlayerWindSoundInstance;

import java.util.*;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("Immersive Wind 0.7"));

            ConfigCategory general = builder.getOrCreateCategory(Text.translatable("General"));
            general.addEntry(builder.entryBuilder()
                    .startBooleanToggle(Text.translatable("Enable Wind Sound"), PlayerWindSoundInstance.enableWind)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> {
                        if (PlayerWindSoundInstance.enableWind != newValue) {
                            PlayerWindSoundInstance.enableWind = newValue;
                            ModConfig.saveConfig();
                        }
                    })
                    .build());

            // Group particles by namespace and display them by their class names
            Map<String, List<String>> particlesByNamespace = groupParticlesByModCategory();

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // Create a subcategory for each namespace with particle toggle entries
            for (Map.Entry<String, List<String>> entry : particlesByNamespace.entrySet()) {
                String namespace = entry.getKey();
                List<String> particles = entry.getValue();

                SubCategoryBuilder subCategoryBuilder = entryBuilder.startSubCategory(Text.translatable(namespace + " Particle Blacklist"));
                for (String particle : particles) {
                    boolean isBlacklisted = ParticleBlacklist.isBlacklisted(particle);
                    BooleanListEntry toggleEntry = entryBuilder.startBooleanToggle(Text.translatable(particle), isBlacklisted)
                            .setDefaultValue(isBlacklisted)
                            .setSaveConsumer(newValue -> {
                                if (ParticleBlacklist.isBlacklisted(particle) != newValue) {
                                    if (newValue) {
                                        ParticleBlacklist.addBlacklist(particle);
                                    } else {
                                        ParticleBlacklist.removeBlacklist(particle);
                                    }
                                    ModConfig.saveConfig();
                                }
                            })
                            .build();
                    subCategoryBuilder.add(toggleEntry);
                }
                general.addEntry(subCategoryBuilder.build());
            }

            return builder.build();
        };
    }

    private Map<String, List<String>> groupParticlesByModCategory() {
        Map<String, List<String>> particlesByModCategory = new HashMap<>();

        Registries.PARTICLE_TYPE.stream().forEach(particleType -> {
            String particleId = Objects.requireNonNull(Registries.PARTICLE_TYPE.getId(particleType)).toString();
            String[] parts = particleId.split(":");
            String namespace = parts.length > 1 ? parts[0] : "minecraft";
            String particleName = parts.length > 1 ? parts[1] : particleId;

            // Get the mod category from the namespace
            String modCategory = getModCategory(namespace);

            // Format the particle name to its simple name
            String simpleParticleName = ParticleBlacklist.formatParticleName(particleName);

            particlesByModCategory.computeIfAbsent(modCategory, k -> new ArrayList<>()).add(simpleParticleName);
        });

        return particlesByModCategory;
    }

    // Helper method to get the mod category from the namespace
    private String getModCategory(String namespace) {
        // You can implement a custom logic to determine the mod category from the namespace
        // For example, you can use a hardcoded map or a database to store the mod categories
        // For simplicity, let's assume the mod category is the same as the namespace
        return namespace;
    }
}
