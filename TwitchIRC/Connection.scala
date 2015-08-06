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
  * messageLimit : Tuple2[Int, Long] -> A message limit represented as (messages[Int] per[,] time in milliseconds[Long]). For no message limit make messages be negative.
  */
class Connection(server : String, port : Int, messageLimit : Tuple2[Int, Long] = (100, 30000)) {
  private[this] val timeStamps : mutable.Queue[Long] = mutable.Queue()
  private[this] val address : java.net.InetAddress = java.net.InetAddress.getByName(server)
  private[this] val socket : java.net.Socket = new java.net.Socket(address, port)

  //Output Stream
  private[this] val writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream))

  //Input Stream
  private[this] val reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream))

  def getNextMessage : Option[String] = {
    if (reader.ready) {
      Some(reader.readLine)
    } else {
      None
    }
  }

  def sendMessage(msg : String) {
    try {
      writer.write(msg)
      writer.flush
      timeStamps += System.currentTimeMillis
    } catch {
      case t : Throwable => println(t.getStackTrace)
    }
    updateTimeStamps
  }

  /**
    * Remove Time Stamps from the Queue if they're older than messageLimit._2
    */
  def updateTimeStamps {
    while (timeStamps.size > 0 && System.currentTimeMillis - timeStamps.head > messageLimit._2) {
      timeStamps.dequeue
    }
  }

  /**
    * Determines if the Connection is ready depending on the messageLimit
    */
  def ready() : Boolean = {
    updateTimeStamps
    if (messageLimit._1 >= 0) timeStamps.length < messageLimit._1
    else true
  }

  def close() : Boolean = {
    try {
      writer.close
      reader.close
      true
    } catch {
      case t : Throwable => {
        println(t.getStackTrace)
        false
      }
    }
  }
}