package server

import server.Game.color
import server.Game.station
import server.Game.tickets

class PlayerInfo(val player: Player) {
    val tickets = player.tickets
    val station = player.station.number
    val color   = player.color.color
}

class GameInfo(game: Game) {
    val detectives: List<PlayerInfo> = game.detectives.map { player -> PlayerInfo(player) }

    val misterX: Player = game.misterX
    val misterXLastSeen: String = game.misterXLastSeen
}