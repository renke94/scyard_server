package server

class Player(val name: String)

object Game : GameSocket() {
    private var isReady: Boolean = false
    set(value) {
        field = value
        onGameReadyStateChanged(GameReadyStateChangedEvent(value))
    }

    override fun onGameStarted(event: GameStartedEvent) {
        players.forEach { it.send(event) }
    }

    override fun onPlayersChanged(event: PlayersUpdatedEvent) {
        players.forEach { it.send(event) }
    }

    override fun onGameReadyStateChanged(event: GameReadyStateChangedEvent) {
        players.forEach { it.send(event) }
    }
}