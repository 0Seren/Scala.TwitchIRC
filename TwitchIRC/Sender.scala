package TwitchIRC

/**
  * @author 0Seren
  */
case class Sender(_name : String, _tags : Map[String, String] = Map()) {

  def name : String = _name

  def displayName : String = {
    _tags.getOrElse("display-name", _name)
  }

  def emotes : Option[Iterable[Emote]] = {
    Emote.StringAsEmotes(_tags.getOrElse("emotes", ""))
  }

  def color : Option[String] = {
    _tags.get("color")
  }

  def subscriber : Boolean = {
    val sub = _tags.getOrElse("subscriber", "")
    (sub == "1" || sub == "true")
  }

  def turbo : Boolean = {
    val turbo = _tags.getOrElse("turbo", "")
    (turbo == "1" || turbo == "true")
  }

  def user_type : String = {
    val level = _tags.getOrElse("user-type", "")
    if (level == "" || !Sender.levels.find(_ == level).isDefined) {
      Sender.viewer
    } else {
      level
    }
  }
}

object Sender {
  def viewer = "viewer"
  def mod = "mod"
  def global_mod = "global_mod"
  def admin = "admin"
  def staff = "staff"
  def owner = "owner"
  def levels : Iterable[String] = Set(viewer, mod, global_mod, admin, staff, owner)
  def Twitch = Sender("tmi.twitch.tv", Map("level" -> staff, "display-name" -> "TWITCH", "subscriber" -> "1", "turbo" -> "1"))
  def Error = Sender("error.twitch.tv", Map("level" -> staff, "display-name" -> "[ERROR]", "subscriber" -> "1", "turbo" -> "1"))
}
