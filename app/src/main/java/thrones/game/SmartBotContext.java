package thrones.game;
import ch.aplu.jcardgame.Hand;
import java.util.Random;

public class SmartBotContext {

    private final int playerIndex;
    private final Hand hand;

    private final Hand ownPile;
    private final Hand opponentPile;

    private final int ownPileIndex;
    private final int opponentPileIndex;

    private final PileAttributes ownAttributes;
    private final PileAttributes opponentAttributes;

    private final boolean characterSelectionRequired;
    private final Random random;

    public SmartBotContext(
            int playerIndex,
            Hand hand,
            Hand ownPile,
            Hand opponentPile,
            int ownPileIndex,
            int opponentPileIndex,
            boolean characterSelectionRequired,
            Random random
    ) {
        this.playerIndex = playerIndex;
        this.hand = hand;
        this.ownPile = ownPile;
        this.opponentPile = opponentPile;
        this.ownPileIndex = ownPileIndex;
        this.opponentPileIndex = opponentPileIndex;
        this.characterSelectionRequired = characterSelectionRequired;
        this.random = random;

        this.ownAttributes = new PileAttributes().calculate(ownPile);
        this.opponentAttributes = new PileAttributes().calculate(opponentPile);
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public Hand getHand() {
        return hand;
    }

    public Hand getOwnPile() {
        return ownPile;
    }

    public Hand getOpponentPile() {
        return opponentPile;
    }

    public int getOwnPileIndex() {
        return ownPileIndex;
    }

    public int getOpponentPileIndex() {
        return opponentPileIndex;
    }

    public PileAttributes getOwnAttributes() {
        return ownAttributes;
    }

    public PileAttributes getOpponentAttributes() {
        return opponentAttributes;
    }

    public boolean isCharacterSelectionRequired() {
        return characterSelectionRequired;
    }

    public Random getRandom() {
        return random;
    }
}
