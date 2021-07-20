package server.game.socket

import server.Game
import server.London
import java.util.*

class Player(val name: String, val color: String) {
    val uuid: UUID = UUID.randomUUID()
}

class PlayerInfo(
    val uuid: UUID,
    val name: String,
    var color: String,
    var station: Int,
    val tickets: Tickets,
    val isMisterX: Boolean
) {
    val reachableStations: Map<String, Set<Int>> get() {
        val occupiedStations = Game.occupiedStations
        return London.stations[station].neighbors.entries
            .associate { (ticket, stations) ->
                ticket to stations.map { it.number }.toSet() - occupiedStations
            }
    }
}