package thrones.game;

import ch.aplu.jcardgame.Card;
import thrones.game.Suit;

/**
 * <strong>TeamDefence (TD)</strong>: play if the selected card is a spade (♠)
 * and the selected pile belongs to the bot's own team — playing it would
 * increase the bot's Defence value.
 */
public class TeamDefenceConsideration implements Consideration {

    @Override
    public Verdict evaluate(Card selectedCard, boolean isOwnTeamPile) {
        Suit suit = (Suit) selectedCard.getSuit();
        if (suit.isDefence() && isOwnTeamPile) {
            return Verdict.MUST_PLAY;
        }
        return Verdict.NOT_APPLICABLE;
    }
}
