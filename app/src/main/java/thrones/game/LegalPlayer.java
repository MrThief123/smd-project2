package thrones.game;

import ch.aplu.jcardgame.Card;
import thrones.game.Suit;

import java.util.ArrayList;
import java.util.List;

/**
 * The configurable legal-bot decision-maker (Feature 2 of the project spec).
 *
 * <p>The legal bot does <strong>not</strong> pick its own card or pile. Per the
 * spec, it operates on a <em>randomly suggested</em> (card, pile) pair and
 * decides whether the move is legal-and-acceptable, or whether the bot should
 * pass instead. The random selection mechanism remains the existing
 * {@code pickACorrectSuit} / {@code selectRandomPile} pair in
 * {@link thrones.game.GameOfThrones}; this class is invoked after them.
 *
 * <h2>Decision algorithm</h2>
 * <ol>
 *   <li><strong>Mandatory: diamond-on-heart rule.</strong> If the selected
 *       card is a diamond (♦) and the top card of the selected pile is a
 *       heart (♥), the move is illegal — pass.</li>
 *   <li><strong>Configured considerations.</strong> Iterate the bot's list of
 *       configured {@link Consideration}s. Verdict precedence:
 *       <ul>
 *         <li>If any consideration returns {@link Verdict#MUST_PLAY} → play.</li>
 *         <li>Else if any returns {@link Verdict#MUST_PASS} → pass.</li>
 *         <li>Else (none applicable) → pass, per the spec's "no applicable
 *             consideration" fallback.</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <p>Mandatory rule one from the spec — "play a heart if the team's pile is
 * empty" — is structurally satisfied by the game loop, which already calls
 * the heart-only selection during the first two turns of each play. The legal
 * bot is only consulted during non-heart turns.
 *
 * <h2>Design pattern</h2>
 * Strategy at the per-consideration level (each consideration is a small
 * interchangeable rule object), with this class as the composite-list
 * iterator. See the report for the GRASP justification (Polymorphism,
 * Protected Variations, Pure Fabrication, High Cohesion).
 */
public class LegalPlayer {

    private final List<Consideration> considerations;

    /**
     * @param considerations configured considerations for this bot; may be
     *                       empty (in which case the bot always passes during
     *                       non-heart turns)
     */
    public LegalPlayer(List<Consideration> considerations) {
        this.considerations = new ArrayList<>(considerations);
    }

    /**
     * Decides whether to play the suggested move or pass.
     *
     * @param selectedCard      the random-selected card the bot would play;
     *                          must be non-null (callers handle the
     *                          already-passed case before invoking)
     * @param topOfSelectedPile the top card currently on the target pile
     *                          (used for the diamond-on-heart legality
     *                          check); may be null if the pile is empty
     *                          (defensive — shouldn't happen in normal flow)
     * @param isOwnTeamPile     {@code true} iff the selected pile belongs to
     *                          this bot's own team
     * @return {@code true} to play the move, {@code false} to pass
     */
    public boolean shouldPlay(Card selectedCard, Card topOfSelectedPile, boolean isOwnTeamPile) {
        if (isDiamondOnHeart(selectedCard, topOfSelectedPile)) {
            return false;
        }

        boolean sawMustPass = false;

        for (Consideration consideration : considerations) {
            Verdict verdict = consideration.evaluate(selectedCard, isOwnTeamPile);

            if (verdict == Verdict.MUST_PLAY) {
                return true;
            }

            if (verdict == Verdict.MUST_PASS) {
                sawMustPass = true;
            }
        }

        if (sawMustPass) {
            return false;
        }

        return false;
    }

    /** Returns the configured considerations (defensive copy). */
    public List<Consideration> getConsiderations() {
        return new ArrayList<>(considerations);
    }

    // ---------------------------------------------------------------------

    private static boolean isDiamondOnHeart(Card selectedCard, Card topOfSelectedPile) {
        if (selectedCard == null || topOfSelectedPile == null) return false;
        Suit selectedSuit = (Suit) selectedCard.getSuit();
        Suit topSuit = (Suit) topOfSelectedPile.getSuit();
        return selectedSuit.isMagic() && topSuit.isCharacter();
    }
}
