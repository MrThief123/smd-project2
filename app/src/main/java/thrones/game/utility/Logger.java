package thrones.game.utility;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.GameOfThrones;
import thrones.game.Pile;
import thrones.game.Rank;
import thrones.game.Suit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    StringBuilder stringBuilder = new StringBuilder();
    private int lineNumber = 1;
    public Logger() {
    }

    /**
     * Log event for testing purpose
     * @param event: the event to log
     */
    public void logEvent(String event) {
        stringBuilder.append(lineNumber + "." + event + "\n");
        lineNumber++;
    }

    private String getCardName(Card card) {
        Suit suit = (Suit) card.getSuit();
        Rank rank = (Rank) card.getRank();
        return rank.getShortHandValue() + suit.getSuitShortHand();
    }

    public void logPlayerCards(Hand hand, int playerIndex) {
        stringBuilder.append(lineNumber + ".P" + playerIndex + ":");
        lineNumber++;
        for (Card card : hand.getCardList()) {
            stringBuilder.append(getCardName(card) + ",");
        }
        stringBuilder.append("\n");
    }

    public void logPlayerMovement(int playerIndex, Card card, int pileIndex) {
        stringBuilder.append(lineNumber + ".P" + playerIndex + ":" +
                getCardName(card) + "-" + pileIndex + "\n");
        lineNumber++;
    }

    public void logPileCards(Hand hand, Pile pile) {
        stringBuilder.append(lineNumber + ".Pile" + pile.toString() + ":");
        lineNumber++;
        for (Card card : hand.getCardList()) {
            stringBuilder.append(getCardName(card) + ",");
        }
        stringBuilder.append("\n");
    }

    public void logScores(int[] pileNorthRanks, int[] pileSouthRanks, int[] scores) {
        stringBuilder.append(lineNumber + ".Values:Pile" + Pile.NORTH + ":" + pileNorthRanks[GameOfThrones.ATTACK_RANK_INDEX] + "-" +
                pileNorthRanks[GameOfThrones.DEFENCE_RANK_INDEX] + ",");
        stringBuilder.append("Pile" + Pile.SOUTH + ":" + pileSouthRanks[GameOfThrones.ATTACK_RANK_INDEX] + "-" +
                pileSouthRanks[GameOfThrones.DEFENCE_RANK_INDEX] + "\n");
        lineNumber++;
        stringBuilder.append(lineNumber + ".Score:" + scores[0] + "," + scores[1] + "," +
                scores[2] + "," + scores[3] + "\n");
        lineNumber++;
    }

    public String getAllLog() {
        return stringBuilder.toString();
    }
}
