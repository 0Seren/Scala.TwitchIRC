#Scala.TwitchIRC

This project is centered around providing an easy to use and flexible `Scala` library that interacts with and adheres to Twitch Chat and their [rules/rate limits](https://github.com/justintv/Twitch-API/blob/master/IRC.md). It does so in such a way that the user of the library shouldn't have to fear about breaking any rate limits or rules.

###Pros:
- Allow for unlimited message sending to single or multiple channels assuming the message are being sent from a Moderator level or higher source without fear of global ban.
- Will join channels as soon as is able per Twitch Chat rules.
- Parses incoming messages from channels into [Message Case Classes](https://github.com/0Seren/Scala.TwitchIRC/blob/master/TwitchIRC/Message.scala).

###Cons (TODO's):
- Does not currently have whisper support.
- Twitch might eat messages sent if you are not at Moderator level or higher if they are sent too fast.
- No support for Twitch APIs (i.e. get user lists).
- No support for event channels (i.e. Riot_Games).

##Example Use:
```scala
val username = /*username*/
val oauth = /*"oauth:"rest of oauth token*/
val channels_to_join = List(/*...*/)
val irc = new TwitchIRC(username, oauth)

irc.joinChannel(username)
irc.joinChannels(channels_to_join)

for(i <- 1 to 9001){
  irc.sendMessage(i.toString, channel)
}

while(/*!done*/){
  irc.getMessage match {
    case None => /*no message has been recieved*/
    case Some(cm : ChannelMessage) => /*message sent to a channel's chat*/
    case Some(sm: StatusMessage) => /*message sent pertaining to something like "sub mode on"*/
    case Some(tm : TwitchMessage) => /*misc messages sent from twitch.tv*/
    case Some(pem: ParseErrorMessage) => /*messages unable to be parsed (should be logged and reported here)*/
    case Some(m : Message) => /*this should never occur, and if it does, it should be logged and reported here)*/
    case _ => /*this should never occur, and if it does, it should be logged and reported here)*/
  }
}
```
