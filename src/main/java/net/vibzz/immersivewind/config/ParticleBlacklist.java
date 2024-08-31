package net.vibzz.immersivewind.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParticleBlacklist {
    private static final Set<String> BLACKLIST = new HashSet<>();
    private static final Set<Pattern> BLACKLIST_PATTERNS = new HashSet<>();

    public static boolean isBlacklisted(String particle) {
        String formattedParticle = formatParticleName(particle);
        return BLACKLIST_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(formattedParticle).matches());
    }

    public static void addBlacklist(String particle) {
        String formattedParticle = formatParticleName(particle);
        BLACKLIST.add(formattedParticle);
        BLACKLIST_PATTERNS.add(Pattern.compile(".*" + formattedParticle + ".*", Pattern.CASE_INSENSITIVE));
    }

    public static void removeBlacklist(String particle) {
        String formattedParticle = formatParticleName(particle);
        BLACKLIST.remove(formattedParticle);
        BLACKLIST_PATTERNS.removeIf(pattern -> pattern.pattern().contains(formattedParticle));
    }

    public static List<String> getBlacklist() {
        return List.copyOf(BLACKLIST);
    }

    public static void setBlacklist(List<String> blacklist) {
        BLACKLIST.clear();
        BLACKLIST_PATTERNS.clear();
        BLACKLIST.addAll(blacklist);
        BLACKLIST_PATTERNS.addAll(blacklist.stream()
                .map(name -> Pattern.compile(".*" + name + ".*", Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toSet()));
    }

    public static String formatParticleName(String simpleName) {
        StringBuilder formattedName = new StringBuilder();
        for (char c : simpleName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!formattedName.isEmpty()) {
                    formattedName.append("_");
                }
                formattedName.append(Character.toLowerCase(c));
            } else {
                formattedName.append(c);
            }
        }
        return formattedName.toString();
    }
}
