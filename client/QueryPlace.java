package client;

import java.io.*;
import java.util.*;

class QueryPlace{

    public static byte[] createMessage(Scanner scanner, int id)throws UnsupportedEncodingException{
        System.out.println(Constants.SEPARATOR);
        System.out.println("Querying flights by places...\n");

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
        System.out.println(Constants.SEPARATOR);
        System.out.printf("Source Place: %s\n", sourcePlace);
        System.out.printf("Destination Place: %s\n", destinationPlace);
        System.out.println(Constants.CONFIRM_MSG);
        String confirm = scanner.nextLine();

        if (confirm.toLowerCase().equals("y")){
            return QueryPlace.constructMessage(sourcePlace, destinationPlace, id);
        }
        return new byte[0];
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

        int ptr = 0;
        String statusStr = Utils.unmarshalString(response, ptr, Constants.RESPONSE_TYPE_SIZE);
        int status = Integer.parseInt(statusStr);
        ptr += Constants.RESPONSE_TYPE_SIZE;

        if (debug) System.out.printf("[DEBUG][QueryPlace][Status = %d]\n", status);

        switch(status){
            case Constants.NAK:
                if (debug) System.out.println("[DEBUG][QueryPlace][Unsuccessful response]");

                String errMsg = Utils.unmarshalMsgString(response, ptr);
                System.out.printf(Constants.ERR_MSG, errMsg);
                break;
            case Constants.ACK:
                if (debug) System.out.println("[DEBUG][QueryPlace][Successful response]");

                int flightCount = Utils.unmarshalInteger(response, ptr);

                if (flightCount == 0) System.out.println("There are no flights that match the source and destination places!\n");
                else System.out.println("Found " + flightCount + " flight(s) that match the souce and destination places!\n");

                ptr += Constants.INT_SIZE;
                int flightId;

                for (int i=0; i< flightCount; i++){
                    flightId = Utils.unmarshalInteger(response, ptr);
                    System.out.println("Flight ID: " + flightId);
                    ptr += Constants.INT_SIZE;
                }

                break;
            default:
                System.out.println(Constants.INVALID_RESPONSE);
        }
        System.out.println();
        System.out.println(Constants.SEPARATOR);
    }
}