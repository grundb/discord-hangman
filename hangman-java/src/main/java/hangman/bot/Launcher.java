package hangman.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Launcher {
    private static String BOT_TOKEN;
    private static String BOT_TOKEN_PATH = "src/data/token.txt";

    // throwing these exceptions from main method is fine, no point in trying to start if we cant start
    public static void main(String[] args) throws LoginException, InterruptedException {
        try {
            BOT_TOKEN = new Scanner(new File(BOT_TOKEN_PATH)).nextLine();
        } catch (FileNotFoundException e) {
            System.out.println("Failed to read bot token. Exiting...");
            System.exit(0);
        }
        JDA api = new JDABuilder(BOT_TOKEN).build();
        api.awaitStatus(JDA.Status.CONNECTED); // Wait for login to complete
        api.addEventListener(new VirtualBotManager());
    }


}
