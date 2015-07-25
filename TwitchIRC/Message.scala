package TwitchIRC

/**
  * @author 0Seren
  */
class Message(val _msg : String, val _sender : Sender) {
	override def toString = "[" + _sender.name + "]" + _msg
	def message() = _msg
	def sender() = _sender
}
object Message {
	def apply(_msg : String, _sender : Sender) : Message = new Message(_msg, _sender)
	def apply(_msg : String) : Message = Parser.StringAsMessage(_msg)
	def unapply(message : Message) = Some((message._msg, message._sender))
}

case class ParseErrorMessage(override val _msg : String) extends Message(_msg, Sender.Error) {
	override def toString() = "\n\nERROR:" + _msg + "\n\n"
}
case class StatusMessage(override val _msg : String, _channel : String, _msg_data : Option[Map[String, String]] = None) extends Message(_msg, Sender.Twitch) {
	def channel() = _channel
	def msg_data() = _msg_data
	override def toString() = "[" + channel + " STATUS]" + _msg
}
case class ChannelMessage(override val _msg : String, override val _sender : Sender, _channel : String) extends Message("[" + _channel + "]" + _msg, _sender) {
	override def toString() = "[" + _channel + "]" + _sender.name + ": " + _msg
	def channel() = _channel
}
case class TwitchMessage(override val _msg : String) extends Message(_msg, Sender.Twitch)