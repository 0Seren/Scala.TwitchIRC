package TwitchIRC

/**
 * @author 0Seren
 */
class SocketListener(server: String, port: Int) {
  private val address: java.net.InetAddress = java.net.InetAddress.getByName(server)
  private val socket: java.net.Socket = new java.net.Socket(address, port)
  
  //Output Stream
  private val writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream))
  
  //Input Stream
  private val reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream))
  
  def getNextMessage : Option[String] = {
    if(reader.ready){
      Some(reader.readLine)
    } else{
      None
    }
  }
  
  def sendMessage(msg: String) {
    try{
      writer.write(msg)
      writer.flush
    } catch {
      case e: Throwable => println(e.getStackTrace)
    }
  }
}