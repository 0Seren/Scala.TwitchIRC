package TwitchIRC
import java.util.Date
/**
  * @author 0Seren
  */
object Parser {
	/* TODO: ADD TimeStamps
	 * private val DateFormatter : java.text.DateFormat = new java.text.SimpleDateFormat("MM/dd/YY HH:mm:ss:SSS z");
	 * private val date : java.util.Date = new java.util.Date(System.currentTimeMillis)
	 * private val dateFormatted = DateFormatter.format(date)
	 */

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
	private val viewerToChannelPattern = "^:([^!]+)!\\1@\\1\\.tmi\\.twitch\\.tv PRIVMSG #(\\S+) :(.+)".r
	private val pingPattern = "^PING (.+)".r
	private val twitchToIRCPatternStdPattern = "^:tmi\\.twitch\\.tv (.+)".r
	private val twitchToIRCPatternSelfPattern = "^:\\S+\\.tmi.twitch\\.tv (.+)".r
	private val twitchToIRCPatternJTVPattern = "^:jtv (.+)".r
	//membership
	private val joinPartPattern = "^:([^!]+)!\\1@\\1\\.tmi\\.twitch\\.tv (\\S+) #(\\S+)".r //Generic Status Message for Join, Part
	private val modJoinPartPattern = "^:jtv MODE #(\\S+) (\\p{Punct}o .+)".r
	//commands
	private val noticePattern = "^@msg-id=(\\S+) :tmi\\.twitch\\.tv NOTICE #(\\S+) :(.+)".r
	private val hostStartPattern = "^:tmi\\.twitch\\.tv HOSTTARGET #(\\S+) :(\\S+) .+".r
	private val hostStopPattern = "^:tmi\\.twitch\\.tv HOSTTARGET #(\\S+) :[-] .+".r
	private val userTimedOutPattern = "^:tmi\\.twitch\\.tv CLEARCHAT #(\\S+) :(.+)".r
	private val chatClearedPattern = "^:tmi\\.twitch\\.tv CLEARCHAT #(\\S+)".r
	private val userstatePattern = "^:tmi\\.twitch\\.tv USERSTATE #(\\S+)".r
	private val roomstatePattern = "^:tmi\\.twitch\\.tv ROOMSTATE #(\\S+)".r
	//tags
	private val viewerToChannelWithTagsPattern = "^@(\\S+) :([^!]+)!\\2@\\2\\.tmi\\.twitch\\.tv PRIVMSG #(\\S+) :(.+)".r
	private val userstateWithTagsPattern = "^@(\\S+) :tmi\\.twitch\\.tv USERSTATE #(\\S+)".r
	private val roomstateWithTagsPattern = "^@(\\S+) :tmi\\.twitch\\.tv ROOMSTATE #(\\S+)".r

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
	private def dealWithTags(tagsAsString : String, channel : String = "_", name : String = "") : Option[Map[String, String]] = {
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