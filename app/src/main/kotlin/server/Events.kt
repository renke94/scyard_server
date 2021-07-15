package server

import com.fasterxml.jackson.annotation.JsonIgnore

open class Event(val type: String)
class PlayersUpdatedEvent(val players: List<Player>) : Event("playersUpdate")
class HostStartedTheGameEvent : Event("hostStartedTheGame")
class YouAreHostEvent : Event("youAreHost")
class GameReadyStateChangedEvent(val isReady: Boolean) : Event("gameReadyStateChanged")
class MoveEvent(val move: Move) : Event("moveEvent")
class UpdateMisterXEvent(val playerInfo: PlayerInfo) : Event("updateMisterX")
class GameStartedEvent(val gameInfo: GameInfo) : Event("gameStarted")
