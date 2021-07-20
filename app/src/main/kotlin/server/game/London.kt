package server

import org.json.JSONArray

class Station(val number: Int) {
    val neighbors = mapOf<String, HashSet<Station>>(
        "taxi"  to HashSet(),
        "bus"   to HashSet(),
        "train" to HashSet(),
        "black" to HashSet(),
    )

    fun canReach(station: Station, ticket: String) = neighbors[ticket]?.contains(station) ?: false

    fun reachableStations(): Map<String, Set<Int>> = neighbors.entries.associate {
            (k, v) -> k to v.map { it.number }.toSet()
    }

//    fun rs(ticket: Tickets): List<Int> =
}

object London {
    val stations: List<Station> = (0..199).map { Station(it) }

    private val startPositions = listOf(12, 25, 28, 33, 49, 52, 90, 93, 102, 110, 115, 130, 139, 153, 172, 195, 196)

    fun getRandomStartPositions(): List<Int> = startPositions.shuffled()

    private fun connect(ticket: String, a: Int, b: Int) {
        stations[a].neighbors[ticket]?.add(stations[b])
        stations[b].neighbors[ticket]?.add(stations[a])
    }

    init {
        val json = IO.readJSON("src/main/resources/public/london.json")

        val taxi  : JSONArray = json.getJSONArray("taxi")
        val bus   : JSONArray = json.getJSONArray("bus")
        val train : JSONArray = json.getJSONArray("train")
        val black : JSONArray = json.getJSONArray("black")

        taxi.map  { (it as JSONArray) }.forEach {
            connect("taxi",  it.getInt(0), it.getInt(1))
            connect("black", it.getInt(0), it.getInt(1))
        }
        bus.map   { (it as JSONArray) }.forEach {
            connect("bus",   it.getInt(0), it.getInt(1))
            connect("black", it.getInt(0), it.getInt(1))
        }
        train.map { (it as JSONArray) }.forEach {
            connect("train", it.getInt(0), it.getInt(1))
            connect("black", it.getInt(0), it.getInt(1))
        }
        black.map { (it as JSONArray) }.forEach {
            connect("black", it.getInt(0), it.getInt(1))
        }
    }
}