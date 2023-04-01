
#ifndef FLIGHT
#define FLIGHT
#include <string>
using namespace std;
class Flight{
private:
	int flightId, seatsAvailable, flightTime;
	float airfare;
	string source, destination;
public:
	int getFlightId();
	int getSeatsAvailable();
	int getFlightTime();
	float getAirFare();
	string getSource();
	string getDestination();
	Flight();
	Flight(int flightId_, string source_, string destination_, int seatsAvailable_, float airfare_, int flightTime_);

    void addSeats(int seats);
    bool subtractSeats(int seats);

};
#endif
