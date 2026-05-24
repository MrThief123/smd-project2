package thrones.game;

import ch.aplu.jcardgame.Card;
import thrones.game.Suit;

/**
 * <strong>OpponentDefence (OD)</strong>: pass if the selected card is a spade
 * (♠) and the selected pile belongs to the opponent — playing it would
 * increase the opponent's Defence value, helping them.
 */
public class OpponentDefenceConsideration implements Consideration {

    @Override
    public Verdict evaluate(Card selectedCard, boolean isOwnTeamPile) {
        Suit suit = (Suit) selectedCard.getSuit();
        if (suit.isDefence() && !isOwnTeamPile) {
            return Verdict.MUST_PASS;
        }
        return Verdict.NOT_APPLICABLE;
    }
}
