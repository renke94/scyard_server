package server

import io.javalin.websocket.WsMessageContext
import org.json.JSONObject

abstract class GameSocket : PlayerSocket() {
    override fun onMessage(ctx: WsMessageContext) {
        val data = JSONObject(ctx.message())
        when (data["type"]) {
            "hostStartedTheGame" -> onStartGame(ctx.player, HostStartedTheGameEvent())
            "moveEvent" -> onPlayerMove(ctx.player, ctx.message<MoveEvent>())
            else -> println("event: ${data["type"]} not implemented yet")
        }
    }

    abstract fun onStartGame(player: Player, hostStartedTheGameEvent: HostStartedTheGameEvent)

    abstract fun onGameReadyStateChanged(event: GameReadyStateChangedEvent)

    abstract fun onPlayerMove(player: Player, moveEvent: MoveEvent)
}