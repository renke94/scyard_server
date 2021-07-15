package server

import io.javalin.websocket.*
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

abstract class GameSocket {
    private val sessionPlayerMap = ConcurrentHashMap<WsContext, Player>()
    private val playerSessionMap = ConcurrentHashMap<Player, WsContext>()

    private val WsContext.player: Player
        get() = sessionPlayerMap[this] ?: throw IllegalStateException("session is not mapped to a player")

    val endpoint: (WsHandler) -> Unit = { ws ->
        ws.onConnect(::onConnect)
        ws.onClose(::onClose)
        ws.onMessage(::onMessage)
    }

    val players get() = playerSessionMap.keys().toList()

    protected fun Player.send(event: Event) {
        playerSessionMap[this]?.send(event)
    }

    var host: WsContext? = null
        set(value) {
            field = value
            field?.send(YouAreHostEvent())
        }

    private fun onConnect(ctx: WsConnectContext) {
        val name: String = ctx.pathParam(playerParam)
        val player = Player(name)
        sessionPlayerMap[ctx] = player
        playerSessionMap[player] = ctx
        updatePlayers()
        if (host == null) host = ctx
    }

    private fun onClose(ctx: WsCloseContext) {
        val player = sessionPlayerMap.remove(ctx)
        playerSessionMap.remove(player)
        updatePlayers()
        if (ctx == host) {
            host = sessionPlayerMap.keys().toList().randomOrNull()
        }
    }

    private fun onMessage(ctx: WsMessageContext) {
        val data = JSONObject(ctx.message())
        when (data["type"]) {
            "startGame" -> onStartGame(GameStartedEvent())
            "moveEvent" -> onPlayerMove(ctx.player, ctx.message<MoveEvent>())
            else -> println("event: ${data["type"]} not implemented yet")
        }
    }

    private fun updatePlayers() {
        val event = PlayersUpdatedEvent(sessionPlayerMap.values.toList())
        onPlayersChanged(event)
    }

    abstract fun onStartGame(event: GameStartedEvent)

    abstract fun onPlayersChanged(event: PlayersUpdatedEvent)

    abstract fun onGameReadyStateChanged(event: GameReadyStateChangedEvent)

    abstract fun onPlayerMove(player: Player, moveEvent: MoveEvent)

    companion object {
        const val playerParam = ":player-name"
    }
}