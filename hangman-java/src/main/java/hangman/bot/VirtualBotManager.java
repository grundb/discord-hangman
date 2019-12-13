package hangman.bot;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The virtual bot manager maintains a bot for each guild of the actual bot and distributes any message events
 * to the correct virtual bot based on the guild from which the message was sent. If the message was a DM,
 * it is sent to the virtual bots of all guilds to which the sender belongs (all mutual guilds).
 */
public class VirtualBotManager extends ListenerAdapter {

    // Keeps track of what executor to assign to next virtual bot
    private int execIndex;

    private List<ExecutorService> singleThreadExecutors;

    // All virtual bots are kept in a hash table, ensures at most one bot per guild
    HashMap<Guild, VirtualBot> virtualBots;

    // Each virtual bot has a corresponding executor service
    HashMap<VirtualBot, ExecutorService> botExecutors;

    VirtualBotManager() {
        virtualBots = new HashMap<>();
        botExecutors = new HashMap<>();
        singleThreadExecutors = new ArrayList<>();
        int cores = Runtime.getRuntime().availableProcessors();
        for(int i = 0; i < cores - 1; i++) singleThreadExecutors.add(Executors.newSingleThreadExecutor());
        execIndex = 0;
    }

    /**
     * Receives an event from the event queue and sends it to the appropriate virtual bot.
     * @param event The event received from the api.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        ChannelType t = event.getChannelType();
        if (t == ChannelType.TEXT) {
            VirtualBot b = safeGetBot(event.getGuild());
            botExecutors.get(b).submit(() -> b.sendMessageEvent(event));
        } else if (t == ChannelType.PRIVATE) {
            for(Guild mutualGuild : event.getAuthor().getMutualGuilds()) {
                VirtualBot b = safeGetBot(mutualGuild);
                botExecutors.get(b).submit(() -> b.sendMessageEvent(event));
            }
        }
    }

    // Either sends the bot to the correct virtual bot, or creates a new one if there is no such bot
    private VirtualBot safeGetBot(Guild g) {
        VirtualBot b = virtualBots.get(g);
        if (b == null) {
            b = new VirtualBot(g);
            virtualBots.put(g, b);
            botExecutors.put(b, singleThreadExecutors.get(execIndex++));
        }
        return b;
    }
}