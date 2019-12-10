package hangman.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Class used to model a hangman game. Once instantiated, the methods for guessing are used
 * to model player guesses and the state of the game, including the current drawing, are queried for
 * using various methods.
 */
public class Game {
    public static int DEFAULT_ALLOWED_FAILS;
    private static List<String> GRAPHIC_STRINGS;

    private char[] actualString;
    private char[] displayString;
    private int nrFails;
    private List<String> currentGraphicStrings;
    private Set<String> guessedWords;
    private Set<Character> guessedLetters;

    /* Initialize list of all graphics strings,
    and set the default nr of fails allowed to the maximum number possible */
    static {
        GRAPHIC_STRINGS = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File("src/data/hangman-ascii"));
            int height = Integer.parseInt(sc.nextLine());
            int length = Integer.parseInt(sc.nextLine());
            for(int i = 0; i < length; i++) {
                StringBuilder sb = new StringBuilder();
                for(int j = 0; j < height; j++) {
                    sb.append(sc.nextLine());
                    sb.append(j == height - 1 ? "" : "\n");
                }
                GRAPHIC_STRINGS.add(sb.toString());
            }
            DEFAULT_ALLOWED_FAILS = GRAPHIC_STRINGS.size() - 2; // max number of fails possible
        } catch (FileNotFoundException e) {
            // Catastrophic failure
            System.out.println("Failed to find ascii art file. Exiting...");
            System.exit(0);
        }
    }

    /**
     * Initialize a game.
     * @param gameString The word to guess
     * @param failsAllowed The number of failed attempts allowed. For example, if failsAllowed = 5,
     *                       the game is lost after 6 incorrect guesses.
     */
    Game(String gameString, int failsAllowed) {
        initialize(gameString, failsAllowed);
    }

    /**
     * Initialize a game with default number of rounds.
     * @param gameString The word to guess
     */
    Game(String gameString) {
        initialize(gameString, DEFAULT_ALLOWED_FAILS);
    }

    // Set up an instance. Only call in constructor.
    private void initialize(String gameString, int failsAllowed) {
        if (gameString.equals("") || failsAllowed < 1 || failsAllowed > DEFAULT_ALLOWED_FAILS) {
            throw new IllegalArgumentException("gameString cannot be empty, failsAllowed must be in range "
                + "[1, " + DEFAULT_ALLOWED_FAILS + "]");
        }

        guessedLetters = new HashSet<>();
        guessedWords = new HashSet<>();

        nrFails = 0;

        // If we allow n fails, we need n + 2 graphics images
        currentGraphicStrings = GRAPHIC_STRINGS.subList(
                GRAPHIC_STRINGS.size() - (failsAllowed + 2), GRAPHIC_STRINGS.size());

        actualString = gameString.toCharArray();
        displayString = new char[gameString.length()];
        Arrays.fill(displayString, '_');
        for(int i = 0; i < actualString.length; i++) {
            if(actualString[i] == ' ') displayString[i] = ' ';
        }

    }

    /**
     * Guess for a letter. Throws an UnsupportedOperationException if the game is over.
     * @param guess The letter to guess for.
     * @return true if the guess produced new letters in the display string, false otherwise.
     */
    public boolean guessChar(char guess) throws UnsupportedOperationException {
        checkGameOver();
        // If already guessed, consider it a fail
        if(!guessedLetters.add(guess)) {
            nrFails++;
            return false;
        }
        boolean success = false;
        for(int i = 0; i < actualString.length; i++) {
            if (actualString[i] == guess) {
                displayString[i] = guess;
                success = true;
            }
        }
        nrFails += success ? 0 : 1;
        return success;
    }

    /**
     * Guesses for a whole word. Throws an UnsupportedOperationException if the game is over.
     * @param guess The word to guess for
     * @return True if correct, false if incorrect.
     */
    public boolean guessWord(String guess) {
        checkGameOver();
        // If already guessed, consider it a failure
        if(!guessedWords.add(guess)) {
            nrFails++; // Return false if already guessed
        }
        boolean success = new String(actualString).equals(guess);
        if(success) displayString = Arrays.copyOf(actualString, actualString.length);
        nrFails += success ? 0 : 1;
        return success;
    }

    /* Returns a string with spaces between underscores and characters to make length of string and
    spaces clearly visible */
    private String formatDisplayString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < displayString.length; i++) {
            sb.append(displayString[i]).append(' ');
        }
        return sb.toString();
    }

    /**
     * @return All letters guessed so far as strings.
     */
    public List<String> getGuessedLetters() {
        List<String> r = new ArrayList<>();
        for (char c : guessedLetters) r.add(""+c);
        return r;
    }

    /**
     * @return All words guessed so far.
     */
    public List<String> getGuessedWords() {
        return new ArrayList<>(guessedWords);
    }

    /**
     * @return A string representation of
     */
    public String displayGraphics() {
        StringBuilder sb = new StringBuilder();
        sb.append("`Status: ").append(formatDisplayString()).append("`\n");
        sb.append("Guessed letters: ").append(guessedLetters).append('\n');
        sb.append("Guessed words: ").append(guessedWords).append('\n');
        return sb.append(currentGraphicStrings.get(nrFails)).toString();
    }

    /**
     * @return True if the game is over and won, false if not. False means the game is either
     * in progress or over and lost.
     */
    public boolean isWon() {
        boolean won = new String(displayString).equals(new String(actualString));
        if(won && isLost()) {
            throw new IllegalStateException("Internal failure: game is both lost and won");
        }
        return won;
    }

    /**
     * @return True if the game is over and lost, false if the game is over and won or in progress.
     */
    public boolean isLost() {
        boolean lost = nrFails > getFailsAllowed();
        if (lost && isWon()) {
            throw new IllegalStateException("Internal failure: game is both lost and won");
        }
        return lost;
    }

    /**
     * @return The number of fails allowed.
     */
    public int getFailsAllowed() {
        return currentGraphicStrings.size() - 2; // avoid redundancy, may change later
    }

    /**
     * @return The number of available fails left
     */
    public int getFailsLeft() {
        return getFailsAllowed() - nrFails;
    }

    // Noone should be calling methods if game is over
    private void checkGameOver() {
        if (isWon() || isLost()) throw new UnsupportedOperationException("Game is over!");
    }

}
