package thrones.game;
import ch.aplu.jcardgame.Card;

public class MoveCandidate {

    private final Card card;
    private final int pileIndex;
    private final int effectValue;
    private final MoveCategory category;

    public MoveCandidate(Card card, int pileIndex, int effectValue, MoveCategory category) {
        this.card = card;
        this.pileIndex = pileIndex;
        this.effectValue = effectValue;
        this.category = category;
    }

    public Card getCard() {
        return card;
    }

    public int getPileIndex() {
        return pileIndex;
    }

    public int getEffectValue() {
        return effectValue;
    }

    public MoveCategory getCategory() {
        return category;
    }

    public SmartMove toSmartMove() {
        return SmartMove.play(card, pileIndex);
    }
}
