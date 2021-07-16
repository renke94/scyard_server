package server

import kotlin.test.Test
import kotlin.test.assertTrue

class StationTest {
    @Test fun stationCanReach() {
        assertTrue(London.stations[138].canReach(London.stations[131], Ticket.TAXI))
        assertTrue(London.stations[138].canReach(London.stations[131], Ticket.BUS))
    }
}