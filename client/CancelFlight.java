package client;

import java.io.*;
import java.util.*;

class CancelFlight{

    public static byte[] createMessage(Scanner scanner, int id)throws UnsupportedEncodingException{
        System.out.println(Constants.SEPARATOR);
        System.out.println("Cancelling a flight...\n");

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
        
        System.out.println();
        System.out.println(Constants.SEPARATOR);
        System.out.printf("Flight ID: %d\n", flightId);
        System.out.println(Constants.CONFIRM_MSG);
        String confirm = scanner.nextLine();

        if (confirm.toLowerCase().equals("y")){
            return CancelFlight.constructMessage(flightId, id);
        }
        return new byte[0];
    }

    public static byte[] constructMessage(int flightId, int id)throws UnsupportedEncodingException{
        List message = new ArrayList();
        Utils.append(message, id);
        Utils.append(message, Constants.CANCEL_FLIGHT);
        Utils.appendMessage(message, flightId);

        return Utils.byteUnboxing(message);
    }


    public static void handleResponse(byte[] response, boolean debug){
        System.out.println(Constants.SEPARATOR);

        int ptr = 0;
        String statusStr = Utils.unmarshalString(response, ptr, Constants.RESPONSE_TYPE_SIZE);
        int status = Integer.parseInt(statusStr);
        ptr += Constants.RESPONSE_TYPE_SIZE;

        if (debug) System.out.printf("[DEBUG][CancelFlight][Status = %d]\n", status);

        switch(status){
            case Constants.NAK:
                if (debug) System.out.println("[DEBUG][CancelFlight][Unsuccessful response]");

                String errMsg = Utils.unmarshalMsgString(response, ptr);
                System.out.printf(Constants.ERR_MSG, errMsg);
                break;
            case Constants.ACK:
                if (debug) System.out.println("[DEBUG][CancelFlight][Successful response]");

                int seatsCancelled = Utils.unmarshalInteger(response, ptr);

                if (seatsCancelled == -1) System.out.println("There are no flights that match given flight ID!\n");
                else if (seatsCancelled == 0) System.out.println("No booking found for given flight ID!");
                else System.out.println("Cancelled " + seatsCancelled + " seats for the flight!\n");

                break;
            default:
                System.out.println(Constants.INVALID_RESPONSE);
        }
        System.out.println();
        System.out.println(Constants.SEPARATOR);
    }
}