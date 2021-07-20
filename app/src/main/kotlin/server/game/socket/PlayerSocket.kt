package server.game.socket

import io.javalin.websocket.*
import org.json.JSONObject
import server.game.Event
import server.game.UpdatePlayersEvent
import server.game.YouAreHostEvent
import java.util.concurrent.ConcurrentHashMap

abstract class PlayerSocket {
    companion object {
        const val playerParam = ":player-name"
    }

    val endpoint: (WsHandler) -> Unit = { ws ->
        ws.onConnect(::onConnect)
        ws.onClose(::onClose)
        ws.onMessage(::onMessage)
    }

    private val colors = mutableListOf("black", "blue", "green", "metal", "orange", "pink", "red")

    private val sessionPlayerMap = ConcurrentHashMap<WsContext, Player>()
    private val playerSessionMap = ConcurrentHashMap<Player, WsContext>()

    protected val WsContext.player: Player
        get() = sessionPlayerMap[this] ?: throw IllegalStateException("session is not mapped to a player")

    val players get() = playerSessionMap.keys.toList()

    protected fun Player.send(event: Event) {
        playerSessionMap[this]?.send(event)
    }

    protected fun broadcast(event: Event) {
        sessionPlayerMap.keys.forEach { it.send(event) }
    }

    var host: Player? = null
        set(value) {
            field = value
            field?.send(YouAreHostEvent)
        }

    private fun updatePlayers() {
        val event = UpdatePlayersEvent(players)
        onPlayersUpdate(event)
    }

    private fun onConnect(ctx: WsConnectContext) {
        val name: String = ctx.pathParam(playerParam)
        val player = Player(name, colors.removeLastOrNull() ?: "black")
        sessionPlayerMap[ctx] = player
        playerSessionMap[player] = ctx
        updatePlayers()
        if (host == null) host = ctx.player
    }

    private fun onClose(ctx: WsCloseContext) {
        val player = sessionPlayerMap.remove(ctx)
        if (player != null) {
            playerSessionMap.remove(player)
            colors.add(player.color)
            updatePlayers()
            if (player == host) {
                host = players.randomOrNull()
            }
        }
    }

    private fun onMessage(ctx: WsMessageContext) {
        val type = JSONObject(ctx.message()).getString("type")
        onEvent(type, ctx.player, ctx)
    }

    open fun onEvent(type: String, player: Player, ctx: WsMessageContext) {
        println("event: $type not implemented yet")
    }

    abstract fun onPlayersUpdate(event: UpdatePlayersEvent)
}