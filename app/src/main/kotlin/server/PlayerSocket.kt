package server

import io.javalin.websocket.*
import java.util.concurrent.ConcurrentHashMap

abstract class PlayerSocket {
    companion object {
        const val playerParam = ":player-name"
    }

    private val sessionPlayerMap = ConcurrentHashMap<WsContext, Player>()
    private val playerSessionMap = ConcurrentHashMap<Player, WsContext>()

    protected val WsContext.player: Player
        get() = sessionPlayerMap[this] ?: throw IllegalStateException("session is not mapped to a player")

    val players get() = playerSessionMap.keys.toList()

    protected fun Player.send(event: Event) {
        playerSessionMap[this]?.send(event)
    }

    val endpoint: (WsHandler) -> Unit = { ws ->
        ws.onConnect(::onConnect)
        ws.onClose(::onClose)
        ws.onMessage(::onMessage)
    }

    var host: Player? = null
        set(value) {
            field = value
            field?.send(YouAreHostEvent())
        }

    private fun updatePlayers() {
        val event = PlayersUpdatedEvent(players)
        onPlayersUpdate(event)
    }

    private fun onConnect(ctx: WsConnectContext) {
        val name: String = ctx.pathParam(playerParam)
        val player = Player(name)
        sessionPlayerMap[ctx] = player
        playerSessionMap[player] = ctx
        updatePlayers()
        if (host == null) host = ctx.player
    }

    private fun onClose(ctx: WsCloseContext) {
        val player = sessionPlayerMap.remove(ctx)
        playerSessionMap.remove(player)
        updatePlayers()
        if (ctx.player == host) {
            host = players.randomOrNull()
        }
    }

    protected abstract fun onMessage(ctx: WsMessageContext)

    abstract fun onPlayersUpdate(event: PlayersUpdatedEvent)
}