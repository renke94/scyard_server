package server

object Game : GameSocket() {
    private var gameStarted: Boolean = false

    private var isReady: Boolean = false
    set(value) {
        if (value != field) {
            field = value
            onGameReadyStateChanged(GameReadyStateChangedEvent(field))
        }
    }

    override fun onPlayersUpdate(event: PlayersUpdatedEvent) {
        players.forEach { it.send(event) }
        isReady = players.size > 3
    }

    override fun onStartGame(player: Player, event: GameStartedEvent) {
        if (isReady && !gameStarted && player == host) {
            gameStarted = true
            players.forEach { it.send(event) }
        }
    }

    override fun onGameReadyStateChanged(event: GameReadyStateChangedEvent) {
        players.forEach { it.send(event) }
    }

    override fun onPlayerMove(player: Player, moveEvent: MoveEvent) {
        println("${player.name} moves to station ${moveEvent.move.targetStation} by ${moveEvent.move.ticket}")
    }
}