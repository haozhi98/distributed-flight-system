#ifndef HANDLERS
#define HANDLERS

#include <iostream>
#include "udp_server.h"
#include "utils.h"
#include "FlightSystem.h"
#include "constants.h"
#include <chrono>
#include <deque>
#include <string>
#include <map>
using namespace std;

class Handler{
 private:
    map<pair<unsigned long,int>,string> memo;
    map<pair<unsigned long, int>, string> responses;
    FlightSystem flightSystem;
    int response_id;
    int limit;
    unsigned seed;
    mt19937 generator;
    std::uniform_real_distribution<double> distribution;

    double failureRate;
 public:
    void ackHandler(udp_server &server, char *header, char *response, int responseSize, int responseID, int status, unsigned long cAddress);
    int getResponseID();

    void queryPlace(udp_server &server, char *p, int req_id, int status);
    void queryFlightId(udp_server &server, char *p, int req_id, int status);
    void queryUserId(udp_server &server, char *p, int req_id, int status);
    void queryAllFlights(udp_server &server, char *p, int req_id, int status);
    void bookFlight(udp_server &server, char *p, int req_id, int status);
    void cancelFlight(udp_server &server, char *p, int req_id, int status);
    void registerUpdateService(udp_server &server, char *p, int req_id, int status);
    void doUpdateService(udp_server &server, sockaddr_in cAddress, int flightId, int seats, int status);
    bool checkAndSendOldResponse(udp_server &server, unsigned long cAddress, int req_id);
    void sendReply(udp_server &server, char *header, char *response, int respondSize);
    Handler(int _limit, double _failureRate);
};
#endif
