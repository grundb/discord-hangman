package hangman.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Bot {
    private static String BOT_TOKEN;
    private static String BOT_TOKEN_PATH = "src/data/token.txt";

    // throwing LoginException from main method is fine, no point in trying to start if we cant login
    public static void main(String[] args) throws LoginException {
        try {
            BOT_TOKEN = new Scanner(new File(BOT_TOKEN_PATH)).nextLine();
        } catch (FileNotFoundException e) {
            System.out.println("Failed to read bot token. Exiting...");
            System.exit(0);
        }
        JDA api = new JDABuilder(BOT_TOKEN).addEventListeners(new MyListener()).build();
    }

    public static class MyListener extends ListenerAdapter
    {
        @Override
        public void onMessageReceived(MessageReceivedEvent event)
        {
            if (event.getAuthor().isBot()) return;
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
}
