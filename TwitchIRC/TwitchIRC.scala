package TwitchIRC
import scala.collection.mutable
/**
  * @author 0Seren
  */
class TwitchIRC(private val _username : String, private val auth_token : String) {
  require(auth_token.startsWith("oauth:"), "Must use a valid oauth token.")
  private[this] val username : String = _username.toLowerCase
  private[this] val joins : mutable.Queue[Long] = new mutable.Queue()
  private[this] val joinWaitlist : mutable.Queue[String] = new mutable.Queue()
  private[this] val reader : Connection = newFactoryConnection()
  private[this] val senders : mutable.ArrayBuffer[Connection] = mutable.ArrayBuffer(newFactoryConnection())
  private[this] val channels : mutable.Set[String] = mutable.Set()
  private[this] val mailbox : mutable.Queue[Message] = mutable.Queue()

  //This is a helper thread that performs all sorts of background operations to make things "easy"
  private[this] class helpThread() extends Thread {
    setName("TwitchIRC Helper")

    override def run() {
      while (!Thread.interrupted()) {
        //Clear Read Buffer From `senders`--------------------------------------------
        senders.foreach(emptyConnection(_))

        //Retrieve message from `reader` into queue. Will be useful for Whispers later
        val next = reader.getNextMessage()
        if (next.isDefined) {
          val message = next.get
          if (message.startsWith("PING ")) {
            sendMessage("PONG " + message.drop(5), reader)
          } else {
            mailbox += Message(message)
          }
        }

        //JOIN Channel----------------------------------------------------------------
        //Clear old joins
        if (joins.size > 0 && System.currentTimeMillis - joins.head > 15000) {
          joins.dequeue()
        }

        //if there's a channel to join and we're able to join it, do so.
        if (joins.size < 50 && joinWaitlist.size > 0) {
          val channel = joinWaitlist.dequeue()
          val lowerChannel = channel.toLowerCase
          sendMessage("JOIN #" + lowerChannel, reader)
          channels += lowerChannel
          joins += System.currentTimeMillis
        }
      }
    }
  }

  private[this] val helperThread : Thread = new Thread(new helpThread())
  helperThread.start()

  private[this] def sendMessage(_msg : String, _connection : Connection) {
    _connection.sendMessage(_msg + (if (!_msg.endsWith("\r\n")) "\r\n" else ""))
  }

  def sendMessage(_msg : String) {
    senders.find(_.ready()) match {
      case Some(c : Connection) => sendMessage(_msg, c)
      case _ => {
        val newconnection = newFactoryConnection()
        senders += newconnection
        sendMessage(_msg, newconnection)
      }
    }
  }

  def sendMessage(msg : String, channel : String) {
    sendMessage("PRIVMSG #" + channel + " :" + msg)
  }

  def getMessage() : Option[Message] = {
    if (mailbox.size > 0) {
      Some(mailbox.dequeue)
    } else {
      None
    }
  }

  def joinChannel(channel : String) {
    joinWaitlist += channel
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

  def joinChannels(_channels : Iterable[String]) {
    _channels.foreach(joinChannel(_))
  }

  def leaveChannels(_channels : Iterable[String]) {
    _channels.foreach(leaveChannel(_))
  }

  def close() : Boolean = {
    helperThread.interrupt()
    helperThread.join()
    leaveChannels(channels)
    senders.foreach(emptyConnection(_))
    emptyConnection(reader)
    senders.forall(_.close()) && reader.close()
  }

  def emptyConnection(c : Connection) {
    var next = c.getNextMessage
    while (next.isDefined) {
      val value = next.get
      if (value.startsWith("PING ")) sendMessage("PONG " + value.drop(5), c)
      next = c.getNextMessage
    }
  }

  def newFactoryConnection() : Connection = {
    val connection = new Connection()
    connection.sendMessage("PASS " + auth_token + "\r\n")
    connection.sendMessage("NICK " + username + "\r\n")
    connection.sendMessage("CAP REQ :twitch.tv/membership\r\n")
    connection.sendMessage("CAP REQ :twitch.tv/commands\r\n")
    connection.sendMessage("CAP REQ :twitch.tv/tags\r\n")

    connection
  }
}
