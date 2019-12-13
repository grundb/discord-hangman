# Discord hangman 
A discord bot that lets users play the hangman game using ASCII art. 

Uses the [Java Discord API](https://github.com/DV8FromTheWorld/JDA) for all interacion with discord, and the code is based on their guide to getting started. 

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