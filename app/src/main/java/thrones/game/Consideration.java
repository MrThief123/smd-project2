package thrones.game;

import ch.aplu.jcardgame.Card;

/**
 * One configurable "consideration" used by the legal bot when deciding whether
 * to play a randomly suggested (card, pile) move or to pass.
 *
 * <p>This is the GoF Strategy pattern: each consideration encapsulates one
 * decision rule from the spec, sharing a uniform interface so {@link
 * thrones.game.player.LegalPlayer} can iterate a configured list without
 * branching on which consideration is active.
 *
 * <p>The six concrete considerations specified in Section 3.2 of the project
 * spec — OA, OD, OM, TA, TD, TM — are implemented as one class each
 * (see {@link OpponentAttackConsideration} etc.). The spec notes that "more
 * considerations may be added" in future; that extension point is exactly
 * what this interface protects (GRASP Protected Variations).
 */
public interface Consideration {

    /**
     * Returns this consideration's verdict on whether to play or pass the
     * candidate move.
     *
     * @param selectedCard   the randomly suggested card the bot would play
     * @param isOwnTeamPile  {@code true} if the candidate target pile belongs
     *                       to the bot's own team, {@code false} if it
     *                       belongs to the opposing team
     * @return one of {@link Verdict#MUST_PLAY}, {@link Verdict#MUST_PASS}, or
     *         {@link Verdict#NOT_APPLICABLE}
     */
    Verdict evaluate(Card selectedCard, boolean isOwnTeamPile);
}
