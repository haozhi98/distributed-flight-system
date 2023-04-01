#include <fstream>
#include <sstream>
#include <iostream>
#include "FlightSystem.h"

FlightSystem::FlightSystem(){
    newFlightId = 1;
    flights.clear();
    bookings.clear();
    monitorQueue.clear();
    ReadFlights();
}

void FlightSystem::ReadFlights(){
    string fileName = "flights.csv";
    vector<vector<string>> data;
    vector<string> row;
    string line, word;

    fstream file (fileName, ios::in);
    if (file.is_open()) {
        while (getline(file, line)) {
            row.clear();
            stringstream str(line);

            while (getline(str, word, ',')) row.push_back(word);

            data.push_back(row);
        }
    } else {
        cout << "Error in opening flights data file" << endl;
        return;
    }

    for (auto& item: data) {
        if (item.size() == 5) {
            cout << addFlight(item[0], item[1], stoi(item[2]), stof(item[3]), stoi(item[4])) << " ";
        }
        for (auto& i: item) cout << i << " ";
        cout << endl;
    }
}

int FlightSystem::addFlight(string source, string destination, int seatsAvailable, float airfare, int flightTime){
    int flightId = newFlightId++;
     
	flights[flightId] = Flight(flightId, source, destination, seatsAvailable, airfare, flightTime);    
	return flightId;
}

vector<int> FlightSystem::queryByPlace(string source, string destination){
    vector<int> res;
    cout << source << endl;
    cout << destination << endl;
    cout << flights.size() << endl;
    for (auto& item: flights) {
        // filter by source and destination
        cout << item.second.getSource() << endl;
        cout << item.second.getDestination() << endl;
        
        if (item.second.getSource() == source && item.second.getDestination() == destination) {
            // add flightId to return array
            res.push_back(item.first);
        }
    }
    return res;
}

vector<pair<int,int>> FlightSystem::queryBookings(unsigned long userId){
    vector<pair<int,int>> res;
    
    if (bookings.find(userId) != bookings.end()) {
        for (auto& item: bookings[userId]) {
            res.push_back(make_pair(item.first, item.second));
        }
    }
    return res;
}

vector<Flight> FlightSystem::queryByFlightId(int flightId){
    vector<Flight> res;
    if (flights.find(flightId) != flights.end()) {
        cout << "Found flight" << endl;
        res.push_back(flights[flightId]);
    }
    return res;
}

vector<Flight> FlightSystem::queryAllFlights(){
    vector<Flight> res;
    for (auto& flight: flights) {
        res.push_back(flight.second);
    }
    return res;
}

pair<int,int> FlightSystem::createBooking(unsigned long userId, int flightId, int seats){
    // check if flight exists
    if (flights.find(flightId) == flights.end()) {
        return make_pair(-1,0);
    }
    int availableSeats = flights[flightId].getSeatsAvailable();

    // make the booking and update seats in flights if sufficient seats available
    if (flights[flightId].subtractSeats(seats)) {

        // user has existing booking
        if (bookings.find(userId) != bookings.end()) {
            // user already has booking for this flight
            if (bookings[userId].find(flightId) != bookings[userId].end()) {
                bookings[userId][flightId] += seats;
            }
            else {
                bookings[userId][flightId] = seats;
            }
        }
        // user has not made booking
        else {
            bookings[userId] = map<int, int> {{flightId, seats}};
        }

        callUpdateService(flightId);

        return make_pair(availableSeats,seats);
    }
    return make_pair(availableSeats,0);
}

int FlightSystem::cancelBooking(unsigned long userId, int flightId){
    if (bookings.find(userId) != bookings.end()) {
        if (bookings[userId].find(flightId) != bookings[userId].end()) {
            int seats = bookings[userId][flightId];
            flights[flightId].addSeats(seats);

            // remove booking from bookings
            bookings[userId].erase(flightId);

            callUpdateService(flightId);

            return seats;
        }
    }
    return 0;
}

bool FlightSystem::registerUpdateService(unsigned long userId, int flightId, int monitorInterval){
    if (flights.find(flightId) == flights.end()) return false;
    
    deque<pair<time_t,int>> monitoredFlights;
    if (monitorQueue.find(flightId) != monitorQueue.end()) {
        monitoredFlights = monitorQueue[flightId];
    }

    time_t curTime = time(nullptr);
    
    // remove monitored flights that have expired
    while (!monitoredFlights.empty() && monitoredFlights.front().first < curTime) {
        monitoredFlights.pop_front();
    }
    
    time_t expiryTime = curTime + monitorInterval;

    monitoredFlights.push_back(make_pair(expiryTime, userId));
    monitorQueue[userId] = monitoredFlights;

    return true;
}

vector<int> FlightSystem::callUpdateService(int flightId){
    vector<int> userIds;
    deque<pair<time_t,int>> monitoredFlights;

    if (monitorQueue.find(flightId) != monitorQueue.end()) {
        monitoredFlights = monitorQueue[flightId];
    }

    time_t curTime = time(nullptr);
    
    // remove monitored flights that have expired
    while (!monitoredFlights.empty() && monitoredFlights.front().first < curTime) {
        monitoredFlights.pop_front();
    }

    for (auto& item: monitoredFlights) {
        userIds.push_back(item.second);
    }

    return userIds;

}