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
	map<unsigned long, map<int, int>> bookings;

	// map of flightId: queue of <time, userId>
	map<int, deque<pair<time_t, unsigned long>>> monitorQueue;
public:
	int addFlight(string source, string destination, int seatsAvailable, float airfare, int flightTime);
	vector<int> queryByPlace(string source, string destination);
	vector<pair<int,int>> queryBookings(unsigned long userId);
	bool checkFlightId(int flightId);
	vector<Flight> queryByFlightId(int flightId);
	vector<Flight> queryAllFlights();
	pair<int,int> createBooking(unsigned long userId, int flightId, int seats);
	int cancelBooking(unsigned long userId, int flightId);
	bool registerUpdateService(unsigned long userId, int flightId, int monitorInterval);
    pair<vector<unsigned long>, int>  callUpdateService(int flightId);
	FlightSystem();
    void ReadFlights();
};

#endif
