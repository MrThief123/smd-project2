package thrones.game;

import ch.aplu.jcardgame.Card;
import thrones.game.Suit;

/**
 * <strong>TeamAttack (TA)</strong>: play if the selected card is a club (♣)
 * and the selected pile belongs to the bot's own team — playing it would
 * increase the bot's Attack value.
 */
public class TeamAttackConsideration implements Consideration {

    @Override
    public Verdict evaluate(Card selectedCard, boolean isOwnTeamPile) {
        Suit suit = (Suit) selectedCard.getSuit();
        if (suit.isAttack() && isOwnTeamPile) {
            return Verdict.MUST_PLAY;
        }
        return Verdict.NOT_APPLICABLE;
    }
}
