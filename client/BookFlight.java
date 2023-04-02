package client;

import java.io.*;
import java.util.*;

class BookFlight{

    public static byte[] createMessage(Scanner scanner, int id)throws UnsupportedEncodingException{
        System.out.println(Constants.SEPARATOR);
        System.out.println("Booking a flight...\n");

        // flight id
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

        // seats
        String seatsString;
        int seats;
        while(true){
            System.out.print("Enter number of seats: ");
            seatsString = scanner.nextLine();

            if (seatsString.length() < 1) System.out.println("number of seats cannot be empty!\n");
            else if (!seatsString.chars().allMatch(Character::isDigit)) System.out.println("number of seats must be all digits!\n");
            else {
                seats = Integer.parseInt(seatsString);
                break;
            }
        }
        
        System.out.println();
        System.out.println(Constants.SEPARATOR);
        System.out.printf("Flight ID: %d\n", flightId);
        System.out.printf("Number of seats: %d\n", seats);
        System.out.println(Constants.CONFIRM_MSG);
        String confirm = scanner.nextLine();

        if (confirm.toLowerCase().equals("y")){
            return BookFlight.constructMessage(flightId, seats, id);
        }
        return new byte[0];
    }

    public static byte[] constructMessage(int flightId, int seats, int id)throws UnsupportedEncodingException{
        List message = new ArrayList();
        Utils.append(message, id);
        Utils.append(message, Constants.BOOK_FLIGHT);
        Utils.appendMessage(message, flightId);
        Utils.appendMessage(message, seats);

        return Utils.byteUnboxing(message);
    }


    public static void handleResponse(byte[] response, boolean debug){
        System.out.println(Constants.SEPARATOR);

        int ptr = 0;
        String statusStr = Utils.unmarshalString(response, ptr, Constants.RESPONSE_TYPE_SIZE);
        int status = Integer.parseInt(statusStr);
        ptr += Constants.RESPONSE_TYPE_SIZE;

        if (debug) System.out.printf("[DEBUG][BookFlight][Status = %d]\n", status);

        switch(status){
            case Constants.NAK:
                if (debug) System.out.println("[DEBUG][BookFlight][Unsuccessful response]");

                String errMsg = Utils.unmarshalMsgString(response, ptr);
                System.out.printf(Constants.ERR_MSG, errMsg);
                break;
            case Constants.ACK:
                if (debug) System.out.println("[DEBUG][BookFlight][Successful response]");

                int seatsFound = Utils.unmarshalInteger(response, ptr);
                ptr += Constants.INT_SIZE;
                int seatsBooked = Utils.unmarshalInteger(response, ptr);

                if (seatsFound == -1) System.out.println("There are no flights that match given flight ID!\n");
                else if (seatsBooked == 0) System.out.println("Insufficient seats, only " + seatsFound + " seat(s) left!\n");
                else System.out.println("Booked " + seatsBooked + " seats for the flight!\n");

                break;
            default:
                System.out.println(Constants.INVALID_RESPONSE);
        }
        System.out.println();
        System.out.println(Constants.SEPARATOR);
    }
}