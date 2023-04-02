package client;

/**
 * Constants class, contains all constant used in the client
 */
class Constants{
    // Argument Constant
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 8080;
    public static final double DEFAULT_FAILURE_RATE = 0.0;
    public static final int DEFAULT_TIMEOUT = 1000;
    public static final int DEFAULT_NO_TIMEOUT = 0;
    public static final int DEFAULT_MAX_TIMEOUT = 0;

    // Connection Constant
    public static final int ACK = 1;
    public static final String ACK_CHAR = "1";
    public static final int NAK = 0;
    public static final String NAK_CHAR = "0";
    public static final int RESPONSE_TYPE_SIZE = 1;
    public static final String INVALID_RESPONSE = "Sorry we are having problem in the server";
    public static final String TIMEOUT_MSG = "Timeout!, resending request ... (%d/%d)\n";
    public static final String MAX_TIMEOUT_MSG = "Max timeout limit exceeded\n";

    public static final int NO_SEM_INVO = 0;
    public static final int AT_LEAST_ONE_SEM_INVO = 1;
    public static final int AT_MOST_ONE_SEM_INVO = 2;

    // Type Constant
    public static final int INT_SIZE = 4;
    public static final int FLOAT_SIZE = 4;

    // Service constant
    public static final int QUERY_PLACE = 1;
    public static final int QUERY_FLIGHT_ID = 2;
    public static final int QUERY_USER_ID = 3;
    public static final int QUERY_ALL_FLIGHTS = 4;
    public static final int BOOK_FLIGHT = 5;
    public static final int CANCEL_FLIGHT = 6;
    public static final int CANCEL_SINGLE_FLIGHT = 7;
    public static final int REGISTER_UPDATE_SERVICE = 8;
    public static final int EXIT = 9;

    // Main UI Constant
    public static final String ERR_MSG = "Error: %s\n";
    public static final String SUCCESS_MSG = "SUCCESS!";
    public static final String SEPARATOR = "================================================================================\n";
    public static final String EXIT_SVC_MSG = "8. Exit.";
    public static final String CHOICE_SVC_MSG = "Your choice: ";

    public static final String QUERY_SOURCE_PLACE = "Enter your source place: ";
    public static final String CONFIRM_MSG = "Are you sure? (Y/N) ";
}