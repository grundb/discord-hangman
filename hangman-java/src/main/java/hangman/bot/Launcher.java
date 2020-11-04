package hangman.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * This class initializes a JDA API and creates a virtual bot manager to handle incoming messages.
 */
public class Launcher {
    private static String BOT_TOKEN;
    private static String BOT_TOKEN_PATH = "token.txt";

    // throwing these exceptions from main method is fine, no point in trying to start if we cant start
    public static void main(String[] args) throws LoginException, InterruptedException {
        try {
            InputStream is = Launcher.class.getClassLoader().getResourceAsStream(BOT_TOKEN_PATH);
            Scanner sc = new Scanner(is);
            BOT_TOKEN = sc.nextLine();
        } catch (NullPointerException e) {
            System.out.println("Failed to read bot token. Exiting...");
            System.exit(0);
        }
        JDA api = JDABuilder.createDefault(BOT_TOKEN)
                .addEventListeners(new VirtualBotManager(true))
                .build();
        api.awaitStatus(JDA.Status.CONNECTED); // Wait for login to complete
    }
}
