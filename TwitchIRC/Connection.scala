package TwitchIRC
import scala.collection.mutable
/**
  * @author 0Seren
  */

/**
  * A Connection deals with connecting to a socket along with getting and sending messages to that socket.
  * val connection = new Connection(server, port, messageLimit)
  *
  * server       : String            -> The server to connect to
  * port         : Int               -> The port to connect to
  */
class Connection(server : String = "irc.chat.twitch.tv", port : Int = 6667) {
  private[this] val timeStamps : mutable.Queue[Long] = mutable.Queue()
  private[this] val address : java.net.InetAddress = java.net.InetAddress.getByName(server)
  private[this] val socket : java.net.Socket = new java.net.Socket(address, port)
  private[this] val writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream))
  private[this] val reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream))

  def getNextMessage() : Option[String] = {
    if (reader.ready) {
      Some(reader.readLine)
    } else {
      None
    }
  }

  def sendMessage(msg : String) : Boolean = {
    try {
      writer.write(msg)
      writer.flush
      timeStamps += System.currentTimeMillis
      updateTimeStamps
      true
    } catch {
      case t : Throwable => false
    }
  }

  def updateTimeStamps() {
    while (timeStamps.size > 0 && System.currentTimeMillis - timeStamps.head > 30000) {
      timeStamps.dequeue
    }
  }

  def ready() : Boolean = {
    updateTimeStamps
    timeStamps.size < 20
  }

  def close() : Boolean = {
    try {
      writer.close
      reader.close
      true
    } catch {
      case t : Throwable => {
        throw t
        false
      }
    }
  }
}
