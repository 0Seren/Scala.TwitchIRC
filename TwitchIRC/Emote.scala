package TwitchIRC

/**
  * @author 0Seren
  */
case class Emote(_id : Int, _indices : Array[(Int, Int)]) {}
object Emote {
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
}