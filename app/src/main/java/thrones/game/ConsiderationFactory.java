package thrones.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builds a list of {@link Consideration}s from short string keys.
 *
 * <p>The property file format is a comma-separated list of two-letter keys,
 * e.g. {@code players.1.considerations=OA,TM}. Unknown keys are silently
 * skipped so an inadvertent typo doesn't crash the game; the bot will simply
 * not have that consideration active.
 *
 * <p>If the configuration string is missing or empty, this returns the full
 * set of six considerations as a sensible default — otherwise the bot would
 * pass on every turn (per the spec's "no applicable consideration → pass"
 * rule), which is not the intent of running a "legal" bot.
 */
public final class ConsiderationFactory {

    private ConsiderationFactory() {}

    /**
     * Parses a comma-separated list of consideration keys into a list of
     * {@link Consideration} instances. Whitespace is tolerated; unknown keys
     * are skipped.
     *
     * @param configurationString comma-separated keys, e.g. "OA, TM, TD"
     * @return list of considerations (never null; may be empty)
     */
    public static List<Consideration> parse(String configurationString) {
        List<Consideration> result = new ArrayList<>();
        if (configurationString == null || configurationString.trim().isEmpty()) {
            return defaultAll();
        }

        for (String raw : configurationString.split(",")) {
            Consideration c = fromKey(raw.trim());
            if (c != null) result.add(c);
        }
        return result;
    }

    /** Returns one instance of each of the six built-in considerations. */
    public static List<Consideration> defaultAll() {
        return new ArrayList<>(Arrays.asList(
                new OpponentAttackConsideration(),
                new OpponentDefenceConsideration(),
                new OpponentMagicConsideration(),
                new TeamAttackConsideration(),
                new TeamDefenceConsideration(),
                new TeamMagicConsideration()
        ));
    }

    /** Maps a single key to its consideration, or {@code null} if unknown. */
    public static Consideration fromKey(String key) {
        if (key == null) return null;
        switch (key.toUpperCase()) {
            case "OA": return new OpponentAttackConsideration();
            case "OD": return new OpponentDefenceConsideration();
            case "OM": return new OpponentMagicConsideration();
            case "TA": return new TeamAttackConsideration();
            case "TD": return new TeamDefenceConsideration();
            case "TM": return new TeamMagicConsideration();
            default:   return null;
        }
    }
}
