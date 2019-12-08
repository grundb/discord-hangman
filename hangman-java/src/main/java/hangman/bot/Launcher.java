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

public class Launcher {
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
        JDA api = new JDABuilder(BOT_TOKEN).addEventListeners(new Bot()).build();

    }

    /* Game g = new Game("flaggstång", 4);
        System.out.println(g.displayGraphics());
        System.out.println();
        g.guessChar('g');
        System.out.println(g.displayGraphics());
        System.out.println();
        g.guessChar('b');
        System.out.println(g.displayGraphics());
        System.out.println();
        g.guessChar('a');
        System.out.println(g.displayGraphics());
        System.out.println();
        g.guessChar('å');
        System.out.println(g.displayGraphics());
        System.out.println();
        g.guessChar('x');
        System.out.println(g.displayGraphics());
        System.out.println();
        g.guessChar('x');
        System.out.println(g.displayGraphics());
        System.out.println();
        g.guessChar('p');
        System.out.println(g.displayGraphics());
        System.out.println();
        g.guessChar('f');
        System.out.println(g.displayGraphics());
        System.out.println();
        g.guessWord("flaggstång");
        System.out.println(g.displayGraphics());
        System.out.println(); */
}
