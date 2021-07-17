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
        detectives.forEach { player ->
            broadcast(UpdatePlayerEvent(PlayerInfo(player)))
            player.send(UpdateSelfEvent(SelfInfo(player)))
        }

        updateMisterX()
    }

    private fun updateMisterX() {
        misterX.send(UpdatePlayerEvent(PlayerInfo(misterX)))
        misterX.send(UpdateSelfEvent(SelfInfo(misterX)))
    }

    private fun detectivesSend(event: Event) {
        detectives.forEach { it.send(event) }
    }

    private fun broadcast(event: Event) {
        players.forEach { it.send(event) }
    }

    private fun broadcastMessage(message: String) {
        val event = MessageEvent(message).apply {
            sender = "Server"
        }
        broadcast(event)
    }

    val occupiedStations: List<Station> get() = detectives.map { it.station }

    override fun onMessage(player: Player, messageEvent: MessageEvent) {
        if (messageEvent.message.startsWith("!")) {
            handleDevCommand(player, messageEvent)
            return
        }
        broadcast(messageEvent)
    }

    private val developers = HashSet<Player>()

    private fun handleDevCommand(player: Player, messageEvent: MessageEvent) {
        val help = "!dev <password> -- !move <station> -- !give <ticket> <amount>"

        fun sendMessage(message: String) {
            player.send(MessageEvent(message).apply { sender = "Server" })
        }

        fun updatePlayer() {
            player.send(UpdateSelfEvent(SelfInfo(player)))
            if (player != misterX) {
                broadcast(UpdatePlayerEvent(PlayerInfo(player)))
            } else {
                player.send(UpdatePlayerEvent(PlayerInfo(player)))
            }
        }

        val command: List<String> = messageEvent.message.split(Regex(" +"))

        if (command[0] == "!dev") {
            when {
                developers.contains(player) -> sendMessage("Developer Tools already enabled")
                command.size < 2            -> return
                command[1] == "kek"         -> developers.add(player).also { sendMessage("Developer Tools enabled") }
                else                        -> sendMessage("Developer Login failed")
            }
            return
        }

        if (!developers.contains(player)) return
//        if (command.size < 2) {
//            sendMessage("Too few arguments. Use !help")
//            return
//        }

        when(command[0]) {
            "!move" -> if(gameStarted) command[1].toIntOrNull()?.let { stationNr ->
                player.station = London.stations[stationNr]
                updatePlayer()
                sendMessage("(cheat) move to station ${stationNr}")
            } else sendMessage("Game not started")

            "!give" -> {
                if (!gameStarted) {
                    sendMessage("Game not started")
                    return
                }
                if (command.size < 3) sendMessage("Too few arguments. Try !give <ticket> <amount>")
                val ticket = command[1].toLowerCase()
                command[2].toIntOrNull()?.let { amount ->
                    val tickets = playerTickets[player]!!
                    when(ticket) {
                        "taxi"  -> tickets[Ticket.TAXI]  = tickets[Ticket.TAXI]!!  + amount
                        "bus"   -> tickets[Ticket.BUS]   = tickets[Ticket.BUS]!!   + amount
                        "train" -> tickets[Ticket.TRAIN] = tickets[Ticket.TRAIN]!! + amount
                        "black" -> tickets[Ticket.BLACK] = tickets[Ticket.BLACK]!! + amount
                    }
                    playerTickets[player] = tickets
                    println(player.tickets)
                    updatePlayer()
                } ?: sendMessage("amount must be an integer")
            }

            "!help" -> sendMessage(help)
            "!logout" -> developers.remove(player).also { sendMessage("Developer Tools disabled") }
            else    -> sendMessage("Undefined command")
        }
    }

    override fun onPlayerMove(player: Player, moveEvent: MoveEvent) {
        broadcastMessage("Player ${player.name} moves to station ${moveEvent.move.targetStation} by ${moveEvent.move.ticket}")
        val targetStation: Station = London.stations[moveEvent.move.targetStation]
        val ticket: Ticket = Ticket.valueOf(moveEvent.move.ticket.toUpperCase())

        println("log 1")

        if (player.station.canReach(targetStation, ticket)) {
            println("log 2")
            player.station = targetStation

            player.send(UpdateSelfEvent(SelfInfo(player)))
            if (player != misterX) {
                broadcast(UpdatePlayerEvent(PlayerInfo(player)))
            } else {
                player.send(UpdatePlayerEvent(PlayerInfo(player)))
            }
        }
    }
}