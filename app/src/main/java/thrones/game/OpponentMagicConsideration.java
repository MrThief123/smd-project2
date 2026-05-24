package thrones.game;

import ch.aplu.jcardgame.Card;
import thrones.game.Suit;

/**
 * <strong>OpponentMagic (OM)</strong>: play if the selected card is a diamond
 * (♦) and the selected pile belongs to the opponent — playing it would
 * decrease the opponent's Attack or Defence, hurting them.
 */
public class OpponentMagicConsideration implements Consideration {

    @Override
    public Verdict evaluate(Card selectedCard, boolean isOwnTeamPile) {
        Suit suit = (Suit) selectedCard.getSuit();
        if (suit.isMagic() && !isOwnTeamPile) {
            return Verdict.MUST_PLAY;
        }
        return Verdict.NOT_APPLICABLE;
    }
}
