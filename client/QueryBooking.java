package client;

import java.io.*;
import java.util.*;

class QueryBooking{

    public static byte[] createMessage(Scanner scanner, int id)throws UnsupportedEncodingException{
        System.out.println(Constants.SEPARATOR);
        System.out.println("Querying flights by User ID...\n");
        System.out.println(Constants.CONFIRM_MSG);
        String confirm = scanner.nextLine();

        if (confirm.toLowerCase().equals("y")){
            return QueryBooking.constructMessage(id);
        }
        return new byte[0];
    }

    public static byte[] constructMessage(int id)throws UnsupportedEncodingException{
        List message = new ArrayList();
        Utils.append(message, id);
        Utils.append(message, Constants.QUERY_USER_ID);

        return Utils.byteUnboxing(message);
    }

    public static void handleResponse(byte[] response, boolean debug){
        System.out.println(Constants.SEPARATOR);

        int ptr = 0;
        String statusStr = Utils.unmarshalString(response, ptr, Constants.RESPONSE_TYPE_SIZE);
        int status = Integer.parseInt(statusStr);
        ptr += Constants.RESPONSE_TYPE_SIZE;

        if (debug) System.out.printf("[DEBUG][QueryUserId][Status = %d]\n", status);

        switch(status){
            case Constants.NAK:
                if (debug) System.out.println("[DEBUG][QueryUserId][Unsuccessful response]");

                String errMsg = Utils.unmarshalMsgString(response, ptr);
                System.out.printf(Constants.ERR_MSG, errMsg);
                break;
            case Constants.ACK:
                if (debug) System.out.println("[DEBUG][QueryUserId][Successful response]");

                int bookingCount = Utils.unmarshalInteger(response, ptr);

                if (bookingCount == 0) System.out.println("You have no flights booked yet!\n");
                else System.out.println("Found " + bookingCount + " bookings(s)!\n");

                ptr += Constants.INT_SIZE;
                int flightId, tickets;

                for (int i=0; i< bookingCount; i++){
                    flightId = Utils.unmarshalInteger(response, ptr);
                    ptr += Constants.INT_SIZE;
                    tickets = Utils.unmarshalInteger(response, ptr);
                    ptr += Constants.INT_SIZE;
                    System.out.println("\nFlight ID: " + flightId + "\nTickets: " + tickets);
                }
                break;
            default:
                System.out.println(Constants.INVALID_RESPONSE);
        }
        System.out.println();
        System.out.println(Constants.SEPARATOR);
    }
}