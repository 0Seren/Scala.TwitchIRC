package TwitchIRC
import scala.collection.mutable
/**
  * @author 0Seren
  */
class TwitchIRC(private val _username : String, private val auth_token : String, membership : Boolean = true, commands : Boolean = true, tags : Boolean = true) {
  require(auth_token.startsWith("oauth:"), "Must use a valid oauth token.")
  private[this] val username : String = _username.toLowerCase
  private[this] val joins : mutable.Queue[Long] = new mutable.Queue()
  private[this] val reader : Connection = new Connection(username, auth_token)
  private[this] val senders : mutable.ArrayBuffer[Connection] = mutable.ArrayBuffer(new Connection(username, auth_token))
  private[this] val channels : mutable.Set[String] = mutable.Set()

  private[this] def sendMessage(_msg : String, _connection : Connection) {
    _connection.sendMessage(_msg + (if (!_msg.endsWith("\r\n")) "\r\n" else ""))
    emptySenders
  }

  def sendMessage(_msg : String) {
    senders.find(_.ready()) match {
      case Some(c : Connection) => sendMessage(_msg, c)
      case _ => {
        val newconnection = new Connection(username, auth_token)
        senders += newconnection
        sendMessage(_msg, newconnection)
      }
    }
  }

  def sendMessage(msg : String, channel : String) {
    sendMessage("PRIVMSG #" + channel + " :" + msg)
  }

  def getMessage() : Option[Message] = {
    emptySenders
    reader.getNextMessage match {
      case None => None
      case Some(s) => {
        if (s.startsWith("PING ")) sendMessage("PONG " + s.drop(5), reader)
        Some(Message(s))
      }
    }
  }

  def joinChannel(channel : String) : Boolean = {
    while (System.currentTimeMillis - joins.head > 15000) {
      joins.dequeue()
    }
    if (joins.size == 50) {
      false
    } else {
      val lowerChannel = channel.toLowerCase
      sendMessage("JOIN #" + lowerChannel, reader)
      channels += lowerChannel
      joins += System.currentTimeMillis
      true
    }
  }

  def leaveChannel(channel : String) {
    val lowerChannel = channel.toLowerCase
    if (channels.contains(lowerChannel)) {
      sendMessage("PART #" + lowerChannel, reader)
      channels -= lowerChannel
    }
  }

  def channels() : Iterable[String] = channels

  def inChannel(c : String) : Boolean = channels.contains(c.toLowerCase)

  def joinChannels(_channels : Iterable[String]) : Boolean = {
    _channels.forall(joinChannel(_))
  }

  def leaveChannels(_channels : Iterable[String]) {
    _channels.foreach(leaveChannel(_))
  }

  def close() : Boolean = {
    leaveChannels(channels)
    emptySenders
    emptyConnection(reader)
    senders.forall(_.close()) && reader.close()
  }

  def emptySenders {
    senders.foreach(emptyConnection(_))
  }

  private[this] def emptyConnection(c : Connection) {
    var next = c.getNextMessage
    while (next.isDefined) {
      val value = next.get
      if (value.startsWith("PING ")) sendMessage("PONG " + value.drop(5), c)
      next = c.getNextMessage
    }
  }
}