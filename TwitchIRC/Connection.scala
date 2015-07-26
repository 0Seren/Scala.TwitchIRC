package TwitchIRC
import scala.collection.mutable
/**
  * @author 0Seren
  */
class Connection(server : String, port : Int) {
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
			case e : Throwable => println(e.getStackTrace)
		}
	}

	/*
	 * Remove Time Stamps from the Queue if they're older than 30seconds
	 */
	def updateTimeStamps {
		while (timeStamps.size > 0 && System.currentTimeMillis - timeStamps.head > 30000) {
			timeStamps.dequeue
		}
	}

	/*
	 * Determines if the Socket is ready. Depending on if it's a mod or not. This is to avoid the global ban.
	 */
	def ready(isMod : Boolean = true) : Boolean = {
		timeStamps.length < (if (isMod) 100 else 20)
	}
}