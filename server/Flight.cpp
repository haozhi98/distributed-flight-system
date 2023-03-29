#include "Flight.h"

Flight::Flight(){}

Flight::Flight(int flightId_, string source_, string destination_, int seatsAvailable_, float airfare_){
	flightId = flightId_;
	source = source_;
	destination = destination_;
	seatsAvailable = seatsAvailable_;
	airfare = airfare_;
}

int Flight::getFlightId(){
	return flightId;
}

int Flight::getSeatsAvailable(){
    return seatsAvailable;
}

float Flight::getAirFare(){
	return airfare;
}

string Flight::getSource(){
	return source;
}

string Flight::getDestination(){
	return destination;
}

void Flight::addSeats(int seats){
    seatsAvailable += seats;
}

bool Flight::subtractSeats(int seats){
  if (seatsAvailable < seats) return false;
  seatsAvailable -= seats;
  return true;
}