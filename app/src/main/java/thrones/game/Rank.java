package thrones.game;

public enum Rank {
    ACE(0, 1),
    KING(10, 13),
    QUEEN(10, 12),
    JACK(10, 11),
    TEN(10, 10), NINE(9, 9), EIGHT(8, 8), SEVEN(7, 7), SIX(6, 6),
    FIVE(5, 5), FOUR(4, 4), THREE(3, 3), TWO(2, 2);

    private int scoreValue = 0;
    private int shortHandValue = 0;

    Rank(int scoreValue, int shortHandValue) {
        this.scoreValue = scoreValue;
        this.shortHandValue = shortHandValue;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public int getShortHandValue() {
        return shortHandValue;
    }

    public String getCardLog() {
        return String.format("%d", shortHandValue);
    }
}