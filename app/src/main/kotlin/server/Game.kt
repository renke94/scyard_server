package server

import java.util.concurrent.ConcurrentHashMap

object Game : GameSocket() {
    private var gameStarted: Boolean = false

    private var isReady: Boolean = false
    set(value) {
        if (value != field) {
            field = value
            println("game is ${if (field) "" else "not"} ready")
            onGameReadyStateChanged(GameReadyStateChangedEvent(field))
        }
    }

    override fun onPlayersUpdate(event: PlayersUpdatedEvent) {
        players.forEach { it.send(event) }
        isReady = players.size > 1
    }

    override fun onStartGame(player: Player, hostStartedTheGameEvent: HostStartedTheGameEvent) {
        if (isReady && !gameStarted && player == host) {
            println("${player.name} started the game")
            gameStarted = true
            initializeGame()
        }
    }

    override fun onGameReadyStateChanged(event: GameReadyStateChangedEvent) {
        players.forEach { it.send(event) }
    }

    override fun onPlayerMove(player: Player, moveEvent: MoveEvent) {
        println("${player.name} moves to station ${moveEvent.move.targetStation} by ${moveEvent.move.ticket}")
    }

    /**
     * Game data section - only access, when game is started!!
     */

    lateinit var misterX    : Player
    lateinit var detectives : List<Player>

    private val playerColors = ConcurrentHashMap<Player, Color>()
    var Player.color get() = playerColors[this] ?: throw IllegalStateException("player got no color")
    set(value) { playerColors[this] = value }

    private val playerTickets = ConcurrentHashMap<Player, Tickets>()
    val Player.tickets get() = playerTickets[this] ?: throw IllegalStateException("player got no tickets")

    private val playerStations = ConcurrentHashMap<Player, Station>()
    var Player.station get() = playerStations[this] ?: London.stations[0] //throw IllegalStateException("player not set on any station")
    set(value) { playerStations[this] = value }

    var misterXLastSeen: String = "Mister X has not been seen yet"

    private fun initializeGame() {
        players.shuffled().also {
            misterX    = it.first()
            detectives = it.drop(1)
        }

        detectives.forEach { playerTickets[it] = getDetectiveStartingTickets() }
        playerTickets[misterX] = getMisterXStartingTickets()

        val randomPositions = London.getRandomStartPositions(players.size)

        println(randomPositions)

        misterX.station = London.stations[randomPositions.first()]
        println("Mister X station: " + misterX.station.number)
        randomPositions.drop(1).forEachIndexed { i, number ->
            detectives[i].station = London.stations[number]
        }

        misterX.color = colors.last()
        detectives.forEachIndexed { i, d -> d.color = colors[i] }


        // notify players that the game has been initialized
        val event = GameStartedEvent(GameInfo(this))
        players.forEach { it.send(event) }
        misterX.send(UpdateMisterXEvent(PlayerInfo(misterX)))
    }
}