package thrones.game;
import ch.aplu.jcardgame.Card;

public class SmartMove {

    private final Card card;
    private final int pileIndex;
    private final boolean pass;

    private SmartMove(Card card, int pileIndex, boolean pass) {
        this.card = card;
        this.pileIndex = pileIndex;
        this.pass = pass;
    }

    public static SmartMove play(Card card, int pileIndex) {
        return new SmartMove(card, pileIndex, false);
    }

    public static SmartMove pass() {
        return new SmartMove(null, -1, true);
    }

    public Card getCard() {
        return card;
    }

    public int getPileIndex() {
        return pileIndex;
    }

    public boolean isPass() {
        return pass;
    }

}
