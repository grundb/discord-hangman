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
 * to the correct virtual bot based on the guild and/or user from which the message was sent. The
 * VirtualBotManager tries to use all logical threads available on the system. The virtual bot manager handles the
 * creation of all virtual bots, and the VirtualBot class should not be used if this class is used.
 */
public class VirtualBotManager extends ListenerAdapter {

    // All virtual bots are kept in a hash table, ensures at most one bot per guild
    private HashMap<Guild, VirtualBot> virtualBots;

    /* Users may choose to use multiple or a single core, whatever works best.
    All methods in this class have two behaviours; one for multithreading and one for
    single thread use. The state below is only used if multithreading is true.
    It would be a good idea to make this class abstract and implement two subclasses; one for
    single thread use and one for parallel use. */
    private boolean multithreading;

    // Each virtual bot has a corresponding executor service, if multithreaded
    private HashMap<VirtualBot, ExecutorService> botExecutors;

    // We maintain n-1 single thread executors, where n is the number of logical threads.
    private List<ExecutorService> singleThreadExecutors;

    // Keeps track of what executor to assign to next virtual bot
    private int execIndex;


    /**
     * Creates a new virtual bot manager. It is not recommended to ever instantiate more than one.
     * If the multithreading parameter is set to true, the VirtualBotManager will attempt to use one
     * core per logical thread and split the work between each core.
     * @param multithreading If true, multiple cores are used. Else, a single thread is used.
     */
    VirtualBotManager(boolean multithreading) {
        virtualBots = new HashMap<>();

        if(Runtime.getRuntime().availableProcessors() > 1) {
            this.multithreading = multithreading;
        } else {
            this.multithreading = false; // If there is only one logical thread, we shouldnt waste resources
        }

        if(multithreading) {
            botExecutors = new HashMap<>();
            singleThreadExecutors = new ArrayList<>();
            int cores = Runtime.getRuntime().availableProcessors();
            for (int i = 0; i < cores - 1; i++) singleThreadExecutors.add(Executors.newSingleThreadExecutor());
            execIndex = 0;
        }
    }

    /**
     * Receives an event from the event queue and sends it to the appropriate virtual bot. If the message
     * was a text message, it is sent to the corresponding virtual bot. If the message was a DM,
     * it is sent to the virtual bots of all guilds to which the sender belongs (all mutual guilds).
     * @param event The event received from the api.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        ChannelType t = event.getChannelType();
        if (t == ChannelType.TEXT) {
            VirtualBot b = safeGetBot(event.getGuild());
            if(multithreading) {
                botExecutors.get(b).submit(() -> b.handleMessageEvent(event));
            } else {
                b.handleMessageEvent(event);
            }
        } else if (t == ChannelType.PRIVATE) {
            for(Guild mutualGuild : event.getAuthor().getMutualGuilds()) {
                VirtualBot b = safeGetBot(mutualGuild);
                if(multithreading) {
                    botExecutors.get(b).submit(() -> b.handleMessageEvent(event));
                } else {
                    b.handleMessageEvent(event);
                }
            }
        }
    }

    // Either sends the bot to the correct virtual bot, or creates a new one if there is no such virtual bot
    // If multithreading is on and there is no bot, the bot is also assigned to an executor.
    private VirtualBot safeGetBot(Guild g) {
        VirtualBot b = virtualBots.get(g);
        if (b == null) {
            b = new VirtualBot(g);
            virtualBots.put(g, b);
            if(multithreading) {
                botExecutors.put(b, singleThreadExecutors.get((execIndex++) % singleThreadExecutors.size()));
            }
        }
        return b;
    }
}