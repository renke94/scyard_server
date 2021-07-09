package server

open class Event(val type: String)
class PlayersUpdatedEvent(val players: List<Player>) : Event("playersUpdate")
class GameStartedEvent : Event("gameStarted")
class YouAreHostEvent : Event("youAreHost")
class GameReadyStateChangedEvent(val isReady: Boolean) : Event("gameReadyStateChanged")