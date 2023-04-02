package client;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

class QueryAllFlights{

    public static byte[] createMessage(Scanner scanner, int id)throws UnsupportedEncodingException{
        System.out.println(Constants.SEPARATOR);
        System.out.println("Querying all flights...\n");

        System.out.println(Constants.CONFIRM_MSG);
        String confirm = scanner.nextLine();

        if (confirm.toLowerCase().equals("y")){
            return QueryAllFlights.constructMessage(id);
        }
        return new byte[0];
    }

    public static byte[] constructMessage(int id)throws UnsupportedEncodingException{
        List message = new ArrayList();
        Utils.append(message, id);
        Utils.append(message, Constants.QUERY_ALL_FLIGHTS);

        return Utils.byteUnboxing(message);
    }


    public static void handleResponse(byte[] response, boolean debug){
        System.out.println(Constants.SEPARATOR);

        int ptr = 0;
        String statusStr = Utils.unmarshalString(response, ptr, Constants.RESPONSE_TYPE_SIZE);
        int status = Integer.parseInt(statusStr);
        ptr += Constants.RESPONSE_TYPE_SIZE;

        if (debug) System.out.printf("[DEBUG][QueryAllFlights][Status = %d]\n", status);

        switch(status){
            case Constants.NAK:
                if (debug) System.out.println("[DEBUG][QueryAllFlights][Unsuccessful response]");

                String errMsg = Utils.unmarshalMsgString(response, ptr);
                System.out.printf(Constants.ERR_MSG, errMsg);
                break;
            case Constants.ACK:
                if (debug) System.out.println("[DEBUG][QueryAllFlights][Successful response]");

                int flightCount = Utils.unmarshalInteger(response, ptr);

                if (flightCount == 0) System.out.println("There are no flights!\n");
                else System.out.println("Found " + flightCount + " flight(s)!\n");

                ptr += Constants.INT_SIZE;
                int flightId, unixFlightTime, seatsAvailable, strSize;
                String source, destination;
                Float airfare;

                for (int i=0; i< flightCount; i++){
                    flightId = Utils.unmarshalInteger(response, ptr);
                    ptr += Constants.INT_SIZE;

                    strSize = Utils.unmarshalInteger(response, ptr);
                    ptr += Constants.INT_SIZE;
                    source = Utils.unmarshalString(response, ptr, ptr+strSize);
                    ptr += strSize;

                    strSize = Utils.unmarshalInteger(response, ptr);
                    ptr += Constants.INT_SIZE;
                    destination = Utils.unmarshalString(response, ptr, ptr+strSize);
                    ptr += strSize;

                    unixFlightTime = Utils.unmarshalInteger(response, ptr);
                    ptr += Constants.INT_SIZE;

                    airfare = Utils.unmarshalFloat(response, ptr);
                    ptr += Constants.FLOAT_SIZE;

                    seatsAvailable = Utils.unmarshalInteger(response, ptr);
                    ptr += Constants.INT_SIZE;

                    Date flightTime = new Date((long)unixFlightTime*1000);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    System.out.printf("\nFlight ID: %d\n", flightId);
                    System.out.printf("Source Place: %s\n", source);
                    System.out.printf("Destination Place: %s\n", destination);
                    System.out.printf("Flight Date and Time: " + formatter.format(flightTime) + "\n");
                    System.out.printf("Air Fare: $%.2f\n", airfare);
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