# Distributed Flight System

This project consolidates basic knowledge about interprocess communication and remote invocation through constructing client and server programs that use UDP as the transport protocol. The client and server programs are designed to simulate a distributed flight system.

[Report](https://github.com/cacad-ntu/distributed-banking-system-report)

## Environment


### Operating System
- Ubuntu


### Programming Languages
- Java 1.9 (Client)
- C++ 11 (Server)

## Getting Started

### Client (Java)
```
cd client
javac -d . -cp .:lib/* *.java
java -cp .:lib/* client.UDPClient -h <HOST NAME> -p <PORT> [-al] [-am] [-fr <FAILURE RATE>] [-to <TIMEOUT>] [-mt <MAX TIMEOUT COUNT>] [-v]
```     

#### Note:
```bash
Options:
-al,--atleast             Enable at least once invocation semantic
-am,--atmost              Enable at most once invocation semantic
-fr,--failurerate <arg>   Set failure rate (float)
-h,--host <arg>           Server host
-mt,--maxtimeout <arg>    Set timeout max count
-p,--port <arg>           Server port
-to,--timeout <arg>       Set timeout in millisecond
-v,--verbose              Enable verbose print for debugging
```


### Server (C++)
```
cd server
g++ -o server -std=c++11 main.cpp udp_server.cpp utils.cpp Handler.cpp Flight.cpp FlightSystem.cpp
./server <PORT> <MODE> <FAULT> <LIMIT>
```

#### Note:

```<MODE>``` is the invocation semantic. Possible values:

- 0: no ack
- 1: at-least-once
- 2: at-most-once

```<FAULT>``` is the probability that server fails to reply

```<LIMIT>``` is the limit of retries



## Supported Service


### Open New Account

#### Request
| Params   | Type         |
| -------- | ------------ |
| Name     | ```String``` |
| Password | ```String``` |
| Currency | ```int```    |
| Balance  | ```float```  |

#### Response
| Params         | Type      |
| -------------- | --------- |
| Account Number | ```int``` |


### Close Existing Account

#### Request
| Params         | Type         |
| -------------- | ------------ |
| Name           | ```String``` |
| Account Number | ```int```    |
| Password       | ```String``` |

#### Response
| Params | Type          |
| ------ | ------------- |
| ACK    | ```boolean``` |


### Deposit/Withdraw Money

#### Request
| Params         | Type         |
| -------------- | ------------ |
| Name           | ```String``` |
| Account Number | ```int```    |
| Password       | ```String``` |
| Currency       | ```int```    |
| Balance        | ```float```  |

#### Response
| Params   | Type        |
| -------- | ----------- |
| Currency | ```int```   |
| Balance  | ```float``` |


### Monitor Updates

#### Request
| Params   | Type      |
| -------- | --------- |
| Duration | ```int``` |

#### Response
| Params | Type          |
| ------ | ------------- |
| ACK    | ```boolean``` |


### Transfer Money

#### Request
| Params                   | Type         |
| ------------------------ | ------------ |
| Name                     | ```String``` |
| Account Number           | ```int```    |
| Recipient Name           | ```String``` |
| Recipient Account Number | ```int```    |
| Password                 | ```String``` |
| Currency                 | ```int```    |
| Balance                  | ```float```  |

#### Response
| Params   | Type        |
| -------- | ----------- |
| Currency | ```int```   |
| Balance  | ```float``` |


### Change Password

#### Request
| Params         | Type         |
| -------------- | ------------ |
| Name           | ```String``` |
| Account Number | ```int```    |
| Old Password   | ```String``` |
| New Password   | ```String``` |

#### Response
| Params | Type          |
| ------ | ------------- |
| ACK    | ```boolean``` |

# data
15 april 1200
15 april 1800
20 april 1530
20 april 2030
21 april 0030