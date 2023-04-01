package client;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

class QueryFlightId{

    public static byte[] createMessage(Scanner scanner, int id)throws UnsupportedEncodingException{
        System.out.println(Constants.SEPARATOR);
        System.out.println("Querying flights by flight ID...\n");

        // Source place
        String flightIdString;
        int flightId;
        while(true){
            System.out.print("Enter flight ID: ");
            flightIdString = scanner.nextLine();

            if (flightIdString.length() < 1) System.out.println("Flight ID cannot be empty!\n");
            else if (!flightIdString.chars().allMatch(Character::isDigit)) System.out.println("Flight ID must be all digits!\n");
            else {
                flightId = Integer.parseInt(flightIdString);
                break;
            }
        }

        System.out.println();
        System.out.println(Constants.SEPARATOR);
        System.out.println(Constants.CONFIRM_SUMMARY);
        System.out.printf("Flight ID: %s\n", flightId);
        System.out.println(Constants.CONFIRM_MSG);
        String confirm = scanner.nextLine();

        if (confirm.toLowerCase().equals("y")){
            return QueryFlightId.constructMessage(flightId, id);
        }
        return new byte[0];
    }

    public static byte[] constructMessage(int flightId, int id)throws UnsupportedEncodingException{
        List message = new ArrayList();
        Utils.append(message, id);
        Utils.append(message, Constants.QUERY_FLIGHT_ID);
        Utils.appendMessage(message, flightId);

        return Utils.byteUnboxing(message);
    }


    public static void handleResponse(byte[] response, boolean debug){
        System.out.println(Constants.SEPARATOR);

        int ptr = 0;
        String statusStr = Utils.unmarshalString(response, ptr, Constants.RESPONSE_TYPE_SIZE);
        int status = Integer.parseInt(statusStr);
        ptr += Constants.RESPONSE_TYPE_SIZE;

        if (debug) System.out.printf("[DEBUG][QueryFlightId][Status = %d]\n", status);
        switch(status){
            case Constants.NAK:
                if (debug) System.out.println("[DEBUG][QueryFlightId][Unsuccessful response]");
                String errMsg = Utils.unmarshalMsgString(response, ptr);
                System.out.printf(Constants.ERR_MSG, errMsg);
                break;
            case Constants.ACK:
                if (debug) System.out.println("[DEBUG][QueryFlightId][Successful response]");
                int flightFound = Utils.unmarshalInteger(response, ptr);

                if (flightFound == 0) {System.out.println("There are no flights that match the flight ID");}
                else {
                    ptr += Constants.INT_SIZE;
                    int flightId = Utils.unmarshalInteger(response, ptr);
                    ptr += Constants.INT_SIZE;
                    int unixFlightTime = Utils.unmarshalInteger(response, ptr);
                    ptr += Constants.INT_SIZE;
                    float airFare = Utils.unmarshalFloat(response, ptr);
                    ptr += Constants.FLOAT_SIZE;
                    int seatsAvailable = Utils.unmarshalInteger(response, ptr);

                    Date flightTime = new Date((long)unixFlightTime*1000);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    System.out.printf("\nFlight ID: %d\n", flightId);
                    System.out.printf("Flight Date and Time: " + formatter.format(flightTime) + "\n");
                    System.out.printf("Air Fare: $%.2f\n", airFare);
                    System.out.printf("Seats Available: %d\n", seatsAvailable);
                }
                break;
            default:
                System.out.println(Constants.INVALID_RESPONSE);
        }
        System.out.println();
        System.out.println(Constants.SEPARATOR);
    }
}