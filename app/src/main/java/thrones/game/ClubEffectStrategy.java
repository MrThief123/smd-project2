package thrones.game;

import ch.aplu.jcardgame.Card;

public class ClubEffectStrategy implements EffectStrategy {
    @Override
    public void applyEffect(Card current, Card previous, PileAttributes pile) {
        pile.setAttack(pile.getAttack() + calculateCardValue(current, previous));
        pile.setPreviousAffected(PileAttributes.AffectedAttribute.ATTACK);
    }
}
