package server

import java.util.concurrent.ConcurrentHashMap

typealias Tickets = ConcurrentHashMap<Ticket, Int>

enum class Ticket { TAXI, BUS, TRAIN, BLACK }