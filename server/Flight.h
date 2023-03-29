
#ifndef FLIGHT
#define FLIGHT
#include <string>
using namespace std;
class Flight{
private:
	int flightId, seatsAvailable;
	float airfare;
	string source, destination;
public:
	int getFlightId();
	int getSeatsAvailable();
	float getAirFare();
	string getSource();
	string getDestination();
	Flight();
	Flight(int flightId_, string source_, string destination_, int seatsAvailable_, float airfare_);

    void addSeats(int seats);
    bool subtractSeats(int seats);

};
#endif
