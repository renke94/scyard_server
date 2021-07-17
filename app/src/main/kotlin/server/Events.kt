package server

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

open class Event(val type: String)
class PlayersUpdatedEvent(val players: List<Player>) : Event("playersUpdate")
class HostStartedTheGameEvent : Event("hostStartedTheGame")
class YouAreHostEvent : Event("youAreHost")
class GameReadyStateChangedEvent(val isReady: Boolean) : Event("gameReadyStateChanged")
class MoveEvent(val move: Move) : Event("moveEvent")
class UpdatePlayerEvent(val playerInfo: PlayerInfo) : Event("updatePlayer")
class UpdateSelfEvent(val selfInfo: SelfInfo) : Event("updateSelf")
class GameStartedEvent(val gameInfo: GameInfo) : Event("gameStarted")
class MessageEvent(val message: String) : Event("messageEvent") {
    val timestamp: Date = Date()
    var sender: String = ""
}
