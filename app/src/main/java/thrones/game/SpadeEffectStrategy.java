package thrones.game;

import ch.aplu.jcardgame.Card;

public class SpadeEffectStrategy implements EffectStrategy {
    @Override
    public void applyEffect(Card current, Card previous, PileAttributes pile) {
        pile.setDefence(pile.getDefence() + calculateCardValue(current, previous));
        pile.setPreviousAffected(PileAttributes.AffectedAttribute.DEFENCE);
    }
}
