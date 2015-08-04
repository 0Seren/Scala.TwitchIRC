package TwitchIRC
/**
  * @author 0Seren
  */
private[TwitchIRC] object Parser {

	def StringAsMessage(s : String) : Message = {
		s match {
			case viewerToChannelWithTagsPattern(tags, name, channel, msg) => ChannelMessage(msg, Sender(name, dealWithTags(tags, channel, name).getOrElse(Map())), channel)
			case viewerToChannelPattern(name, channel, msg) => ChannelMessage(msg, Sender(name), channel)
			case joinPartPattern(name, joinOrPart, channel) if (joinOrPart == "JOIN" || joinOrPart == "PART") => StatusMessage(joinOrPart + " " + name, channel)
			case userTimedOutPattern(channel, name) => StatusMessage("TIMEOUT " + name, channel)
			case modJoinPartPattern(channel, msg) => StatusMessage("MODE " + msg, channel)
			case noticePattern(notice, channel, msg) => StatusMessage("NOTICE " + msg, channel, Some(Map("msg_id" -> notice)))
			case hostStopPattern(channel) => StatusMessage("HOSTING_STOP", channel)
			case hostStartPattern(channel, nowHosting) => StatusMessage("HOSTING_START #" + nowHosting, channel)
			case chatClearedPattern(channel) => StatusMessage("CLEARCHAT", channel)
			case userstateWithTagsPattern(tags, channel) => StatusMessage("USERSTATE", channel, dealWithTags(tags))
			case userstatePattern(channel) => StatusMessage("USERSTATE", channel)
			case roomstateWithTagsPattern(tags, channel) => StatusMessage("ROOMSTATE", channel, dealWithTags(tags))
			case roomstatePattern(channel) => StatusMessage("ROOMSTATE", channel)
			case pingPattern(msg) => TwitchMessage("PING " + msg)
			case twitchToIRCPatternStdPattern(msg) => TwitchMessage(msg)
			case twitchToIRCPatternSelfPattern(msg) => TwitchMessage(msg)
			case twitchToIRCPatternJTVPattern(msg) => TwitchMessage(msg)
			case msg => ParseErrorMessage(msg)
		}
	}

	//std
	private[this] val viewerToChannelPattern = "^:([^!]+)!\\1@\\1\\.tmi\\.twitch\\.tv PRIVMSG #(\\S+) :(.+)".r
	private[this] val pingPattern = "^PING (.+)".r
	private[this] val twitchToIRCPatternStdPattern = "^:tmi\\.twitch\\.tv (.+)".r
	private[this] val twitchToIRCPatternSelfPattern = "^:\\S+\\.tmi.twitch\\.tv (.+)".r
	private[this] val twitchToIRCPatternJTVPattern = "^:jtv (.+)".r
	//membership
	private[this] val joinPartPattern = "^:([^!]+)!\\1@\\1\\.tmi\\.twitch\\.tv (\\S+) #(\\S+)".r //Generic Status Message for Join, Part
	private[this] val modJoinPartPattern = "^:jtv MODE #(\\S+) (\\p{Punct}o .+)".r
	//commands
	private[this] val noticePattern = "^@msg-id=(\\S+) :tmi\\.twitch\\.tv NOTICE #(\\S+) :(.+)".r
	private[this] val hostStartPattern = "^:tmi\\.twitch\\.tv HOSTTARGET #(\\S+) :(\\S+) .+".r
	private[this] val hostStopPattern = "^:tmi\\.twitch\\.tv HOSTTARGET #(\\S+) :[-] .+".r
	private[this] val userTimedOutPattern = "^:tmi\\.twitch\\.tv CLEARCHAT #(\\S+) :(.+)".r
	private[this] val chatClearedPattern = "^:tmi\\.twitch\\.tv CLEARCHAT #(\\S+)".r
	private[this] val userstatePattern = "^:tmi\\.twitch\\.tv USERSTATE #(\\S+)".r
	private[this] val roomstatePattern = "^:tmi\\.twitch\\.tv ROOMSTATE #(\\S+)".r
	//tags
	private[this] val viewerToChannelWithTagsPattern = "^@(\\S+) :([^!]+)!\\2@\\2\\.tmi\\.twitch\\.tv PRIVMSG #(\\S+) :(.+)".r
	private[this] val userstateWithTagsPattern = "^@(\\S+) :tmi\\.twitch\\.tv USERSTATE #(\\S+)".r
	private[this] val roomstateWithTagsPattern = "^@(\\S+) :tmi\\.twitch\\.tv ROOMSTATE #(\\S+)".r

	/**
	  * Converts a String to Emote Objects
	  */
	def StringAsEmotes(s : String) : Option[Array[Emote]] = {
		try {
			Some(s.split("/").map { e =>
				val eSplit = e.split(":")
				Emote(eSplit(0).toInt, eSplit(1).split(",").map { s =>
					val hySplit = s.split("-")
					(hySplit(0).toInt -> hySplit(1).toInt)
				})
			})
		} catch {
			case e : Throwable => None
		}
	}

	/**
	  * converts tags to a map of (tag -> data)
	  */
	private[this] def dealWithTags(tagsAsString : String, channel : String = "_", name : String = "") : Option[Map[String, String]] = {
		try {
			val tags = if (tagsAsString.startsWith("@")) tagsAsString.drop(1) else tagsAsString
			Some(tags.split(";").map { t =>
				val parts = t.split("=")
				if (parts(0) == "user-type" && channel == name) "user-type" -> Sender.owner
				else if (parts.size < 2) "" -> ""
				else if (parts(1) == "") "" -> ""
				else parts(0) -> parts(1)
			}.toMap - "")
		} catch {
			case t : Throwable => None
		}
	}
}