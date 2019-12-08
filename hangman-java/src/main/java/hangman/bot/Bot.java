package hangman.bot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static hangman.bot.Bot.state.IDLE;
import static hangman.bot.Bot.state.PLAYING;

public class Bot extends ListenerAdapter
{
    enum state {IDLE, SETUP, PLAYING}

    state currentState;
    Game currentGame;

    public Bot() {
        currentState = IDLE;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        System.out.println();
        // We don't want to respond to other bot accounts, including ourself
        if (event.getAuthor().isBot()) return;

        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        String content = message.getContentRaw();
        String[] words = content.split("\\s+");

        switch (currentState) {
            case IDLE:
                // Listen for start command
                if (words[0].equals("!start")) {
                    currentGame = new Game(words[1]);
                    currentState = PLAYING;
                    channel.sendMessage("Game started!").queue();
                    channel.sendMessage(currentGame.displayGraphics()).queue();
                }
                break;
            case SETUP:
                // Receive setup in private chat from user starting game, move to PLAYING
                break;
            case PLAYING:
                // Receive guesses, end game if game ends, move to IDLE
                if (words[0].equals("!guess")) {
                    if (words[1].length() == 1) {
                        currentGame.guessChar(words[1].charAt(0));
                    } else {
                        currentGame.guessWord(words[1]);
                    }
                    channel.sendMessage(currentGame.displayGraphics()).queue();
                }
                if (currentGame.isWon()) {
                    channel.sendMessage("Game won!").queue();
                    currentState = IDLE;
                }
                if (currentGame.isLost()) {
                    channel.sendMessage("Game lost!").queue();
                    currentState = IDLE;
                    currentGame = null;
                }
                break;
        }

        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)

    }
}