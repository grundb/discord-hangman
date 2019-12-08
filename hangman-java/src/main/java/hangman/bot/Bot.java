package hangman.bot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static hangman.bot.Bot.state.IDLE;

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
        if (event.getAuthor().isBot()) return;

        switch (currentState) {
            case IDLE:
                // Listen for start command
                break;
            case SETUP:
                // Receive setup in private chat from user starting game, move to PLAYING
                break;
            case PLAYING:
                // Receive guesses, end game if game ends, move to IDLE
                break;
        }

        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw();

        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.equals("!ping"))
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
        }
    }
}