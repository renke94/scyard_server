package server.game

import server.game.socket.Message
import server.game.socket.Move
import server.game.socket.Player
import server.game.socket.PlayerInfo
import java.util.*

open class Event(val type: String, var message: String = "")

object YouAreHostEvent
    : Event("YouAreHostEvent", "You're the host now")

class UpdatePlayersEvent(val data: List<Player>)
    : Event("UpdatePlayersEvent", "Players updated")

class GameReadyStateChangedEvent(val data: Boolean)
    : Event("GameReadyStateChangedEvent", if (data) "Game ready to start" else "Game not ready")

object GameStartedEvent
    : Event("GameStartedEvent", "Game has started")

object StartGameEvent
    : Event("StartGameEvent", "Host requested to start the game")

class UpdatePlayerInfoEvent(val data: Map<String, PlayerInfo>, message: String)
    : Event("UpdatePlayerInfoEvent", message)

class UpdateSelfInfoEvent(val data: PlayerInfo)
    : Event("UpdateSelfInfoEvent", "Your Information has been updated")

class MessageEvent(val data: Message)
    : Event("MessageEvent", data.text)

class MoveEvent(val data: Move)
    : Event("MoveEvent")

class IllegalMoveEvent(val data: Move, message: String)
    : Event("IllegalMoveEvent", message)

class NextRoundEvent(val data: Int)
    : Event("NextRoundEvent", "Next round #$data")

object YourTurnEvent
    : Event("YourTurnEvent", "It's your turn")

class MisterXMovedEvent(ticket: String)
    : Event("MisterXMovedEvent", "Mister X moved by $ticket")

class MisterXWasSeenEvent(val data: PlayerInfo)
    : Event("MisterXWasSeenEvent", "Mister X was seen on station ${data.station}")

class MisterXWasCaughtEvent(val data: PlayerInfo)
    : Event("MisterXWasCaughtEvent", "Mister X was caught by ${data.name} on station ${data.station}")

object MisterXEscapedEvent
    : Event("MisterXEscapedEvent", "Mister X escaped!")