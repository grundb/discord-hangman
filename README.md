# Discord hangman 
A discord bot that lets users play the hangman game using ASCII art.

Uses the [Java Discord API](https://github.com/DV8FromTheWorld/JDA) for all interacion with discord, and the code is based on their guide to getting started. 

## Setting up the bot
### Creating a Discord Application 
Go to the [Discord Developer Portal](https://discordapp.com/developers/applications). Under *Applications*, click *New Application*. Give the app an appropriate name, the specifics are not important. Next, go to the *Bot* tab and click *Add Bot*. Again, give the bot an appropriate name and maybe a profile picture. Next, navigate to the *OAuth2* tab. Here, in the *Scopes* section, check the bot box (and no other box). This should make a link appear in the bottom of the scopes view, as well as a *Bot Permissions* view at the bottom of the page. In the Bot Permissions view, we now need to set the appropriate permissions for the bot. This bot needs 3 kinds of permissions:

1. *View Channels* under "General Permissions"
2. *Send Messages* under "Text Permissions"
3. *Read Message History* under "Text Permissions"

Now, we are ready to add the bot to our server. The link in the scopes box should now contain something like `permissions=xxx`, where `xxx` is not `0`. Click this link, and follow the instructions to add the bot to your server.

### Setting up the IntelliJ project 
The easiest way to work with this repo is in IntelliJ IDEA. To set it all up, open IntelliJ and navigate to the *Import Project* view. Select the `hangman-java` folder in the repo and press OK. Next, select *Import project from external model* and pick *Gradle*. Press next. Check the option *Use auto-import*, and pick the option *Use gradle 'wrapper' task configuration*. Provided you have Java 12 installed, everything else can be left as is. Press *Finish* to complete the import.

If you try running the main method of `hangman.bot.Launcher`, you will notice that the method exits immediately and an error message is printed. This is because we have not yet added the bot token. Create a file called `token.txt` in the `hangman-java/src/main/resources` directory. In this file, we need to add the bot token. The `token.txt` file is added to the gitignore because it is secret and should not be commited to GitHub. Double check that the name and path of the file is correct. To find the bot token, go to the [Discord Developer Portal](https://discordapp.com/developers/applications) again, and copy the secret bot token for your bot which is found at the *Bot* tab. Paste it into the `hangman-java/src/main/resources/token.txt` file. You whould now be able to run `hangman.bot.Launcher.main` from IntelliJ to start the bot.

### Building a JAR file
When you want to deploy the bot to a server, such as a Raspberry Pi, the easiest way is using an executable jar. To build one, IntelliJ can be used. Top the right of the window, find the *Gradle* tab, then navigate to *Tasks* > *shadow* > *shadowJar* and double click it. If all works well, this should create the folder `hangman-java/build` which will contain the jar named something like `hangman-java-1.0-all.jar`. This can be executed by running `java -jar hangman-java-1.0-all.jar` from the directory in which it is located. 

### Setting up the bot to run automatically on Linux
In case you want the bot to run continously, you can set it up as a *systemd service*. First, add a bash script to the same folder as your jar file called `start.sh` and paste the following content:

```
#!/bin/sh
jar_name=$(ls <<PATH_TO_BOT_JAR>> | grep jar | head -n 1)
java -jar     <<PATH_TO_BOT_JAR>>/$jar_name
```

Where `<<PATH_TO_BOT_JAR>>` is replaced by the path to the directory containing your JAR file. Then make it executable by running `chmod -ux start.sh`.

Next, we will configure this script to run as a systemd service. To do this, add the following content to the file `/etc/systemd/system/discord-hangman.service` (which you will need to create with root privileges):

```
[Unit]
Description=Discord Hangman bot
After=network-online.target systemd-networkd-wait-online.service
Wants=network-online.target systemd-networkd-wait-online.service

[Service]
ExecStart=<<PATH_TO_BOT_JAR>>/start.sh
ExecStartPre=/bin/bash -c 'until host discordapp.com; do sleep 1; done'
Restart=always

[Install]
WantedBy=multi-user.target
```

Where `<<PATH_TO_BOT_JAR>>` is replaced by the path to the directory containing your JAR file. To load the changes, run `sudo systemctl daemon-reload` and then start the service with `sudo systemctl start discord-hangman`. This makes the systemd utility in Linux ensure that the bot is run on startup. If the bot crashes for whatever reason, it is immediately restarted.  Note that the bot will not start unless the host `discordapp.com` is accessible. This avoids a bug in which the bot is started before there is a functioning internet connection. To control the service manually, use `sudo systemctl restart discord-hangman`, `sudo systemctl start discord-hangman`, `sudo systemctl stop discord-hangman` and `sudo systemctl status discord-hangman`. To see that it works, try rebooting the system and watch your bot come online.

## Using the bot
Users can interact with the bot in 4 ways. They can start a game, set up a game, guess for a word or letter, and reset the bot.

### Starting a game
Users can start a game by sending the message `!hangman-start` in a channel of a discord server to which the hangman bot is invited. The bot can only have one active game per server at any one time.

### Configuring a game
Once a game has been started, the bot will ask the user who gave it the start command to supply a word to guess for, and a number of failed guesses allowed by other players. After successfully configuring a game, the game will start in the channel where the start command was issued.

### Guessing for a word
When a game is started in a channel, users can guess for words or letters by simply sending a message in the chat. The user who setup the game cannot make guesses.

### Resetting the bot
At any time, users can message the hangman bot either via DM or a text channel to reset it. To reset the bot, type `!hangman-reset`.
