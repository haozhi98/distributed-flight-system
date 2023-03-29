package client;

import java.io.*;
import java.util.*;

class QueryFlightId{

    public static byte[] createMessage(Scanner scanner, int id)throws UnsupportedEncodingException{
        System.out.println(Constants.SEPARATOR);
        System.out.println(Constants.OPEN_MSG);

        // Source place
        String sourcePlace;
        while(true){
            System.out.print("Enter source place: ");
            sourcePlace = scanner.nextLine();

            if (sourcePlace.length() > 0) break;
            System.out.println("Source place cannot be empty!\n");
        }

        // Destination place
        String destinationPlace;
        while(true){
            System.out.print("Enter destination place: ");
            destinationPlace = scanner.nextLine();

            if (destinationPlace.length() > 0) break;
            System.out.println("Destination place cannot be empty!\n");
        }
        System.out.println();
        if (QueryFlightId.confirm(sourcePlace, destinationPlace, scanner)){
            return QueryFlightId.constructMessage(sourcePlace, destinationPlace, id);
        }
        return new byte[0];
    }

    public static boolean confirm(String sourcePlace, String destinationPlace, Scanner scanner){
        System.out.println(Constants.SEPARATOR);
        System.out.println(Constants.CONFIRM_SUMMARY);
        System.out.println();
        System.out.printf("Source Place: %s\n", sourcePlace);
        System.out.printf("Source Place: %s\n", destinationPlace);
        String confirm = scanner.nextLine();

        return confirm.toLowerCase().equals("y");
    }

    public static byte[] constructMessage(String sourcePlace, String destinationPlace, int id)throws UnsupportedEncodingException{
        List message = new ArrayList();
        Utils.append(message, id);
        Utils.append(message, Constants.QUERY_PLACE);
        Utils.appendMessage(message, sourcePlace);
        Utils.appendMessage(message, destinationPlace);

        return Utils.byteUnboxing(message);
    }


    public static void handleResponse(byte[] response, boolean debug){
        System.out.println(Constants.SEPARATOR);
        String statusStr = Utils.unmarshalString(response, 0, Constants.RESPONSE_TYPE_SIZE);
        int status = Integer.parseInt(statusStr);
        if (debug) System.out.printf("[DEBUG][HandleOpenAccount][Status = %d]\n", status);
        switch(status){
            case Constants.NAK:
                if (debug) System.out.println("[DEBUG][HandleOpenAccount][Unsuccessful response]");
                String errMsg = Utils.unmarshalMsgString(response, Constants.RESPONSE_TYPE_SIZE);
                System.out.printf(Constants.ERR_MSG, errMsg);
                break;
            case Constants.ACK:
                if (debug) System.out.println("[DEBUG][HandleOpenAccount][Successful response]");
                int accountNumber = Utils.unmarshalMsgInteger(response, Constants.RESPONSE_TYPE_SIZE);
                System.out.println(Constants.SUCCESS_MSG);
                System.out.printf(Constants.SUCCESSFUL_OPEN_ACCOUNT, accountNumber);
                break;
            default:
                System.out.println(Constants.INVALID_RESPONSE);
        }
        System.out.println();
        System.out.println(Constants.SEPARATOR);
    }
}