package server

import server.game.UpdatePlayersEvent
import server.game.socket.*
import java.util.concurrent.ConcurrentHashMap

object Game : GameSocket() {
    private val playerInfos = ConcurrentHashMap<Player, PlayerInfo>()

    private var Player.info: PlayerInfo
        get() = playerInfos[this]!!
        set(value) { playerInfos[this] = value }

    private var Player.station: Station
        get() = London.stations[playerInfos[this]!!.station]
        set(value) { playerInfos[this]!!.station = value.number }

    private val Player.tickets: Tickets get() = playerInfos[this]!!.tickets

    private var isReady: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                println("game is ${if (field) "" else "not"} ready")
                sendGameReadyStateChangedEvent(field)
            }
        }

    private var gameStarted: Boolean = false

    override fun onPlayersUpdate(event: UpdatePlayersEvent) {
        broadcast(event)
        isReady = event.data.size > 1
    }

    override fun onStartGame(player: Player) {
        if (isReady && !gameStarted && player == host) {
            println("${player.name} started the game")
            gameStarted = true
            initializeGame()
        }
    }


    private lateinit var misterX    : Player
    private lateinit var detectives : List<Player>

    val occupiedStations: Set<Int> get() = playerInfos.map { (_, info) -> info.station }.toSet() - misterX.station.number

    private val rounds: MutableList<Round> = mutableListOf()
    private val currentRound: Round get() = rounds.last()

    private val revealingStations: Set<Int> = setOf(3, 8, 13, 18, 24)

    private fun initializeGame() {
        // Choose Mister X randomly
        players.shuffled().also {
            misterX    = it.first()
            detectives = it.drop(1)
        }

        val randomPositions = London.getRandomStartPositions()

        misterX.info = PlayerInfo(
            misterX.uuid,
            misterX.name,
            "white",
            randomPositions.last(),
            Tickets.getMisterXStartTickets(detectives.size),
            true
        )

        detectives.forEachIndexed { idx, player ->
            player.info = PlayerInfo(
                player.uuid,
                player.name,
                player.color,
                randomPositions[idx],
                Tickets(),
                false
            )
        }

        rounds.add(Round())

        sendGameStartedEvent()
    }

    override fun onClientIsReady(player: Player) {
        player.updateSelfInfo(player.info)
        if (player != misterX) {
            player.updatePlayerInfo(detectives.associate { it.uuid.toString() to it.info }, "Game initialized")
        } else {
            player.updatePlayerInfo(playerInfos.values.associateBy { it.uuid.toString() }, "Game initialized")
        }
    }

    override fun onMessage(player: Player, message: Message) {
        broadcastMessage(message)
    }

    override fun onMove(player: Player, move: Move) {
        currentRound.handleMove(player, move)
        player.updateSelfInfo(player.info)

        if (player != misterX) {
            val message = "${player.name} moved to station ${move.targetStation} by ${move.ticket}"
            players.updatePlayerInfo(detectives.associate { it.uuid.toString() to it.info }, message)
        } else {
            val message = "Mister X moved by ${move.ticket}"
            player.updatePlayerInfo(playerInfos.values.associateBy { it.uuid.toString() }, message)
            sendMisterXMovedEvent(move.ticket)
        }
    }

    class Round {
        class IllegalMoveException(msg: String = "") : Exception(msg)

        private fun onRoundCompleted() {
            if (rounds.size >= 24) sendMisterXEscapedEvent()
            rounds.add(Round())
            sendNextRoundEvent(rounds.size)
            misterX.sendYourTurnEvent()
        }

        private val moves = ConcurrentHashMap(playerInfos.keys.associateWith { -1 })

        fun handleMove(player: Player, move: Move) {
            try {
                handle(player, move)
                broadcastMessage(Message("Player ${player.name} moves to station ${move.targetStation} by ${move.ticket}", "Server"))
            } catch (ex: IllegalMoveException) {
                println(ex.message)
                player.sendIllegalMoveEvent(move, ex.message!!)
            }
        }

        private fun handle(player: Player, move: Move) {
            if (!moves.containsKey(player)) throw IllegalMoveException("Illegal Player")
            if (moves[player] != -1) throw IllegalMoveException("Player already moved")

            val target: Station = London.stations[move.targetStation]
            val ticket: String  = move.ticket

            when (ticket) {
                "taxi"  -> if (player.tickets.taxi  <= 0) throw IllegalMoveException("Not enough tickets")
                "bus"   -> if (player.tickets.bus   <= 0) throw IllegalMoveException("Not enough tickets")
                "train" -> if (player.tickets.train <= 0) throw IllegalMoveException("Not enough tickets")
                "black" -> if (player.tickets.black <= 0) throw IllegalMoveException("Not enough tickets")
                else    -> throw IllegalMoveException("Invalid ticket")
            }

            if (!player.station.canReach(target, ticket)) throw IllegalMoveException("Station not reachable")

            if (player == misterX) { // Mister X is moving
                if (detectives.any { it.station == target }) throw IllegalMoveException("Station already occupied")

                player.station = target
                moves[player]  = target.number
                when (ticket) {
                    "taxi"  -> player.tickets.taxi--
                    "bus"   -> player.tickets.bus--
                    "train" -> player.tickets.train--
                    "black" -> player.tickets.black--
                }
                if (rounds.size in revealingStations) sendMisterXWasSeenEvent(misterX.info)
                detectives.sendYourTurnEvent()
            } else { // Detective is moving
                if (moves[misterX] == -1) throw IllegalMoveException("MisterX has to move first")
                if (detectives.any { it.station == target }) throw IllegalMoveException("Station already occupied")

                player.station = target
                moves[player]  = target.number
                when (ticket) {
                    "taxi"  -> { player.tickets.taxi--;   misterX.tickets.taxi++;  }
                    "bus"   -> { player.tickets.bus--;    misterX.tickets.bus++;   }
                    "train" -> { player.tickets.train--;  misterX.tickets.train++; }
                    "black" -> { player.tickets.black--;  misterX.tickets.black++; }
                }
                if (target == misterX.station) sendMisterXWasCaughtEvent(player.info)
            }

            if (isRoundCompleted()) onRoundCompleted()
        }

        private fun isRoundCompleted(): Boolean = !moves.values.any { it == -1 }
    }
}