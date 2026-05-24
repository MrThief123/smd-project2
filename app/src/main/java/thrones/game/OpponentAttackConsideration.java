package thrones.game;

import ch.aplu.jcardgame.Card;
import thrones.game.Suit;

/**
 * <strong>OpponentAttack (OA)</strong>: pass if the selected card is a club
 * (♣) and the selected pile belongs to the opponent — playing it would
 * increase the opponent's Attack value, helping them.
 */
public class OpponentAttackConsideration implements Consideration {

    @Override
    public Verdict evaluate(Card selectedCard, boolean isOwnTeamPile) {
        Suit suit = (Suit) selectedCard.getSuit();
        if (suit.isAttack() && !isOwnTeamPile) {
            return Verdict.MUST_PASS;
        }
        return Verdict.NOT_APPLICABLE;
    }
}
