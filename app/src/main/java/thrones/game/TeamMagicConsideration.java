package thrones.game;

import ch.aplu.jcardgame.Card;
import thrones.game.Suit;

/**
 * <strong>TeamMagic (TM)</strong>: pass if the selected card is a diamond (♦)
 * and the selected pile belongs to the bot's own team — playing it would
 * decrease the bot's own Attack or Defence, hurting the team.
 */
public class TeamMagicConsideration implements Consideration {

    @Override
    public Verdict evaluate(Card selectedCard, boolean isOwnTeamPile) {
        Suit suit = (Suit) selectedCard.getSuit();
        if (suit.isMagic() && isOwnTeamPile) {
            return Verdict.MUST_PASS;
        }
        return Verdict.NOT_APPLICABLE;
    }
}
