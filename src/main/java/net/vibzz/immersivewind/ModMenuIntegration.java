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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModMenuIntegration implements ModMenuApi {

    private static final Logger LOGGER = LogManager.getLogger("ModMenuIntegration");

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("Immersive Wind 0.6"));

            ConfigCategory general = builder.getOrCreateCategory(Text.translatable("General"));
            general.addEntry(builder.entryBuilder()
                    .startBooleanToggle(Text.translatable("Enable Wind Sound"), PlayerWindSoundInstance.enableWind)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> PlayerWindSoundInstance.enableWind = newValue)
                    .build());

            // Group particles by namespace
            Map<String, List<String>> particlesByNamespace = groupParticlesByNamespace();

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
                                if (newValue) {
                                    ParticleBlacklist.addBlacklist(particle);
                                    //LOGGER.info("Added particle {} to blacklist", particle);
                                } else {
                                    ParticleBlacklist.removeBlacklist(particle);
                                    //LOGGER.info("Removed particle {} from blacklist", particle);
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

    private Map<String, List<String>> groupParticlesByNamespace() {
        Map<String, List<String>> particlesByNamespace = new HashMap<>();

        Registries.PARTICLE_TYPE.stream().forEach(particleType -> {
            String particleId = Registries.PARTICLE_TYPE.getId(particleType).toString();
            String namespace = particleId.split(":")[0];

            particlesByNamespace.computeIfAbsent(namespace, k -> new java.util.ArrayList<>()).add(particleId);
        });

        return particlesByNamespace;
    }
}
