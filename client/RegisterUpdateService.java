package client;

import java.io.*;
import java.util.*;

class RegisterUpdateService{

    public static byte[] createMessage(Scanner scanner, int id)throws UnsupportedEncodingException{
        System.out.println(Constants.SEPARATOR);
        System.out.println("Registering for update service...\n");

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

        // monitor interval
        String secondString;
        int seconds;
        while(true){
            System.out.print("Enter number of seconds to register for update service: ");
            secondString = scanner.nextLine();

            if (secondString.length() < 1) System.out.println("number of seconds cannot be empty!\n");
            else if (!secondString.chars().allMatch(Character::isDigit)) System.out.println("number of seconds must be all digits!\n");
            else {
                seconds = Integer.parseInt(secondString);
                break;
            }
        }
        
        System.out.println();
        System.out.println(Constants.SEPARATOR);
        System.out.println(Constants.CONFIRM_SUMMARY);
        System.out.printf("Flight ID: %d\n", flightId);
        System.out.printf("Number of seconds for monitor update service: %d\n", seconds);
        System.out.println(Constants.CONFIRM_MSG);
        String confirm = scanner.nextLine();

        if (confirm.toLowerCase().equals("y")){
            return RegisterUpdateService.constructMessage(flightId, seconds, id);
        }
        return new byte[0];
    }

    public static byte[] constructMessage(int flightId, int seconds, int id)throws UnsupportedEncodingException{
        List message = new ArrayList();
        Utils.append(message, id);
        Utils.append(message, Constants.REGISTER_UPDATE_SERVICE);
        Utils.appendMessage(message, flightId);
        Utils.appendMessage(message, seconds);

        return Utils.byteUnboxing(message);
    }


    public static int handleResponse(byte[] response, boolean debug){
        System.out.println(Constants.SEPARATOR);

        int ptr = 0;
        String statusStr = Utils.unmarshalString(response, ptr, Constants.RESPONSE_TYPE_SIZE);
        int status = Integer.parseInt(statusStr);
        ptr += Constants.RESPONSE_TYPE_SIZE;

        if (debug) System.out.printf("[DEBUG][RegisterUpdateService][Status = %d]\n", status);

        switch(status){
            case Constants.NAK:
                if (debug) System.out.println("[DEBUG][RegisterUpdateService][Unsuccessful response]");

                String errMsg = Utils.unmarshalMsgString(response, ptr);
                System.out.printf(Constants.ERR_MSG, errMsg);
                break;
            case Constants.ACK:
                if (debug) System.out.println("[DEBUG][RegisterUpdateService][Successful response]");

                System.out.println("Total response length is : " + response.length);
                System.out.println("Current response length is : " + (response.length - ptr));

                int seconds = Utils.unmarshalInteger(response, ptr)*1000;
                ptr += Constants.INT_SIZE;
                boolean isRegistered = Utils.unmarshalBool(response, ptr);

                if (isRegistered) System.out.println("Successfully registered for update service!\n");
                else System.out.println("Flight not found!\n");
                return seconds;
            default:
                System.out.println(Constants.INVALID_RESPONSE);
        }
        System.out.println();
        System.out.println(Constants.SEPARATOR);
        return 0;
    }

    public static void handleUpdateServiceResponse(byte[] response, boolean debug){
        System.out.println(Constants.SEPARATOR);

        int ptr = 0;
        String statusStr = Utils.unmarshalString(response, ptr, Constants.RESPONSE_TYPE_SIZE);
        int status = Integer.parseInt(statusStr);
        ptr += Constants.RESPONSE_TYPE_SIZE;

        if (debug) System.out.printf("[DEBUG][RegisterUpdateService][Status = %d]\n", status);

        switch(status){
            case Constants.NAK:
                if (debug) System.out.println("[DEBUG][RegisterUpdateService][Unsuccessful response]");

                String errMsg = Utils.unmarshalMsgString(response, ptr);
                System.out.printf(Constants.ERR_MSG, errMsg);
                break;
            case Constants.ACK:
                if (debug) System.out.println("[DEBUG][RegisterUpdateService][Successful response]");

                System.out.println("Total response length is : " + response.length);
                System.out.println("Current response length is : " + (response.length - ptr));

                boolean isRegistered = Utils.unmarshalBool(response, ptr);

                if (isRegistered) System.out.println("Successfully registered for update service!\n");
                else System.out.println("Flight not found!\n");

                break;
            default:
                System.out.println(Constants.INVALID_RESPONSE);
        }
        System.out.println();
        System.out.println(Constants.SEPARATOR);
    }
}