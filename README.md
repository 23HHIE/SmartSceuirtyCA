# SmartSecurityCA

# Note:Newly created repo after readjusting the request definition in the proto file - 2023/08/08

# ds project start

# About the stub
    The difference between blocking stub and async stub:
    the blocking stub used for the unary and the service streaming communicating, and the async stub is
    suitable for more complicated case such as the client streaming and bidirectional streaming.

# Different stubs sharing a channel to communicate for improving the performance and reducing memory usage

# Note: should add some error handling in the server side or in the client side

# The reason of choosing multicast DNS as a register port for gRPC:
      1-mDNS allows devices to announce their services through broadcasting within the local network
      2-mDNS allows other devices to discover and access these services through service names instead of relying

# Register
    mDNS register --> service register on the mDNS to be accessed by the mDNS -->client communicate with the server by
    locating the register address and port

# Port
    port in jmDNS is used for the registration and discovery of the service
    port in gRPC is responsible for requests and responds of gRPC 

# ServiceListener interface
    It is used for discovery the service during gRPC
    Collaborating with the mDNS, it is responsible for fetching the service information for the client
         
