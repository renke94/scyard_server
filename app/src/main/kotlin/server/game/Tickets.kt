package server.game.socket

class Tickets(
    var taxi  : Int = 10,
    var bus   : Int = 8,
    var train : Int = 4,
    var black : Int = 0,
) {
    companion object {
        fun getMisterXStartTickets(numberOfDetectives: Int): Tickets
            = Tickets(4, 3, 3, numberOfDetectives)
    }
}