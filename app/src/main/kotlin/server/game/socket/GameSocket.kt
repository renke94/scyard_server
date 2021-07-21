package server.game.socket

import io.javalin.websocket.WsMessageContext
import server.game.*

abstract class GameSocket : LobbySocket() {
    override fun onEvent(type: String, player: Player, ctx: WsMessageContext) {
        when (type) {
            "MoveEvent" -> onMove(player, ctx.message<MoveEvent>().data)
            "ClientReadyEvent" -> onClientIsReady(player)
            else -> super.onEvent(type, player, ctx)
        }
    }

    abstract fun onClientIsReady(player: Player)

    abstract fun onMove(player: Player, move: Move)

    protected fun Player.updateSelfInfo(playerInfo: PlayerInfo) {
        this.send(UpdateSelfInfoEvent(playerInfo))
    }

    protected fun Player.updatePlayerInfo(playerInfos: List<PlayerInfo>, message: String) {
        this.send(UpdatePlayerInfoEvent(playerInfos, message))
    }

    protected fun Collection<Player>.updatePlayerInfo(playerInfos: List<PlayerInfo>, message: String) {
        val event = UpdatePlayerInfoEvent(playerInfos, message)
        this.forEach { it.send(event) }
    }

    protected fun Player.sendIllegalMoveEvent(move: Move, message: String) {
        this.send(IllegalMoveEvent(move, message))
    }

    protected fun sendNextRoundEvent(roundNr: Int) {
        broadcast(NextRoundEvent(roundNr))
    }

    protected fun Player.sendYourTurnEvent() {
        this.send(YourTurnEvent)
    }

    protected fun Collection<Player>.sendYourTurnEvent() {
        this.forEach { it.send(YourTurnEvent) }
    }

    protected fun sendMisterXMovedEvent(ticket: String) {
        broadcast(MisterXMovedEvent(ticket))
    }

    protected fun sendMisterXWasSeenEvent(misterXInfo: PlayerInfo) {
        println("Mister X was seen on station ${misterXInfo.station}")
        broadcast(MisterXWasSeenEvent(misterXInfo))
    }

    protected fun sendMisterXWasCaughtEvent(detectiveInfo: PlayerInfo) {
        broadcast(MisterXWasCaughtEvent(detectiveInfo))
    }

    protected fun sendMisterXEscapedEvent() {
        broadcast(MisterXEscapedEvent)
    }
}