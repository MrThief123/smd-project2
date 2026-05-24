package thrones.game;

// Oh_Heaven.java

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;
import thrones.game.utility.Logger;

import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class GameOfThrones extends CardGame {

    /*
    Canonical String representations of Suit, Rank, Card, and Hand
    */
    String canonical(Suit s) { return s.toString().substring(0, 1); }

    String canonical(Rank r) {
        return r.getCardLog();
    }

    String canonical(Card c) { return canonical((Rank) c.getRank()) + canonical((Suit) c.getSuit()); }

    String canonical(Hand h) {
        return "[" + h.getCardList().stream().map(this::canonical).collect(Collectors.joining(",")) + "]";
    }

    static Random random = new Random(30006);

    private boolean isAuto = false;
    public final int nbPlayers = 4;
    private final int nbHeartCardsPerPlayer = 3;
    private final int nbEffectCardsPerPlayer = 9;
    public final int nbPlays = 2;
    public final int nbRounds = 3;
    private Deck deck = new Deck(Suit.values(), Rank.values(), "cover");
    private final Location[] handLocations = {
            new Location(350, 625),
            new Location(75, 350),
            new Location(350, 75),
            new Location(625, 350)
    };

    private final Location[] scoreLocations = {
            new Location(575, 675),
            new Location(25, 575),
            new Location(25, 25),
            new Location(575, 125)
    };
    private final Location[] pileLocations = {
            new Location(350, 280),
            new Location(350, 430)
    };
    private final Location[] pileStatusLocations = {
            new Location(300, 200),
            new Location(300, 520)
    };

    private enum PlayerType {
        HUMAN,
        RANDOM,
        LEGAL,
        SMART;

        public static PlayerType fromStringToPlayerType(String playerTypeStr) {
            if (playerTypeStr == null || playerTypeStr.trim().isEmpty()) {
                return null; // Invalid input
            }

            try {
                // Case-insensitive conversion
                return PlayerType.valueOf(playerTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // No matching enum constant
                return null;
            }
        }
    }

    private final Actor[] pileTextActors = { null, null };
    private final Actor[] scoreActors = {null, null, null, null};
    private final int watchingTime = 5000;
    private Hand[] hands;
    private Hand[] piles;
    private int[] pileOwners = new int[2];

    private int nextStartingPlayer = random.nextInt(nbPlayers);

    private int[] scores = new int[nbPlayers];
    private Logger logger = new Logger();

    Font bigFont = new Font("Arial", Font.BOLD, 36);
    Font smallFont = new Font("Arial", Font.PLAIN, 10);
    PlayerType[] playerTypes = { PlayerType.HUMAN, PlayerType.HUMAN,  PlayerType.HUMAN, PlayerType.HUMAN };
    private final LegalPlayer[] legalPlayers = new LegalPlayer[4];
    private final SmartPlayer[] smartPlayers = new SmartPlayer[4];
    private int currentPlay = 0;
    private List<Integer> firstPlayers = new ArrayList<>();
    private int NUMBER_OF_PLAYS = 2;
    private List<List<List<String>>> playerAutoMovements = new ArrayList<>();
    private List<Integer> playerMovementIndexes = new ArrayList<>();
    private List<List<List<String>>> initialCardStrings = new ArrayList<>();
    private List<List<List<String>>> initialHeartStrings = new ArrayList<>();

    private List<String> cardListFromKey(Properties properties, String key) {
        String initialCardString = properties.getProperty(key);
        if (initialCardString != null && !initialCardString.isEmpty()) {
            return Arrays.asList(initialCardString.split(","));
        }
        return null;
    }

    /**
     * Initialise object
     */
    private void initWithProperties(Properties properties) {
        isAuto = Boolean.parseBoolean(properties.getProperty("isAuto"));

        for (int i = 0; i < nbPlayers; i++) {
            String rawPlayerConfig = properties.getProperty("players." + i);

            String rawPlayerType = rawPlayerConfig;
            String configurationString = null;

            // Supports format like: legal-od,om,tm
            if (rawPlayerConfig != null && rawPlayerConfig.contains("-")) {
                String[] parts = rawPlayerConfig.split("-", 2);
                rawPlayerType = parts[0];
                configurationString = parts[1];
            }

            playerTypes[i] = PlayerType.fromStringToPlayerType(rawPlayerType);

            if (playerTypes[i] == null) {
                throw new IllegalArgumentException(
                        "Invalid or missing player type for players." + i
                                + ". Raw value was: " + rawPlayerConfig
                );
            }

            playerMovementIndexes.add(0);

            if (playerTypes[i] == PlayerType.LEGAL) {
                String considerationsKey = "players." + i + ".considerations";

                if (configurationString == null) {
                    configurationString = properties.getProperty(considerationsKey);
                }

                legalPlayers[i] = new LegalPlayer(
                        ConsiderationFactory.parse(configurationString)
                );
            }

            if (playerTypes[i] == PlayerType.SMART) {
                smartPlayers[i] = new SmartPlayer();
            }

        }

        for (int i = 0; i < NUMBER_OF_PLAYS; i++) {
            String firstPlayerString = properties.getProperty("plays." + i + ".firstPlayer");
            if (firstPlayerString != null && !firstPlayerString.isEmpty()) {
                firstPlayers.add(Integer.parseInt(firstPlayerString));
            } else {
                firstPlayers.add(-1);
            }
        }

        for (int i = 0; i < NUMBER_OF_PLAYS; i++) {
            initialCardStrings.add(new ArrayList<>());
            playerAutoMovements.add(new ArrayList<>());
            initialHeartStrings.add(new ArrayList<>());

            for (int j = 0; j < nbPlayers; j++) {
                String initialCardKey = "plays." + i + ".players." + j + ".initialCards";
                List<String> initialStrings = cardListFromKey(properties, initialCardKey);
                initialCardStrings.get(i).add(new ArrayList<>());
                if (initialStrings != null) {
                    initialCardStrings.get(i).get(j).addAll(initialStrings);
                }

                String movementCardKey = "plays." + i + ".players." + j + ".cardsPlayed";
                List<String> movementStrings = cardListFromKey(properties, movementCardKey);
                playerAutoMovements.get(i).add(new ArrayList<>());
                if (movementStrings != null) {
                    playerAutoMovements.get(i).get(j).addAll(movementStrings);
                }

                String heartCardKey = "plays." + i + ".players." + j + ".initialHearts";
                List<String> heartStrings = cardListFromKey(properties, heartCardKey);
                initialHeartStrings.get(i).add(new ArrayList<>());
                if (heartStrings != null) {
                    initialHeartStrings.get(i).get(j).addAll(heartStrings);
                }
            }
        }
    }

    public GameOfThrones(Properties properties) {
        super(700, 700, 30);
        initWithProperties(properties);
        setSimulationPeriod(100);
        int version = 1;
        setTitle("Game of Thrones (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
        setStatusText("Initializing...");
        initScore();

        setupGame();
    }

    public String runApp() {
        while(currentPlay < nbPlays) {
            executeAPlay();
            updateScores();

        }

        String text;
        if (scores[0] > scores[1]) {
            text = "Players 0 and 2 won.";
        } else if (scores[0] == scores[1]) {
            text = "All players drew.";
        } else {
            text = "Players 1 and 3 won.";
        }
        System.out.println("Result: " + text);
        setStatusText(text);

        refresh();
        return logger.getAllLog();
    }

    private void sortHand(Hand hand) {
        List<Card> cards = hand.getCardList();
        Comparator<Card> cardComparator = (o1, o2) -> {
            Suit suit1 = (Suit) o1.getSuit();
            Suit suit2 = (Suit) o2.getSuit();
            Rank rank1 = (Rank) o1.getRank();
            Rank rank2 = (Rank) o2.getRank();

            if (suit1.ordinal() - suit2.ordinal() != 0) {
                return suit1.ordinal() - suit2.ordinal();
            }

            return rank1.getShortHandValue() - rank2.getShortHandValue();
        };

        cards.sort(cardComparator);
    }

    // return random Card from Hand
    public static Card randomCard(Hand hand) {
        assert !hand.isEmpty() : " random card from empty hand.";
        int x = random.nextInt(hand.getNumberOfCards());
        return hand.get(x);
    }


    private Rank getRankFromString(String cardName) {
        String rankString = cardName.substring(0, cardName.length() - 1);
        Integer rankValue = Integer.parseInt(rankString);

        for (Rank rank : Rank.values()) {
            if (rank.getShortHandValue() == rankValue) {
                return rank;
            }
        }

        return Rank.ACE;
    }

    private Suit getSuitFromString(String cardName) {
        String suitString = cardName.substring(cardName.length() - 1, cardName.length());

        for (Suit suit : Suit.values()) {
            if (suit.getSuitShortHand().equals(suitString)) {
                return suit;
            }
        }
        return Suit.CLUBS;
    }

    private Card getCardFromList(List<Card> cards, String cardName) {
        Rank existingRank = getRankFromString(cardName);
        Suit existingSuit = getSuitFromString(cardName);
        for (Card card : cards) {
            Suit suit = (Suit) card.getSuit();
            Rank rank = (Rank) card.getRank();
            if (suit.getSuitShortHand().equals(existingSuit.getSuitShortHand())
                    && rank.getShortHandValue() == existingRank.getShortHandValue()) {
                return card;
            }
        }

        return null;
    }

    private void dealingOutHeartCards(Hand[] hands, Hand pack) {
        List<Card> heartCards = pack.getCardsWithSuit(Suit.HEARTS);
        for (int i = 0; i < nbPlayers; i++) {
            int remainingHeartCards = nbHeartCardsPerPlayer;
            if (isAuto) {
                if (!initialHeartStrings.get(currentPlay).get(i).isEmpty()) {
                    List<String> playerHeartStrings = initialHeartStrings.get(currentPlay).get(i);
                    for (String heartString : playerHeartStrings) {
                        Card card = getCardFromList(heartCards, heartString);
                        assert card != null;
                        card.removeFromHand(false);
                        hands[i].insert(card, false);
                        remainingHeartCards--;
                    }
                }
            }

            for (int j = 0; j < remainingHeartCards; j++) {
                int x = random.nextInt(heartCards.size());
                Card randomCard = heartCards.get(x);
                randomCard.removeFromHand(false);
                hands[i].insert(randomCard, false);
            }
        }
    }

    private void dealingOutEffectCards(Hand[] hands, Hand pack) {
        for (int i = 0; i < nbPlayers; i++) {
            int remainingEffectCards = nbEffectCardsPerPlayer;
            if (isAuto) {
                if (!initialCardStrings.get(currentPlay).get(i).isEmpty()) {
                    List<String> playerEffectStrings = initialCardStrings.get(currentPlay).get(i);
                    for (String effectString : playerEffectStrings) {
                        Card card = getCardFromList(pack.getCardList(), effectString);
                        assert card != null;
                        card.removeFromHand(false);
                        hands[i].insert(card, false);
                        remainingEffectCards--;
                    }
                }
            }
            for (int j = 0; j < remainingEffectCards; j++) {
                assert !pack.isEmpty() : " Pack has prematurely run out of cards.";
                Card dealt = randomCard(pack);
                dealt.removeFromHand(false);
                hands[i].insert(dealt, false);
            }
        }
    }

    private void dealingOut(Hand[] hands) {
        Hand pack = deck.toHand(false);
        assert pack.getNumberOfCards() == 52 : " Starting pack is not 52 cards.";
        // Remove 4 Aces
        List<Card> aceCards = pack.getCardsWithRank(Rank.ACE);
        for (Card card : aceCards) {
            card.removeFromHand(false);
        }
        assert pack.getNumberOfCards() == 48 : " Pack without aces is not 48 cards.";
        // Give each player 3 heart cards
        dealingOutHeartCards(hands, pack);

        assert pack.getNumberOfCards() == 36 : " Pack without aces and hearts is not 36 cards.";
        // Give each player 9 of the remaining cards
        dealingOutEffectCards(hands, pack);

        for (int j = 0; j < nbPlayers; j++) {
            sortHand(hands[j]);
            logger.logPlayerCards(hands[j], j);
            assert hands[j].getNumberOfCards() == 12 : " Hand does not have twelve cards.";
        }
    }

    private void initScore() {
        for (int i = 0; i < nbPlayers; i++) {
             scores[i] = 0;
            String text = "P" + i + "-0";
            scoreActors[i] = new TextActor(text, Color.WHITE, bgColor, bigFont);
            addActor(scoreActors[i], scoreLocations[i]);
        }

        String text = "Attack: 0 - Defence: 0";
        for (int i = 0; i < pileTextActors.length; i++) {
            pileTextActors[i] = new TextActor(text, Color.WHITE, bgColor, smallFont);
            addActor(pileTextActors[i], pileStatusLocations[i]);
        }
    }

    private void updateScore(int player) {
        removeActor(scoreActors[player]);
        String text = "P" + player + "-" + scores[player];
        scoreActors[player] = new TextActor(text, Color.WHITE, bgColor, bigFont);
        addActor(scoreActors[player], scoreLocations[player]);
    }

    private void updateScores() {
        for (int i = 0; i < nbPlayers; i++) {
            updateScore(i);
        }
    }

    private Optional<Card> selected;
    private final int NON_SELECTION_VALUE = -1;
    private int selectedPileIndex = NON_SELECTION_VALUE;
    private final int UNDEFINED_INDEX = -1;
    public static final int ATTACK_RANK_INDEX = 0;
    public static final int DEFENCE_RANK_INDEX = 1;
    private void setupGame() {
        hands = new Hand[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            hands[i] = new Hand(deck);
        }
        dealingOut(hands);

        for (int i = 0; i < nbPlayers; i++) {
            hands[i].sort(Hand.SortType.SUITPRIORITY, true);
            System.out.println("hands[" + i + "]: " + canonical(hands[i]));
        }

        for (final Hand currentHand : hands) {
            // Set up human player for interaction
            currentHand.addCardListener(new CardAdapter() {
                public void leftDoubleClicked(Card card) {
                    selected = Optional.of(card);
                    currentHand.setTouchEnabled(false);
                }
                public void rightClicked(Card card) {
                    selected = Optional.empty(); // Don't care which card we right-clicked for player to pass
                    currentHand.setTouchEnabled(false);
                }
            });
        }
        // graphics
        RowLayout[] layouts = new RowLayout[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            int handWidth = 400;
            layouts[i] = new RowLayout(handLocations[i], handWidth);
            layouts[i].setRotationAngle(90 * i);
            hands[i].setView(this, layouts[i]);
            hands[i].draw();
        }
        // End graphics
    }

    private void resetIndexes() {
        for (int i = 0; i < playerMovementIndexes.size(); i++) {
            playerMovementIndexes.set(i, 0);
        }
    }

    private void resetPile() {
        if (piles != null) {
            for (Hand pile : piles) {
                pile.removeAll(true);
            }
        }
        piles = new Hand[2];

        pileOwners[Pile.NORTH.ordinal()] = -1;
        pileOwners[Pile.SOUTH.ordinal()] = -1;

        for (int i = 0; i < 2; i++) {
            piles[i] = new Hand(deck);
            int pileWidth = 40;
            piles[i].setView(this, new RowLayout(pileLocations[i], 8 * pileWidth));
            piles[i].draw();
            final Hand currentPile = piles[i];
            final int pileIndex = i;
            piles[i].addCardListener(new CardAdapter() {
                public void leftClicked(Card card) {
                    selectedPileIndex = pileIndex;
                    currentPile.setTouchEnabled(false);
                }
            });
        }

        updatePileRanks();
    }

    private void pickACorrectSuit(int playerIndex, boolean isCharacter) {
        Hand currentHand = hands[playerIndex];
        List<Card> shortListCards = new ArrayList<>();
        for (int i = 0; i < currentHand.getCardList().size(); i++) {
            Card card = currentHand.getCardList().get(i);
            Suit suit = (Suit) card.getSuit();
            if (suit.isCharacter() == isCharacter) {
                shortListCards.add(card);
            }
        }
        if (shortListCards.isEmpty() || !isCharacter && random.nextInt(3) == 0) {
            selected = Optional.empty();
        } else {
            selected = Optional.of(shortListCards.get(random.nextInt(shortListCards.size())));
        }
    }

    private void selectRandomPile() {
        selectedPileIndex = random.nextInt(2);
    }

    private void waitForCorrectSuit(int playerIndex, boolean isCharacter) {
        if (hands[playerIndex].isEmpty()) {
            selected = Optional.empty();
        } else {
            selected = null;
            hands[playerIndex].setTouchEnabled(true);
            do {
                if (selected == null) {
                    delay(100);
                    continue;
                }
                Suit suit = selected.isPresent() ? (Suit) selected.get().getSuit() : null;
                if (isCharacter && suit != null && suit.isCharacter() ||         // If we want character, can't pass and suit must be right
                        !isCharacter && (suit == null || !suit.isCharacter())) { // If we don't want character, can pass or suit must not be character
                    // if (suit != null && suit.isCharacter() == isCharacter) {
                    break;
                } else {
                    selected = null;
                    hands[playerIndex].setTouchEnabled(true);
                }
                delay(100);
            } while (true);
        }
    }

    private void waitForPileSelection() {
        selectedPileIndex = NON_SELECTION_VALUE;
        for (Hand pile : piles) {
            pile.setTouchEnabled(true);
        }
        while(selectedPileIndex == NON_SELECTION_VALUE) {
            delay(100);
        }
        for (Hand pile : piles) {
            pile.setTouchEnabled(false);
        }
    }

    private int[] calculatePileRanks(int pileIndex) {
        Hand currentPile = piles[pileIndex];
        if (currentPile.isEmpty()) {
            return new int[] { 0, 0 };
        }
        PileAttributes stats = new PileAttributes().calculate(currentPile);
        return new int[] {stats.getAttack(), stats.getDefence()};
    }

    private void updatePileRankState(int pileIndex, int attackRank, int defenceRank) {
        TextActor currentPile = (TextActor) pileTextActors[pileIndex];
        removeActor(currentPile);
        String text = "Attack: " + attackRank + " - Defence: " + defenceRank;
        pileTextActors[pileIndex] = new TextActor(text, Color.WHITE, bgColor, smallFont);
        addActor(pileTextActors[pileIndex], pileStatusLocations[pileIndex]);
    }

    private void updatePileRanks() {
        for (int j = 0; j < piles.length; j++) {
            int[] ranks = calculatePileRanks(j);
            updatePileRankState(j, ranks[ATTACK_RANK_INDEX], ranks[DEFENCE_RANK_INDEX]);
        }
    }

    private int getPlayerIndex(int index) {
        return Math.floorMod(index, nbPlayers);
    }

    /**
     * Returns {@code true} iff the pile at {@code pileIndex} is owned by the
     * same team as {@code playerIndex}. Used by the legal bot to translate
     * its "own team / opponent" perspective into a concrete pile.
     *
     * <p>Teams are: {0, 2} = North team, {1, 3} = South team — i.e. two
     * players share a team iff their indexes have the same parity. The pile
     * owner is the player who laid the heart on that pile (see
     * {@link #pileOwners}).
     */
    private boolean isOwnTeamPile(int playerIndex, int pileIndex) {
        int owner = pileOwners[pileIndex];
        if (owner < 0) return false; // pile not yet claimed; defensive
        return (playerIndex % 2) == (owner % 2);
    }

    private int getOwnPileIndex(int playerIndex) {
        for (int i = 0; i < pileOwners.length; i++) {
            if (isOwnTeamPile(playerIndex, i)) {
                return i;
            }
        }

        return NON_SELECTION_VALUE;
    }

    private int getOpponentPileIndex(int playerIndex) {
        for (int i = 0; i < pileOwners.length; i++) {
            int owner = pileOwners[i];

            if (owner >= 0 && (owner % 2) != (playerIndex % 2)) {
                return i;
            }
        }

        return NON_SELECTION_VALUE;
    }

    private int getAvailableCharacterPileIndex(int preferredPileIndex) {
        if (preferredPileIndex >= 0
                && preferredPileIndex < piles.length
                && pileOwners[preferredPileIndex] == NON_SELECTION_VALUE) {
            return preferredPileIndex;
        }

        for (int i = 0; i < pileOwners.length; i++) {
            if (pileOwners[i] == NON_SELECTION_VALUE) {
                return i;
            }
        }

        return NON_SELECTION_VALUE;
    }

    private SmartBotContext buildSmartBotContext(
            int playerIndex,
            boolean characterSelectionRequired,
            int forcedOwnPileIndex
    ) {
        int ownPileIndex;
        int opponentPileIndex;

        if (characterSelectionRequired) {
            ownPileIndex = forcedOwnPileIndex;
            opponentPileIndex = forcedOwnPileIndex == Pile.NORTH.ordinal()
                    ? Pile.SOUTH.ordinal()
                    : Pile.NORTH.ordinal();
        } else {
            ownPileIndex = getOwnPileIndex(playerIndex);
            opponentPileIndex = getOpponentPileIndex(playerIndex);

            if (ownPileIndex == NON_SELECTION_VALUE || opponentPileIndex == NON_SELECTION_VALUE) {
                throw new IllegalStateException(
                        "Smart bot could not determine pile ownership. " +
                                "playerIndex=" + playerIndex +
                                ", pileOwners=" + Arrays.toString(pileOwners) +
                                ", ownPileIndex=" + ownPileIndex +
                                ", opponentPileIndex=" + opponentPileIndex
                );
            }
        }

        return new SmartBotContext(
                playerIndex,
                hands[playerIndex],
                piles[ownPileIndex],
                piles[opponentPileIndex],
                ownPileIndex,
                opponentPileIndex,
                characterSelectionRequired,
                random
        );
    }

    /**
     * Returns the top (most recently played) card of the pile at
     * {@code pileIndex}, or {@code null} if the pile is empty. Used for the
     * legal bot's "diamond-on-heart" mandatory check.
     */
    private Card topOfPile(int pileIndex) {
        Hand pile = piles[pileIndex];
        List<Card> cards = pile.getCardList();
        if (cards.isEmpty()) return null;
        return cards.get(cards.size() - 1);
    }

    private void applyLegalBotDecision(int playerIndex) {
        if (selected.isEmpty()) {
            return;
        }

        if (playerTypes[playerIndex] != PlayerType.LEGAL) {
            return;
        }

        if (legalPlayers[playerIndex] == null) {
            return;
        }

        Card top = topOfPile(selectedPileIndex);
        boolean ownPile = isOwnTeamPile(playerIndex, selectedPileIndex);

        boolean play = legalPlayers[playerIndex].shouldPlay(
                selected.get(), top, ownPile
        );

        if (!play) {
            selected = Optional.empty();
        }
    }

    private void playHeartForCharacters() {
        nextStartingPlayer = -1;

        if (isAuto) {
            if (firstPlayers.size() > currentPlay) {
                nextStartingPlayer = firstPlayers.get(currentPlay);
            }
        }

        if (nextStartingPlayer < 0) {
            nextStartingPlayer = 0;

            while (hands[nextStartingPlayer].getNumberOfCardsWithSuit(Suit.HEARTS) == 0) {
                nextStartingPlayer = getPlayerIndex(nextStartingPlayer + 1);
            }
        }

        assert hands[nextStartingPlayer].getNumberOfCardsWithSuit(Suit.HEARTS) != 0
                : " Starting player has no hearts.";

        for (int i = 0; i < 2; i++) {
            int pileIndex = i % 2;
            selected = Optional.empty();
            selectedPileIndex = NON_SELECTION_VALUE;

            int playerIndex = getPlayerIndex(nextStartingPlayer + i);
            setStatusText("Player " + playerIndex + " select a Heart card to play");

            if (isAuto) {
                if (playerAutoMovements.size() > currentPlay) {
                    List<List<String>> playersCards = playerAutoMovements.get(currentPlay);

                    if (playersCards.size() > playerIndex) {
                        List<String> movementStrings = playersCards.get(playerIndex);
                        Hand currentHand = hands[playerIndex];
                        int playerMovementIndex = playerMovementIndexes.get(playerIndex);

                        if (movementStrings.size() > playerMovementIndex) {
                            String movementString = movementStrings.get(playerMovementIndex);
                            String[] components = movementString.split("-");

                            String cardString = components[0];
                            int autoPileIndex = Integer.parseInt(components[1]);

                            Card autoCard = getCardFromList(currentHand.getCardList(), cardString);

                            // During character selection, only consume the auto movement if it is a heart.
                            // If it is a heart, also respect the pile from the properties file.
                            // If it is not a heart, leave it for playTurns().
                            if (autoCard != null && ((Suit) autoCard.getSuit()).isCharacter()) {
                                selected = Optional.of(autoCard);

                                // Use the auto pile only if it is available.
                                // Otherwise, force the heart onto the remaining empty character pile.
                                pileIndex = getAvailableCharacterPileIndex(autoPileIndex);

                                playerMovementIndexes.set(playerIndex, playerMovementIndex + 1);
                            }
                        }
                    }
                }
            }

            if (selected.isEmpty()) {
                if (playerTypes[playerIndex] == PlayerType.HUMAN) {
                    waitForCorrectSuit(playerIndex, true);

                } else if (playerTypes[playerIndex] == PlayerType.SMART) {
                    SmartBotContext context = buildSmartBotContext(
                            playerIndex,
                            true,
                            pileIndex
                    );

                    SmartMove move = smartPlayers[playerIndex].chooseMove(context);

                    if (!move.isPass()) {
                        selected = Optional.of(move.getCard());
                    }

                } else {
                    pickACorrectSuit(playerIndex, true);
                }
            }

            assert selected.isPresent() : " Pass returned on selection of character.";

// Safety check: character piles must be distinct.
// If the chosen pile is already owned, place the heart on the remaining empty pile.
            pileIndex = getAvailableCharacterPileIndex(pileIndex);

            if (pileIndex == NON_SELECTION_VALUE) {
                throw new IllegalStateException(
                        "No available character pile. pileOwners=" + Arrays.toString(pileOwners)
                );
            }

            selected.get().setVerso(false);
            selected.get().transfer(piles[pileIndex], true);

            pileOwners[pileIndex] = playerIndex;

            logger.logPlayerMovement(playerIndex, selected.get(), pileIndex);
            updatePileRanks();
        }
    }

    private void playTurns() {
        int remainingTurns = nbPlayers * nbRounds - 2;
        int nextPlayer = (nextStartingPlayer + 2) % nbPlayers;

        while (remainingTurns > 0) {
            selected = Optional.empty();
            selectedPileIndex = NON_SELECTION_VALUE;
            boolean moveWasProvided = false;

            nextPlayer = getPlayerIndex(nextPlayer);

            // Auto movement selection
            if (isAuto) {
                List<List<String>> playersCards = playerAutoMovements.get(currentPlay);

                if (playersCards.size() > nextPlayer) {
                    List<String> movementStrings = playersCards.get(nextPlayer);
                    Hand currentHand = hands[nextPlayer];
                    int playerMovementIndex = playerMovementIndexes.get(nextPlayer);

                    if (movementStrings.size() > playerMovementIndex) {
                        String movementString = movementStrings.get(playerMovementIndex);
                        String[] components = movementString.split("-");

                        String cardString = components[0];
                        selectedPileIndex = Integer.parseInt(components[1]);

                        selected = Optional.ofNullable(
                                getCardFromList(currentHand.getCardList(), cardString)
                        );

                        playerMovementIndexes.set(nextPlayer, playerMovementIndex + 1);
                        moveWasProvided = true;

                        if (selected.isPresent()) {
                            setStatusText("Selected: " + canonical(selected.get()) +
                                    ". Player" + nextPlayer +
                                    " select a pile " + selectedPileIndex +
                                    " to play the card.");

                            applyLegalBotDecision(nextPlayer);
                        }
                    }
                }
            }

            // Only do random/manual selection if no auto move was provided.
            // Do NOT enter here just because selected became empty due to legal pass.
            if (!moveWasProvided) {
                setStatusText("Player" + nextPlayer + " select a non-Heart card to play.");

                if (playerTypes[nextPlayer] == PlayerType.HUMAN) {
                    waitForCorrectSuit(nextPlayer, false);

                    if (selected.isPresent()) {
                        setStatusText("Selected: " + canonical(selected.get()) +
                                ". Player" + nextPlayer +
                                " select a pile to play the card.");

                        waitForPileSelection();
                    }

                } else if (playerTypes[nextPlayer] == PlayerType.SMART) {

                    System.out.println("DEBUG before smart context: player=" + nextPlayer
                            + ", pileOwners=" + Arrays.toString(pileOwners)
                            + ", piles[0]=" + canonical(piles[0])
                            + ", piles[1]=" + canonical(piles[1]));

                    SmartBotContext context = buildSmartBotContext(
                            nextPlayer,
                            false,
                            NON_SELECTION_VALUE
                    );

                    SmartMove move = smartPlayers[nextPlayer].chooseMove(context);

                    if (move.isPass()) {
                        selected = Optional.empty();
                    } else {
                        selected = Optional.of(move.getCard());
                        selectedPileIndex = move.getPileIndex();

                        setStatusText("Selected: " + canonical(selected.get()) +
                                ". Player" + nextPlayer +
                                " select a pile " + selectedPileIndex +
                                " to play the card.");
                    }

                } else {
                    pickACorrectSuit(nextPlayer, false);

                    if (selected.isPresent()) {
                        setStatusText("Selected: " + canonical(selected.get()) +
                                ". Player" + nextPlayer +
                                " select a pile to play the card.");

                        selectRandomPile();
                        applyLegalBotDecision(nextPlayer);
                    }
                }
            }

            if (selected.isEmpty()) {
                System.out.println(". Player" + nextPlayer + " Pass.");
                setStatusText("Pass.");
            }

            if (selected.isPresent()) {
                selected.get().setVerso(false);
                selected.get().transfer(piles[selectedPileIndex], true);
                updatePileRanks();
                logger.logPlayerMovement(nextPlayer, selected.get(), selectedPileIndex);
            }

            nextPlayer = (nextPlayer + 1) % nbPlayers;
            remainingTurns--;
        }
    }

    private void addScoreToTeam(int playerIndex, int scoreValue) {
        scores[getPlayerIndex(playerIndex)] += scoreValue;
        scores[getPlayerIndex(playerIndex + 2)] += scoreValue;
    }

    private void updateScoreForPlayers(int[] pileNorthRanks, int[] pileSouthRanks) {
        System.out.println("pile north: " + canonical(piles[Pile.NORTH.ordinal()]));
        System.out.println("pile north is " + "Attack: " + pileNorthRanks[ATTACK_RANK_INDEX] + " - Defence: " + pileNorthRanks[DEFENCE_RANK_INDEX]);

        System.out.println("pile south: " + canonical(piles[Pile.SOUTH.ordinal()]));
        System.out.println("pile south is " + "Attack: " + pileSouthRanks[ATTACK_RANK_INDEX] + " - Defence: " + pileSouthRanks[DEFENCE_RANK_INDEX]);

        Rank pileNorthCharacterRank = (Rank) piles[Pile.NORTH.ordinal()].getCardList().get(0).getRank();
        Rank pileSouthCharacterRank = (Rank) piles[Pile.SOUTH.ordinal()].getCardList().get(0).getRank();

        int northCharacterScore = pileNorthCharacterRank.getScoreValue();
        int southCharacterScore = pileSouthCharacterRank.getScoreValue();

        int northOwner = pileOwners[Pile.NORTH.ordinal()];
        int southOwner = pileOwners[Pile.SOUTH.ordinal()];

        String character0Result;
        String character1Result;

        // NORTH character attacks SOUTH character
        if (pileNorthRanks[ATTACK_RANK_INDEX] > pileSouthRanks[DEFENCE_RANK_INDEX]) {
            // North attack succeeds, so NORTH team wins SOUTH character points
            addScoreToTeam(northOwner, southCharacterScore);
            character0Result = "Character 0 attack on character 1 succeeded.";
        } else {
            // North attack fails, so SOUTH team keeps/scores SOUTH character points
            addScoreToTeam(southOwner, southCharacterScore);
            character0Result = "Character 0 attack on character 1 failed.";
        }

        // SOUTH character attacks NORTH character
        if (pileSouthRanks[ATTACK_RANK_INDEX] > pileNorthRanks[DEFENCE_RANK_INDEX]) {
            // South attack succeeds, so SOUTH team wins NORTH character points
            addScoreToTeam(southOwner, northCharacterScore);
            character1Result = "Character 1 attack on character 0 succeeded.";
        } else {
            // South attack fails, so NORTH team keeps/scores NORTH character points
            addScoreToTeam(northOwner, northCharacterScore);
            character1Result = "Character 1 attack character 0 failed.";
        }

        updateScores();

        System.out.println(character0Result);
        System.out.println(character1Result);
        setStatusText(character0Result + " " + character1Result);
    }

    private void executeAPlay() {
        resetPile();
        resetIndexes();

        playHeartForCharacters();
        playTurns();

        // 3: calculate winning & update scores for players
        updatePileRanks();
        for (int i = 0; i < piles.length; i++) {
            logger.logPileCards(piles[i], Pile.values()[i]);
        }

        int[] pileNorthRanks = calculatePileRanks(Pile.NORTH.ordinal());
        int[] pileSouthRanks = calculatePileRanks(Pile.SOUTH.ordinal());
        updateScoreForPlayers(pileNorthRanks, pileSouthRanks);
        logger.logScores(pileNorthRanks, pileSouthRanks, scores);

        for (SmartPlayer smartPlayer : smartPlayers) {
            if (smartPlayer != null) {
                smartPlayer.resetForNewPlay();
            }
        }

        nextStartingPlayer += 1;
        currentPlay++;
        delay(watchingTime);
    }

    private int getWatchingTime() {
        if (isAuto) {
            return 100;
        } else {
            return 500;
        }
    }


}
