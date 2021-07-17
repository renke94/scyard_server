package server

import java.util.concurrent.ConcurrentHashMap

typealias Tickets = ConcurrentHashMap<Ticket, Int>

enum class Ticket { TAXI, BUS, TRAIN, BLACK }

fun getDetectiveStartingTickets() = Tickets().apply {
    put(Ticket.TAXI,  8)
    put(Ticket.BUS,   6)
    put(Ticket.TRAIN, 4)
    put(Ticket.BLACK, 0)
}

fun getMisterXStartingTickets() = Tickets().apply {
    put(Ticket.TAXI,  5)
    put(Ticket.BUS,   3)
    put(Ticket.TRAIN, 3)
    put(Ticket.BLACK, 2)
}