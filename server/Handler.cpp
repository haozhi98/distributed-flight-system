#include "Handler.h"
#include "Flight.h"

Handler::Handler(int _limit, double _failureRate){
    flightSystem = FlightSystem();
    response_id = 0;
    limit = _limit;

    seed = chrono::system_clock::now().time_since_epoch().count();
    generator = mt19937(seed);
    distribution = uniform_real_distribution<double> (0.0,1.0);
    failureRate = _failureRate;
}

// Increments response ID
int Handler::getResponseID(){
    return response_id++;
}

// Sends response under certain failure rate
void Handler::sendReply(udp_server &server, char *header, char *response, int responseSize){   
    if(distribution(generator) > failureRate){
        server.send(header,HEADER_SIZE);
        server.send(response, responseSize);
        cout << ">>>>>>>>>>Response id " << utils::unmarshalInt(response) << " of length " << responseSize << " is sent\n";
    }
    else cout << "Failure\n";
}

// Repeatedly listens for incoming ack and resends response each timeout
void Handler::ackHandler(udp_server &server, char *header, char *response, int responseSize, int responseID, int status, unsigned long cAddress){
    char ackHeader[HEADER_SIZE];

    for(int i=1; i!=limit+1; i++){

        cout << "WAITING FOR ACK HEADER (ack handler 1)\n";
        int n = server.receive_time(ackHeader,HEADER_SIZE,RECEIVE_TIMEOUT);

        if(n <= 0){
            cout << "Timeout!, resending response ... \n";
            sendReply(server,header,response,responseSize);
            continue;
        }

        int ackSize = utils::unmarshalInt(ackHeader);
        
        char* ack = new char[ackSize];

        cout << "WAITING FOR ACK\n";
        n = server.receive_time(ack,ackSize,RECEIVE_TIMEOUT);

        if(n <= 0){
            cout << "Timeout!, resending response ... \n";
            sendReply(server,header,response,responseSize);
            continue;
        }

        char *x = ack;
        int ack_id = utils::unmarshalInt(x);
        x += ID_SIZE;

        if(ack_id == responseID) break;
        else if(status == 2 && checkAndSendOldResponse(server,cAddress,ack_id)){
            cout << "Old request ID received instead!\n";
            cout << "Old response sent..!\n";
            cout << "Waiting for ack again..\n";
            continue;
        }
        else{
            cout << "ID mismatch!\nACK ID: " << ack_id << "\nResponse ID: " << responseID << "\n";
            sendReply(server,header,response,responseSize);
        }
    }
}

// Checks whether message is a duplicate based on req_id and if so, sends the old response

bool Handler::checkAndSendOldResponse(udp_server &server, unsigned long cAddress, int req_id){
    if(!memo.count({cAddress,req_id})) return false;
    char header[HEADER_SIZE];

    string str = memo[{cAddress,req_id}];
    
    char *response = new char[str.size()];
    for(int i=0;i<(int)str.size();i++)
        response[i] = str[i];
    
    int size = (int)str.size();
        
    utils::marshalInt(size,header);
    sendReply(server,header,response,size);
    cout << "Old response of size " << size << " is sent back\n";
    cout << "??????Status byte is: " << (int)*(response+ID_SIZE) << '\n';
    cout << "??????Status byte iz: " << (int)str[ID_SIZE] << '\n';
    return true;
}

void Handler::queryPlace(udp_server &server, char *p, int req_id, int status){
    cout << "################################ Query by Place #####################################\n";
    unsigned long clientAddress = server.getClientAddress().sin_addr.s_addr;
    if(status == 2 && checkAndSendOldResponse(server,clientAddress,req_id)) return;

    string source, destination;
    int length = utils::unmarshalInt(p);
    p += INT_SIZE;
    source = utils::unmarshalString(p, length);
    p += length;
    length = utils::unmarshalInt(p);
    p += INT_SIZE;
    destination = utils::unmarshalString(p, length);
    p += length;

    vector<int> flightIds = flightSystem.queryByPlace(source, destination);
    cout << "Found " << flightIds.size() << " flight(s) with given source and destination places\n";
    // header indicates size of response
    char header[HEADER_SIZE];
    int responseSize = ID_SIZE+STATUS_SIZE+INT_SIZE*(1+flightIds.size());
    utils::marshalInt(responseSize,header);
    // response
    char response[responseSize];
    char *cur = response;
    // responseId
    int responseID = getResponseID();
    utils::marshalInt(responseID,cur);
    cur += ID_SIZE;
    // status
    utils::marshalString(ACK,cur);
    cur += STATUS_SIZE;
    // number of flights
    utils::marshalInt(flightIds.size(),cur);
    cur += INT_SIZE;
    // array of flight id
    for (auto& flightId: flightIds) {
        cout << "flight ID " << flightId << endl;
        utils::marshalInt(flightId,cur);
        cur += INT_SIZE;
    }

    cout << "!!!!!!!Saving response with status byte " << (int)*(response+ID_SIZE) << " into memory!!!!!!!!!\n";
    if(status == 2) responses[{clientAddress,req_id}] = string(response,responseSize);
    sendReply(server,header,response,responseSize);
    if(status == 2) ackHandler(server, header, response, responseSize, responseID, status, clientAddress);
    cout << "Found " + to_string(flightIds.size()) + " flights with source place from: " + source + ", to destination place:  " + destination << std::endl;
}

void Handler::queryFlightId(udp_server &server, char *p, int req_id, int status){
    cout << "################################ Query by Flight ID #####################################\n";
    unsigned long clientAddress = server.getClientAddress().sin_addr.s_addr;
    if(status == 2 && checkAndSendOldResponse(server,clientAddress,req_id)) return;

    int flightId;
    int length = utils::unmarshalInt(p);
    p += INT_SIZE;
    flightId = utils::unmarshalInt(p);
    p += length;

    vector<Flight> flight = flightSystem.queryByFlightId(flightId);
    // header indicates size of response
    char header[HEADER_SIZE];
    int responseSize = ID_SIZE+STATUS_SIZE+INT_SIZE*(1+flight.size()*3)+FLOAT_SIZE;
    utils::marshalInt(responseSize,header);
    // response
    char response[responseSize];
    char *cur = response;
    // responseId
    int responseID = getResponseID();
    utils::marshalInt(responseID,cur);
    cur += ID_SIZE;
    // status
    utils::marshalString(ACK,cur);
    cur += STATUS_SIZE;
    // is flight found
    if (flight.size() > 0) {
        cout << "Found flight: " << flight[0].getFlightId() << endl;
        utils::marshalInt(1,cur);
        cur += INT_SIZE;
        // flight ID
        utils::marshalInt(flight[0].getFlightId(), cur);
        cur += INT_SIZE;
        // flight time
        utils::marshalInt(flight[0].getFlightTime(),cur);
        cur += INT_SIZE;
        // airfare
        utils::marshalFloat(flight[0].getAirFare(),cur);
        cur += FLOAT_SIZE;
        // seat availability
        utils::marshalInt(flight[0].getSeatsAvailable(),cur);
    } else {
        utils::marshalInt(0,cur);
        cur += INT_SIZE;
    }
    
    cout << "##########Saving response with status byte " << (int)*(response+ID_SIZE) << " into memory##########\n";
    if(status == 2) responses[{clientAddress,req_id}] = string(response,responseSize);
    sendReply(server,header,response,responseSize);
    if(status == 2) ackHandler(server, header, response, responseSize, responseID, status, clientAddress);
    if (flight.size() > 0) cout << "Found flight: " + to_string(flightId) + "\nFlight Time: " + to_string(flight[0].getFlightTime()) + "\nAirfare:  " + to_string(flight[0].getAirFare()) + "\n Seats Available: " + to_string(flight[0].getSeatsAvailable()) << endl;
    else cout << "Not flight found with flight ID: " + to_string(flightId) << endl;
}

void Handler::queryUserId(udp_server &server, char *p, int req_id, int status){
    cout << "################################ Query by User ID #####################################\n";
    unsigned long clientAddress = server.getClientAddress().sin_addr.s_addr;
    if(status == 2 && checkAndSendOldResponse(server,clientAddress,req_id)) return;

    vector<pair<int,int>> bookings = flightSystem.queryBookings(clientAddress);
    // header indicates size of response
    char header[HEADER_SIZE];
    int responseSize = ID_SIZE+STATUS_SIZE+INT_SIZE*(1+bookings.size()*2);
    utils::marshalInt(responseSize,header);
    // response
    char response[responseSize];
    char *cur = response;
    // responseId
    int responseID = getResponseID();
    utils::marshalInt(responseID,cur);
    cur += ID_SIZE;
    // status
    utils::marshalString(ACK,cur);
    cur += STATUS_SIZE;
    // is bookings found
    if (bookings.size() > 0) {
        cout << "Found " << bookings.size() << " bookings" << endl;
        // booking count
        utils::marshalInt(bookings.size(),cur);
        cur += INT_SIZE;

        // array of flight id
        for (auto& booking: bookings) {
            cout << "Flight ID: " << booking.first  << ", Tickets: " << booking.second << endl;
            // flight id
            utils::marshalInt(booking.first,cur);
            cur += INT_SIZE;
            // tickets
            utils::marshalInt(booking.second,cur);
            cur += INT_SIZE;
        }
    } else {
        utils::marshalInt(0,cur);
        cur += INT_SIZE;
    }
    
    cout << "##########Saving response with status byte " << (int)*(response+ID_SIZE) << " into memory##########\n";
    if(status == 2) responses[{clientAddress,req_id}] = string(response,responseSize);
    sendReply(server,header,response,responseSize);
    if(status == 2) ackHandler(server, header, response, responseSize, responseID, status, clientAddress);
    cout << bookings.size() + " booking(s) found" << endl;
}

void Handler::queryAllFlights(udp_server &server, char *p, int req_id, int status){
    cout << "################################ Query All Flights #####################################\n";
    unsigned long clientAddress = server.getClientAddress().sin_addr.s_addr;
    if(status == 2 && checkAndSendOldResponse(server,clientAddress,req_id)) return;

    vector<Flight> flights = flightSystem.queryAllFlights();

    // header indicates size of response
    char header[HEADER_SIZE];
    int responseSize = ID_SIZE+STATUS_SIZE+INT_SIZE+flights.size()*(5*INT_SIZE+FLOAT_SIZE);

    if (flights.size()>1) {
        for (auto& flight: flights) {
            responseSize += flight.getSource().size();
            responseSize += flight.getDestination().size();
        }
    }

    utils::marshalInt(responseSize,header);
    // response
    char response[responseSize];
    char *cur = response;
    // responseId
    int responseID = getResponseID();
    utils::marshalInt(responseID,cur);
    cur += ID_SIZE;
    // status
    utils::marshalString(ACK,cur);
    cur += STATUS_SIZE;
    // flight count
    utils::marshalInt(flights.size(),cur);
    cur += INT_SIZE;

    if (flights.size() > 1) {
        int str_size;
        string source, destination;

        for (auto& flight: flights){
            // flight ID
            utils::marshalInt(flight.getFlightId(), cur);
            cur += INT_SIZE;
            // source
            source = flight.getSource();
            str_size = source.size();
            utils::marshalInt(str_size, cur);
            cur += INT_SIZE;
            utils::marshalString(source, cur);
            cur += str_size;
            // destination 
            destination = flight.getDestination();
            str_size = destination.size();
            utils::marshalInt(str_size, cur);
            cur += INT_SIZE;
            utils::marshalString(destination, cur);
            cur += str_size;
            // flight time
            utils::marshalInt(flight.getFlightTime(), cur);
            cur += INT_SIZE;
            // airfare
            utils::marshalFloat(flight.getAirFare(), cur);
            cur += FLOAT_SIZE;
            // seats available
            utils::marshalInt(flight.getSeatsAvailable(), cur);
            cur += INT_SIZE;
        }
    }
    
    cout << "##########Saving response with status byte " << (int)*(response+ID_SIZE) << " into memory##########\n";
    if(status == 2) responses[{clientAddress,req_id}] = string(response,responseSize);
    sendReply(server,header,response,responseSize);
    if(status == 2) ackHandler(server, header, response, responseSize, responseID, status, clientAddress);
    cout << flights.size() + " flight(s) details sent" << endl;
}

void Handler::bookFlight(udp_server &server, char *p, int req_id, int status){
    cout << "################################ Book Flight #####################################\n";
    unsigned long clientAddress = server.getClientAddress().sin_addr.s_addr;
    if(status == 2 && checkAndSendOldResponse(server,clientAddress,req_id)) return;
    
    int flightId, seats;
    int length = utils::unmarshalInt(p);
    p += INT_SIZE;
    flightId = utils::unmarshalInt(p);
    p += length;
    length = utils::unmarshalInt(p);
    p += INT_SIZE;
    seats = utils::unmarshalInt(p);
    pair<int,int> bookingOutcome = flightSystem.createBooking(clientAddress, flightId, seats);

    // header indicates size of response
    char header[HEADER_SIZE];
    int responseSize = ID_SIZE+STATUS_SIZE+INT_SIZE*2;
    utils::marshalInt(responseSize,header);
    // response
    char response[responseSize];
    char *cur = response;
    // responseId
    int responseID = getResponseID();
    utils::marshalInt(responseID,cur);
    cur += ID_SIZE;
    // status
    utils::marshalString(ACK,cur);
    cur += STATUS_SIZE;
    // seats found
    utils::marshalInt(bookingOutcome.first,cur);
    cur += INT_SIZE;
    // seats booked
    utils::marshalInt(bookingOutcome.second,cur);
    
    cout << "##########Saving response with status byte " << (int)*(response+ID_SIZE) << " into memory##########\n";
    if(status == 2) responses[{clientAddress,req_id}] = string(response,responseSize);
    sendReply(server,header,response,responseSize);
    if(status == 2) ackHandler(server, header, response, responseSize, responseID, status, clientAddress);
    if (bookingOutcome.first == -1) cout << "Flight not found" << endl;
    else if (bookingOutcome.second == 0) cout << bookingOutcome.first + " seats available, but required " + seats << endl;
    else cout << bookingOutcome.second + " seats booked for flight " + flightId  << endl;
    
    pair<vector<sockaddr_in>, int> res = flightSystem.callUpdateService(flightId);
    vector<sockaddr_in> userIds = res.first;
    int remainingSeats = res.second;
    for (auto& userId: userIds) {
        // cout << userId << endl;
        doUpdateService(server, userId, flightId, remainingSeats, status);
    }
}

void Handler::cancelFlight(udp_server &server, char *p, int req_id, int status){
    cout << "################################ Cancel Flight #####################################\n";
    unsigned long clientAddress = server.getClientAddress().sin_addr.s_addr;
    if(status == 2 && checkAndSendOldResponse(server,clientAddress,req_id)) return;
    
    int flightId;
    int length = utils::unmarshalInt(p);
    p += INT_SIZE;
    flightId = utils::unmarshalInt(p);
    
    cout << flightId << endl;
    int cancelOutcome = flightSystem.cancelBooking(clientAddress, flightId);


    // header indicates size of response
    char header[HEADER_SIZE];
    int responseSize = ID_SIZE+STATUS_SIZE+INT_SIZE*2;
    utils::marshalInt(responseSize,header);
    // response
    char response[responseSize];
    char *cur = response;
    // responseId
    int responseID = getResponseID();
    utils::marshalInt(responseID,cur);
    cur += ID_SIZE;
    // status
    utils::marshalString(ACK,cur);
    cur += STATUS_SIZE;
    // cancel outcome
    utils::marshalInt(cancelOutcome,cur);
    
    cout << "##########Saving response with status byte " << (int)*(response+ID_SIZE) << " into memory##########\n";
    if(status == 2) responses[{clientAddress,req_id}] = string(response,responseSize);
    sendReply(server,header,response,responseSize);
    if(status == 2) ackHandler(server, header, response, responseSize, responseID, status, clientAddress);
    if (cancelOutcome == -1) cout << "Flight not found" << endl;
    else if (cancelOutcome == 0) cout << "No booking found for flight" << endl;
    else cout << cancelOutcome + " seats cancelled for flight " + flightId  << endl;

    pair<vector<sockaddr_in>, int> res = flightSystem.callUpdateService(flightId);
    vector<sockaddr_in> userIds = res.first;
    int remainingSeats = res.second;
    for (auto& userId: userIds) {
        // cout << userId << endl;
        doUpdateService(server, userId, flightId, remainingSeats, status);
    }
}

void Handler::cancelSingleBookings(udp_server &server, char *p, int req_id, int status){
    cout << "################################ Cancel Flight #####################################\n";
    unsigned long clientAddress = server.getClientAddress().sin_addr.s_addr;
    if(status == 2 && checkAndSendOldResponse(server,clientAddress,req_id)) return;
    
    int flightId, seatsToCancel;
    int length = utils::unmarshalInt(p);
    p += INT_SIZE;
    flightId = utils::unmarshalInt(p);
    p += INT_SIZE;
    length = utils::unmarshalInt(p);
    p += length;
    seatsToCancel = utils::unmarshalInt(p);
    
    cout << flightId << endl;
    int cancelOutcome = flightSystem.cancelSingleBooking(clientAddress, flightId, seatsToCancel);


    // header indicates size of response
    char header[HEADER_SIZE];
    int responseSize = ID_SIZE+STATUS_SIZE+INT_SIZE*2;
    utils::marshalInt(responseSize,header);
    // response
    char response[responseSize];
    char *cur = response;
    // responseId
    int responseID = getResponseID();
    utils::marshalInt(responseID,cur);
    cur += ID_SIZE;
    // status
    utils::marshalString(ACK,cur);
    cur += STATUS_SIZE;
    // cancel outcome
    utils::marshalInt(cancelOutcome,cur);
    
    cout << "##########Saving response with status byte " << (int)*(response+ID_SIZE) << " into memory##########\n";
    if(status == 2) responses[{clientAddress,req_id}] = string(response,responseSize);
    sendReply(server,header,response,responseSize);
    if(status == 2) ackHandler(server, header, response, responseSize, responseID, status, clientAddress);
    if (cancelOutcome == -1) cout << "Flight not found" << endl;
    else if (cancelOutcome == 0) cout << "No booking found for flight" << endl;
    else cout << cancelOutcome + " seats cancelled for flight " + flightId  << endl;

    pair<vector<sockaddr_in>, int> res = flightSystem.callUpdateService(flightId);
    vector<sockaddr_in> userIds = res.first;
    int remainingSeats = res.second;
    for (auto& userId: userIds) {
        // cout << userId << endl;
        doUpdateService(server, userId, flightId, remainingSeats, status);
    }
}

void Handler::registerUpdateService(udp_server &server, char *p, int req_id, int status){
    cout << "################################ Register Update Service #####################################\n";
    unsigned long clientAddress = server.getClientAddress().sin_addr.s_addr;
    if(status == 2 && checkAndSendOldResponse(server,clientAddress,req_id)) return;

    sockaddr_in cAddress = server.getClientAddress();

    int flightId, seconds;
    int length = utils::unmarshalInt(p);
    p += INT_SIZE;
    flightId = utils::unmarshalInt(p);
    p += length;
    length = utils::unmarshalInt(p);
    p += INT_SIZE;
    seconds = utils::unmarshalInt(p);
    
    cout << flightId << " " << seconds << endl;
    bool isRegistered = flightSystem.registerUpdateService(cAddress, flightId, seconds);

    // header indicates size of response
    char header[HEADER_SIZE];
    int responseSize = ID_SIZE+STATUS_SIZE+INT_SIZE*2+BOOL_SIZE;
    utils::marshalInt(responseSize,header);
    // response
    char response[responseSize];
    char *cur = response;
    // responseId
    int responseID = getResponseID();
    utils::marshalInt(responseID,cur);
    cur += ID_SIZE;
    // status
    utils::marshalString(ACK,cur);
    cur += STATUS_SIZE;
    // seconds
    utils::marshalInt(seconds,cur);
    cur += INT_SIZE;
    // cancel outcome
    utils::marshalBool(isRegistered,cur);
    
    cout << "##########Saving response with status byte " << (int)*(response+ID_SIZE) << " into memory##########\n";
    if(status == 2) responses[{clientAddress,req_id}] = string(response,responseSize);
    sendReply(server,header,response,responseSize);
    if(status == 2) ackHandler(server, header, response, responseSize, responseID, status, clientAddress);
    if (isRegistered) cout << "Registered for update service" << endl;
    else cout << "Flight not found" << endl;
}

void Handler::doUpdateService(udp_server &server, sockaddr_in cAddress, int flightId, int seats, int status){
    cout << "################################ Executing Update Service #####################################\n";
    
    // cout << "client address " << cAddress << " flight id " << flightId << " seats " << seats << endl;
    // header indicates size of response
    char header[HEADER_SIZE];
    int responseSize = ID_SIZE+STATUS_SIZE+INT_SIZE*2;
    utils::marshalInt(responseSize,header);
    // response
    char response[responseSize];
    char *cur = response;
    // responseId
    int responseID = getResponseID();
    utils::marshalInt(responseID,cur);
    cur += ID_SIZE;
    // status
    utils::marshalString(ACK,cur);
    cur += STATUS_SIZE;
    // flightId
    utils::marshalInt(flightId,cur);
    cur += INT_SIZE;
    // seats
    utils::marshalInt(seats,cur);

    server.send(header,HEADER_SIZE, cAddress, sizeof(cAddress));
    server.send(response, responseSize, cAddress, sizeof(cAddress));

    cout << "after ack handler" << endl;
}