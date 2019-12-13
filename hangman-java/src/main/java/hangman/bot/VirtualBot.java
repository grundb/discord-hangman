package hangman.bot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static hangman.bot.VirtualBot.state.*;

public class VirtualBot extends ListenerAdapter {

    enum state {IDLE, SETUP, PLAYING}

    public static final String START_COMMAND = "!hangman-start";
    public static final String RESET_COMMAND = "!hangman-reset";
    private final Guild guild;

    state currentState;
    Game currentGame;
    User startingUser;
    TextChannel gameChannel;

    public VirtualBot(Guild g) {
        currentState = IDLE;
        guild = g;
    }

    /**
     * Sends a MessageReceivedEvent for this bot to handle. Individual bots do not implement the
     * ListenerAdapter interface as this would be inefficient.
     * @param event The MessageReceivedEvent we want this bot to handle.
     */
    public void sendMessageEvent(MessageReceivedEvent event) {
        if (shouldIgnore(event)) return;
        checkGuild(event);

        String[] words = event.getMessage().getContentRaw().toLowerCase().trim().split("\\s+");
        if (words.length > 0 && words[0].equals(RESET_COMMAND)) {
            event.getChannel().sendMessage("I've been reset!").queue();
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

    // We should ignore events from bots, and messages from channels other than
    private boolean shouldIgnore(MessageReceivedEvent event) {
        return ((event.getChannelType() != ChannelType.TEXT ) && (event.getChannelType() != ChannelType.PRIVATE)
                || event.getAuthor().isBot());
    }

    // This bot instance has its own guild, and should not handle events from other guilds
    private void checkGuild(MessageReceivedEvent event) {
        if(event.getChannelType() == ChannelType.TEXT && event.getGuild() != guild) {
            throw new IllegalArgumentException(
                    "Event from guild " + event.getGuild() + " passed to bot for guild " + guild);
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
            if (event.getAuthor() == startingUser) {
                gameChannel.sendMessage("No cheating, " + startingUser.getName() + "! :angry:").queue();
                return;
            }
            String message = event.getMessage().getContentRaw().trim().toUpperCase();
            if (message.length() == 1) {
                currentGame.guessChar(message.charAt(0));
                gameChannel.sendMessage("You guessed: " + message.charAt(0)).queue();
            } else {
                currentGame.guessWord(message);
                gameChannel.sendMessage("You guessed: " + message).queue();
            }
            gameChannel.sendMessage(currentGame.displayGameState()).queue();
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
                "Let's go! Send me a private message with the details, " + startingUser.getName() + ".").queue();
        messageUser(startingUser, privateMessage);
    }

    private void handleSetup(MessageReceivedEvent event) {
        if (!(event.getChannelType() == ChannelType.PRIVATE && event.getAuthor() == startingUser)) return;

        boolean success = true;

        String errorMessage = "Failed to read your message. ";
        String cleanedContent = event.getMessage().getContentRaw()
                .trim()
                .toUpperCase()
                .replaceAll("\\p{Punct}", "")
                .replaceAll("\\s+", " ");
        String[] cleanedContentWords = cleanedContent.split(" ");

        int fails = -1;
        String guessingSentence = "";

        try {
            if (cleanedContentWords.length < 1) {
                success = false;
                errorMessage += "Your message was empty. ";
            } else {
                fails = Integer.parseInt(cleanedContentWords[0]);
                if (!(1 <= fails && fails <= Game.DEFAULT_ALLOWED_FAILS)) {
                    success = false;
                    errorMessage += "Start with a number in the range 1 to " + Game.DEFAULT_ALLOWED_FAILS + ". ";
                }
            }
        } catch (NumberFormatException e) {
            success = false;
            errorMessage += "Start with a number in the range 1 to " + Game.DEFAULT_ALLOWED_FAILS + ". ";
        }

        if (cleanedContentWords.length < 2) {
            success = false;
            errorMessage += "Failed to identify a guessing sentence or word. ";
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
            gameChannel.sendMessage(currentGame.displayGameState()).queue();
        } else {
            messageUser(startingUser, errorMessage);
        }

    }

    // Send a message to a user without response handling
    private void messageUser(User u, String message) {
        u.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
    }
}