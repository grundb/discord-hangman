package hangman.bot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

import static hangman.bot.Bot.state.*;

public class Bot extends ListenerAdapter
{
    enum state {IDLE, SETUP, PLAYING}

    public static final String START_COMMAND = "!start";
    public static final String RESET_COMMAND = "!hangman-reset";

    state currentState;
    Game currentGame;
    User startingUser;
    TextChannel gameChannel;

    public Bot() {
        currentState = IDLE;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if ((event.getChannelType() != ChannelType.TEXT ) && (event.getChannelType() != ChannelType.PRIVATE)
                || event.getAuthor().isBot()) return;

        String[] words = event.getMessage().getContentRaw().toLowerCase().trim().split("\\s+");
        if (words.length > 0 && words[0].equals(RESET_COMMAND)) {
            reset();
            return;
        }

        switch (currentState) {
            case IDLE:
                handleStart(event);
                break;
            case SETUP:
                handleSetup(event);
                break;
            case PLAYING:
                handlePlaying(event);
                break;
        }

    }

    private void reset() {
        currentGame = null;
        startingUser = null;
        currentState = IDLE;
        gameChannel = null;
    }

    private void handlePlaying(MessageReceivedEvent event) {
        if(event.getChannel() == gameChannel) {
            String message = event.getMessage().getContentRaw().trim().toUpperCase();
            if (message.length() == 1) {
                currentGame.guessChar(message.charAt(0));
            } else {
                currentGame.guessWord(message);
            }
            gameChannel.sendMessage(currentGame.displayGraphics()).queue();
            if (currentGame.isWon() || currentGame.isLost()) {
                gameChannel.sendMessage(currentGame.isWon() ? "Game won!" : "Game lost!").queue();
                reset();
            }
        }
    }

    private void handleStart(MessageReceivedEvent event) {
        String firstWord = event.getMessage().getContentRaw().toLowerCase().trim().split("\\s+")[0];

        if (!firstWord.equals(START_COMMAND)) return;

        startingUser = event.getAuthor();
        gameChannel = event.getTextChannel();
        currentState = SETUP;

        String privateMessage = "Please respond with a single number in the range 1 to " +
                Game.DEFAULT_ALLOWED_FAILS + " of allowed fails followed by a space and a word or sentence to guess.";

        gameChannel.sendMessage(
                "Let's go! Send me a private message with the details, " + startingUser + ".").queue();
        messageUser(startingUser, privateMessage);
    }

    private void handleSetup(MessageReceivedEvent event) {
        System.out.println("Entered handleSetup");
        boolean success = true;

        String errorMessage = "Failed to read your message. ";
        String cleanedContent = event.getMessage().getContentRaw()
                .trim()
                .toUpperCase()
                .replaceAll("\\p{Punct}", "")
                .replaceAll("\\s+", " ");
        String[] cleanedContentWords = cleanedContent.split(" ");

        System.out.println("cleanedContentWords: " + Arrays.toString(cleanedContentWords));

        int fails = -1;
        String guessingSentence = "";

        try {
            if (cleanedContentWords.length < 1) {
                success = false;
                errorMessage += "Your message was empty.";
            } else {
                fails = Integer.parseInt(cleanedContentWords[0]);
            }
        } catch (NumberFormatException e) {
            success = false;
            errorMessage += "Start with a number in the range 1 to " + Game.DEFAULT_ALLOWED_FAILS + ".";
        }

        if (cleanedContentWords.length < 2) {
            success = false;
            errorMessage += "Failed to identify a guessing sentence or word.";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < cleanedContentWords.length; i++) {
                sb.append(cleanedContentWords[i]).append(i == cleanedContentWords.length - 1 ? "" : " ");
            }
            guessingSentence = sb.toString();
        }

        // If setup successful, start game. Else,
        if(success) {
            currentGame = new Game(guessingSentence, fails);
            currentState = PLAYING;
            gameChannel.sendMessage("Setup complete, game starting...").queue();
            gameChannel.sendMessage(currentGame.displayGraphics()).queue();
        } else {
            messageUser(startingUser, errorMessage);
        }

    }

    // Send a message to a user without response handling
    private void messageUser(User u, String message) {
        u.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
    }
}