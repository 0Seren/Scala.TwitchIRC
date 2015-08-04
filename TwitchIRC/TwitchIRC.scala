package TwitchIRC
/**
  * @author 0Seren
  */
class TwitchIRC(private val _username : String, private val auth_token : String, membership : Boolean = true, commands : Boolean = true, tags : Boolean = true) {
	require(auth_token.startsWith("oauth:"), "Must use a valid oauth token.")
	private[this] val username = _username.toLowerCase
	private[this] val connection = new Connection("irc.twitch.tv", 6667)
	connect;

	private[this] def connect {
		sendMessage("PASS " + auth_token)
		sendMessage("NICK " + username)
		if (membership) sendMessage("CAP REQ :twitch.tv/membership")
		if (commands) sendMessage("CAP REQ :twitch.tv/commands")
		if (tags) sendMessage("CAP REQ :twitch.tv/tags")
	}

	def joinChannel(channel : String) {
		sendMessage("JOIN #" + channel.toLowerCase)
	}

	def joinChannels(channels : Iterable[String]) {
		channels.foreach(c => joinChannel(c))
	}

	def leaveChannel(channel : String) {
		sendMessage("PART #" + channel.toLowerCase)
	}

	def leaveChannels(channels : Iterable[String]) {
		channels.foreach(c => leaveChannel(c))
	}

	def sendMessage(_msg : String) {
		val msg = _msg + (if (!_msg.endsWith("\r\n")) "\r\n" else "")
		while (!connection.ready()) {}
		connection.sendMessage(msg)
	}

	def sendMessage(msg : String, channel : String) {
		sendMessage("PRIVMSG #" + channel + " :" + msg)
	}

	def getMessage() : Option[Message] = {
		connection.getNextMessage match {
			case None => None
			case Some(s) => {
				if (s.startsWith("PING ")) sendMessage("PONG " + s.drop(5))
				Some(Message(s))
			}
		}
	}

}