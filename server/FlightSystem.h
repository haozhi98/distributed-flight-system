#ifndef FLIGHTSYSTEM
#define FLIGHTSYSTEM

#include<map>
#include<utility>
#include<queue>
#include "Flight.h"
using namespace std;

class FlightSystem{
private:
	int newFlightId;
	// map of flightId: flights
	map<int,Flight> flights;

	// map of userId: map of flightId: seats
	map<int, map<int, int>> bookings;

	// map of flightId: queue of <time, userId>
	map<int, deque<pair<time_t, int>>> monitorQueue;
public:
	int addFlight(string source, string destination, int seatsAvailable, float airfare);
	vector<int> queryByPlace(string source, string destination);
	vector<pair<int,int>> queryBookings(int userId);
	bool checkFlightId(int flightId);
	vector<Flight> queryByFlightId(int flightId);
	bool createBooking(int userId, int flightId, int seats);
	bool cancelBooking(int userId, int flightId);
	bool registerUpdateService(int userId, int flightId, int monitorInterval);
    vector<int> callUpdateService(int flightId);
	FlightSystem();
    void ReadFlights();
};

#endif
