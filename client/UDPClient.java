package client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.*;

class UDPClient
{
    private DatagramSocket clientSocket;
    private InetAddress IPAddress;
    private int port;
    private double failureRate;
    private int idCounter;
    private int semInvo;
    private int maxTimeout;
    private int timeout;
    private Map<Integer, Boolean> handledResponse;
    private boolean debug;

    public UDPClient(String ip, int port, boolean debug) throws SocketException, UnknownHostException{
        this.clientSocket = new DatagramSocket();
        this.setTimeout(Constants.DEFAULT_NO_TIMEOUT, Constants.DEFAULT_MAX_TIMEOUT);

        this.IPAddress = InetAddress.getByName(ip);
        this.port = port;
        this.failureRate = Constants.DEFAULT_FAILURE_RATE;
        this.idCounter = 0;
        this.semInvo = Constants.NO_SEM_INVO;
        this.handledResponse = new HashMap<Integer, Boolean>();
        this.debug = debug;
    }

    public void setFailureRate(double failureRate){
        this.failureRate = failureRate;
    }

    public void setSemInvo(int semInvo){
        this.semInvo = semInvo;
    }

    public void setTimeout(int timeout) throws SocketException{
        clientSocket.setSoTimeout(timeout);
        this.timeout = timeout;
    }

    public void setTimeout(int timeout, int maxTimeout) throws SocketException{
        clientSocket.setSoTimeout(timeout);
        this.timeout = timeout;
        this.maxTimeout = maxTimeout;
    }

    public int getID(){
        this.idCounter++;
        return this.idCounter;
    }
    
    public int getSemInvo(){
        return this.semInvo;
    }

    public int getTimeout(){
        return this.timeout;
    }

    public void send(byte[] message) throws IOException, InterruptedException{
        if (Math.random() < this.failureRate){
            if (this.debug) System.out.println("[DEBUG][UPDClient][SIMULATING SENDING FAILURE ...]");
            return;
        }

        byte[] header = Utils.marshal(message.length);
        DatagramPacket headerPacket = new DatagramPacket(header, header.length, this.IPAddress, this.port);
        System.out.println("Ip address"+this.IPAddress);
        this.clientSocket.send(headerPacket);

        DatagramPacket sendPacket = new DatagramPacket(message, message.length, this.IPAddress, this.port);
        this.clientSocket.send(sendPacket);
    }

    public byte[] receive(boolean monitor) throws IOException, InterruptedException{
        int responseID;
        int messageLength;
        DatagramPacket receivePacket;
        do{
            System.out.println("waiting here");
            byte[] header = new byte[4];
            DatagramPacket headerPacket = new DatagramPacket(header, header.length);
            this.clientSocket.receive(headerPacket);

            messageLength = Utils.unmarshalInteger(headerPacket.getData(), 0);

            byte[] receiveData = new byte[messageLength];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            this.clientSocket.receive(receivePacket);
            responseID = Utils.unmarshalInteger(receivePacket.getData(), 0);

            if (this.debug) System.out.printf("[DEBUG][UPDClient][RECEIVE RESPONSE: %d]\n", responseID);
            if (this.semInvo >= Constants.AT_MOST_ONE_SEM_INVO && this.handledResponse.containsKey(responseID)){
                if (this.debug) System.out.printf("[DEBUG][UPDClient][SEND ACK: %d][DUPLICATE RESPONSE]\n", responseID);
                this.sendACK(responseID);
            }else{
                break;
            }
        } while(this.semInvo >= Constants.AT_MOST_ONE_SEM_INVO);

        if(this.getSemInvo() >= Constants.AT_MOST_ONE_SEM_INVO || (monitor && this.getSemInvo() >= Constants.AT_LEAST_ONE_SEM_INVO)){
            if (this.debug) System.out.printf("[DEBUG][UPDClient][SEND ACK: %d][NEW RESPONSE]\n", responseID);
            this.sendACK(responseID);
        }

        this.handledResponse.put(responseID, true);
        return Arrays.copyOfRange(receivePacket.getData(), Constants.INT_SIZE, messageLength);
    }

    public void sendACK(int curID) throws IOException, InterruptedException{
        List message = new ArrayList();
        Utils.append(message, curID);
        Utils.append(message, Constants.ACK_CHAR);

        this.send(Utils.byteUnboxing(message));
    }

    public byte[] sendAndReceive(byte[] packageByte, int curID) throws IOException, InterruptedException, TimeoutException{
        byte[] response = new byte[0];
        int timeoutCount = 0;
        do{
            try{
                this.send(packageByte);
                response = this.receive(false);
                break;
            } catch(SocketTimeoutException e){
                timeoutCount++;
                System.out.printf(Constants.TIMEOUT_MSG, timeoutCount, this.maxTimeout);
                if (this.maxTimeout > 0 && timeoutCount >= this.maxTimeout){
                    throw new TimeoutException(Constants.MAX_TIMEOUT_MSG);
                }
                continue;
            }
        } while(this.getSemInvo() >= Constants.AT_LEAST_ONE_SEM_INVO);
        return response;
    }

    public static void main(String[] args)throws Exception{
        Options options = new Options();

        Option opHost = new Option("h", "host", true, "Server host");
        opHost.setRequired(true);
        options.addOption(opHost);

        Option opPort = new Option("p", "port", true, "Server port");
        opPort.setRequired(true);
        opPort.setType(Integer.TYPE);
        options.addOption(opPort);

        Option opAtLeastOnce = new Option("al", "atleast", false, "Enable at least once invocation semantic");
        options.addOption(opAtLeastOnce);

        Option opAtMostOnce = new Option("am", "atmost", false, "Enable at most once invocation semantic");
        options.addOption(opAtMostOnce);

        Option opFailureRate = new Option("fr", "failurerate", true, "Set failure rate (float)");
        opFailureRate.setType(Double.TYPE);
        options.addOption(opFailureRate);

        Option opTimeout = new Option("to", "timeout", true, "Set timeout in millisecond");
        opTimeout.setType(Integer.TYPE);
        options.addOption(opTimeout);

        Option opTimeoutCount = new Option("mt", "maxtimeout", true, "Set timeout max count");
        opTimeoutCount.setType(Integer.TYPE);
        options.addOption(opTimeoutCount);

        Option opDebug = new Option("v", "verbose", false, "Enable verbose print for debugging");
        options.addOption(opDebug);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        String host = Constants.DEFAULT_HOST;
        int port = Constants.DEFAULT_PORT;
        boolean atLeastOnce = false;
        boolean atMostOnce = false;
        double failureRate = Constants.DEFAULT_FAILURE_RATE;
        int timeout = Constants.DEFAULT_NO_TIMEOUT;
        int maxTimeout = Constants.DEFAULT_MAX_TIMEOUT;
        boolean debug = false;

        try {
            cmd = parser.parse(options, args);
            host = cmd.getOptionValue("host");
            port = Integer.parseInt(cmd.getOptionValue("port"));
            atLeastOnce = cmd.hasOption("atleast");
            if (atLeastOnce){
                timeout = Constants.DEFAULT_TIMEOUT;
            }
            atMostOnce = cmd.hasOption("atmost");
            if (atMostOnce){
                timeout = Constants.DEFAULT_TIMEOUT;
            }
            if (cmd.hasOption("failurerate")){
                failureRate = Double.parseDouble(cmd.getOptionValue("failurerate"));
            }
            if (cmd.hasOption("timeout")){
                timeout = Integer.parseInt(cmd.getOptionValue("timeout"));
            }
            if (cmd.hasOption("maxtimeout")){
                maxTimeout = Integer.parseInt(cmd.getOptionValue("maxtimeout"));
            }
            debug = cmd.hasOption("verbose");
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("UDPClient", options);

            System.exit(1);
            return;
        }

        try{
            System.out.print(Constants.SEPARATOR);
            System.out.println("Welcome to the Distributed Flight System!");
            System.out.println(Constants.SEPARATOR);
            System.out.println("Client ip "+host);
            UDPClient udpClient = new UDPClient(host, port, debug);
            if (atLeastOnce){
                udpClient.setSemInvo(Constants.AT_LEAST_ONE_SEM_INVO);
                udpClient.setTimeout(timeout, maxTimeout);
            }
            if (atMostOnce){
                udpClient.setSemInvo(Constants.AT_MOST_ONE_SEM_INVO);
                udpClient.setTimeout(timeout, maxTimeout);
            }
            udpClient.setFailureRate(failureRate);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while(!exit){
                System.out.println("Select an option");
                System.out.println("1. Query flight by source and destination place.");
                System.out.println("2. Query flight by flight ID.");
                System.out.println("3. Query flight you have booked.");
                System.out.println("4. Query all flights.");
                System.out.println("5. Book a flight.");
                System.out.println("6. Cancel a flight.");
                System.out.println("7. Register for flight update service.");
                System.out.println("8. Exit.\n");
                System.out.print("You have entered: \n");
                int option = Integer.parseInt(scanner.nextLine());

                byte[] packageByte;
                int curID = udpClient.getID();
                switch(option){
                    case Constants.QUERY_PLACE:
                        try {
                            packageByte = QueryPlace.createMessage(scanner, curID);
                            if (packageByte.length > 0) {
                                byte[] response = udpClient.sendAndReceive(packageByte, curID);
                                QueryPlace.handleResponse(response, debug);
                            }
                        } catch (Exception e) {
                            System.out.print(Constants.SEPARATOR);
                            System.out.printf(Constants.ERR_MSG, e.getMessage());
                            if (debug) throw(e);
                        }
                        break;
                    case Constants.QUERY_FLIGHT_ID:
                        try {
                            packageByte = QueryFlightId.createMessage(scanner, curID);
                            if (packageByte.length > 0) {
                                byte[] response = udpClient.sendAndReceive(packageByte, curID);
                                QueryFlightId.handleResponse(response, debug);
                            }
                        } catch (Exception e) {
                            System.out.print(Constants.SEPARATOR);
                            System.out.printf(e.getMessage());
                            if (debug) throw(e);
                        }
                        break;
                    case Constants.QUERY_USER_ID:
                        try {
                            packageByte = QueryBooking.createMessage(scanner, curID);
                            if (packageByte.length > 0) {
                                byte[] response = udpClient.sendAndReceive(packageByte, curID);
                                QueryBooking.handleResponse(response, debug);
                            }
                        } catch (Exception e) {
                            System.out.print(Constants.SEPARATOR);
                            System.out.printf(e.getMessage());
                            if (debug) throw(e);
                        }
                        break;
                    case Constants.QUERY_ALL_FLIGHTS:
                        try {
                            packageByte = QueryAllFlights.createMessage(scanner, curID);
                            if (packageByte.length > 0) {
                                byte[] response = udpClient.sendAndReceive(packageByte, curID);
                                QueryAllFlights.handleResponse(response, debug);
                            }
                        } catch (Exception e) {
                            System.out.print(Constants.SEPARATOR);
                            System.out.printf(e.getMessage());
                            if (debug) throw(e);
                        }
                        break;
                    case Constants.BOOK_FLIGHT:
                        try {
                            packageByte = BookFlight.createMessage(scanner, curID);
                            if (packageByte.length > 0) {
                                byte[] response = udpClient.sendAndReceive(packageByte, curID);
                                BookFlight.handleResponse(response, debug);
                            }
                        } catch (Exception e) {
                            System.out.print(Constants.SEPARATOR);
                            System.out.printf(e.getMessage());
                            if (debug) throw(e);
                        }
                        break;
                    case Constants.CANCEL_FLIGHT:
                        try {
                            packageByte = CancelFlight.createMessage(scanner, curID);
                            if (packageByte.length > 0) {
                                byte[] response = udpClient.sendAndReceive(packageByte, curID);
                                CancelFlight.handleResponse(response, debug);
                            }
                        } catch (Exception e) {
                            System.out.print(Constants.SEPARATOR);
                            System.out.printf(e.getMessage());
                            if (debug) throw(e);
                        }
                        break;
                    case Constants.REGISTER_UPDATE_SERVICE:
                        try {
                            packageByte = RegisterUpdateService.createMessage(scanner, curID);
                            if (packageByte.length > 0) {
                                byte[] response = udpClient.sendAndReceive(packageByte, curID);
                                int seconds = RegisterUpdateService.handleResponse(response, debug);
                                udpClient.setTimeout(seconds);
                                try {
                                    byte[] updateServiceResponse = udpClient.receive(false);
                                    RegisterUpdateService.handleUpdateServiceResponse(updateServiceResponse, debug);
                                } catch (SocketTimeoutException e) {
                                    System.out.println("Update service has expired!\nDeregistering from the service...\n");
                                }
                                udpClient.setTimeout(0);
                            }
                        } catch (Exception e) {
                            System.out.print(Constants.SEPARATOR);
                            System.out.printf(e.getMessage());
                            if (debug) throw(e);
                        }
                        break;
                    case Constants.EXIT:
                        System.out.println("Quiting program.....");
                        exit = true;
                        break;
                    default:
                        System.out.println("Please enter a valid option");
                }
                System.out.println(Constants.SEPARATOR);
            }
        }
        catch(Exception e){
            System.out.print(Constants.SEPARATOR);
            System.out.printf(Constants.ERR_MSG, e.getMessage());
            System.out.println("Quiting program.....");
            System.out.println(Constants.SEPARATOR);
            if (debug) throw(e);
        }
    }
}
