package server.game.socket

import io.javalin.websocket.WsMessageContext
import server.game.GameReadyStateChangedEvent
import server.game.GameStartedEvent

abstract class LobbySocket : ChatSocket() {
    override fun onEvent(type: String, player: Player, ctx: WsMessageContext) {
        when (type) {
            "StartGameEvent" -> onStartGame(player)
            else -> super.onEvent(type, player, ctx)
        }
    }

    abstract fun onStartGame(player: Player)

    protected fun sendGameStartedEvent() {
        broadcast(GameStartedEvent)
    }

    protected fun sendGameReadyStateChangedEvent(state: Boolean) {
        broadcast(GameReadyStateChangedEvent(state))
    }
}