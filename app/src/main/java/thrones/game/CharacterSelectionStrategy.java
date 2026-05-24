package thrones.game;

import ch.aplu.jcardgame.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Smart-bot strategy for choosing a character card.
 */
public class CharacterSelectionStrategy implements SmartMoveStrategy {

    @Override
    public SmartMove chooseMove(SmartBotContext context) {
        List<Card> hearts = context.getHand().getCardsWithSuit(Suit.HEARTS);

        if (hearts.isEmpty()) {
            return SmartMove.pass();
        }

        Map<Rank, Integer> rankCounts = countRanks(context.getHand().getCardList());

        int bestFrequency = -1;
        int bestRankValue = -1;
        List<Card> tiedBestCards = new ArrayList<>();

        for (Card heart : hearts) {
            Rank rank = (Rank) heart.getRank();
            int frequency = rankCounts.get(rank);
            int rankValue = rank.getScoreValue();

            if (frequency > bestFrequency ||
                    frequency == bestFrequency && rankValue > bestRankValue) {

                bestFrequency = frequency;
                bestRankValue = rankValue;
                tiedBestCards.clear();
                tiedBestCards.add(heart);

            } else if (frequency == bestFrequency && rankValue == bestRankValue) {
                tiedBestCards.add(heart);
            }
        }

        Card selectedHeart;

        if (tiedBestCards.size() == 1) {
            selectedHeart = tiedBestCards.get(0);
        } else {
            int index = context.getRandom().nextInt(tiedBestCards.size());
            selectedHeart = tiedBestCards.get(index);
        }

        return SmartMove.play(selectedHeart, context.getOwnPileIndex());
    }

    private Map<Rank, Integer> countRanks(List<Card> cards) {
        Map<Rank, Integer> counts = new HashMap<>();

        for (Card card : cards) {
            Rank rank = (Rank) card.getRank();
            counts.put(rank, counts.getOrDefault(rank, 0) + 1);
        }

        return counts;
    }
}