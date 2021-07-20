package server.game.socket

import io.javalin.websocket.WsMessageContext
import server.game.MessageEvent


abstract class ChatSocket : PlayerSocket() {
    override fun onEvent(type: String, player: Player, ctx: WsMessageContext) {
        when (type) {
            "MessageEvent" -> onMessage(player, ctx.message<MessageEvent>().data.apply { sender = player.name })
            else -> super.onEvent(type, player, ctx)
        }
    }

    abstract fun onMessage(player: Player, message: Message)

    protected fun Player.sendMessage(message: Message) {
        this.send(MessageEvent(message))
    }

    protected fun broadcastMessage(message: Message) {
        broadcast(MessageEvent(message))
    }
}