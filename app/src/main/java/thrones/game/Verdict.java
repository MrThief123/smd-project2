package thrones.game;

public enum Verdict {
    /** This consideration applies and demands the bot play the card. */
    MUST_PLAY,
    /** This consideration applies and demands the bot pass. */
    MUST_PASS,
    /** This consideration does not apply to the candidate move. */
    NOT_APPLICABLE
}
