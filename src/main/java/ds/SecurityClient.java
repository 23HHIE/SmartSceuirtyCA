package ds;

// import ds.monitor.MonitorGrpc;
// import ds.monitor.MonitorImpl;

import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SecurityClient {
    private static final Logger logger = Logger.getLogger(SecurityClient.class.getName());
    private final MonitorGrpc.MonitorBlockingStub blockingStubService1;
    private final MonitorGrpc.MonitorStub asyncService1Stub;

    private final AccessGrpc.AccessStub asyncService2Stub;
    private final IntrusionGrpc.IntrusionBlockingStub blockingStubService3;
    static Random random = new Random();

    public SecurityClient(Channel channel) {
        blockingStubService1 = MonitorGrpc.newBlockingStub(channel);
        asyncService1Stub = MonitorGrpc.newStub(channel);
        asyncService2Stub = AccessGrpc.newStub(channel);
        blockingStubService3 = IntrusionGrpc.newBlockingStub(channel);

    }

    //for Service1 function1（server streaming）
    public void clientSideStreamVideoFrames() {
        logger.info("Calling gRPC server streaming type (from the client side)");
        try {
            SecurityImpl.MyRequest request = SecurityImpl.MyRequest.newBuilder().setMessage
                    ("(Client said:I'd like to check the video monitor, please!)").build();
            Iterator<SecurityImpl.MyReply> reply = blockingStubService1.withDeadlineAfter(1, TimeUnit.SECONDS)
                    .streamVideoFrames(request);
            while (reply.hasNext()) {
                System.out.println(reply.next());
            }
            logger.info("End of server streaming");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }

    //for Service1 function2(bidirectional streaming)
    public void clientSideLongStaying() {
        //the client calls the logger method to record the communication of bidirectional streaming
        logger.info("Calling gRPC bi-directional streaming type(from the client side)");

        //create an observer to call an object of the bidirectional streaming
        StreamObserver<SecurityImpl.MyRequest> requestObserver = asyncService1Stub.longStaying(new StreamObserver<SecurityImpl.MyReply>() {
            @Override
            public void onNext(SecurityImpl.MyReply myReply) {
                System.out.println("the Client received: " + myReply.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("the Client said : Well done. Stream completed");
            }
        });

        //make a request by using onNext method
        requestObserver.onNext(SecurityImpl.MyRequest.newBuilder().setMessage("the Client Said:" +
                "Can you please check if there is any long time staying suspicious").build());
        for (int i = 0; i < random.nextInt(10); i++) {
            requestObserver.onNext(SecurityImpl.MyRequest.newBuilder().setMessage("The winter is coming").build());
        }
        requestObserver.onCompleted();
    }

    //for Service2 function1()
    public void clientSideAccessing() {
        logger.info("Calling gRPC client streaming type (from the client side)");
        StreamObserver<SecurityImpl.MyReply> responseObserver = new StreamObserver<SecurityImpl.MyReply>() {
            @Override
            public void onNext(SecurityImpl.MyReply value) {
                System.out.println("Received: " + value.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Understood. Stream completed!");
            }
        };

        StreamObserver<SecurityImpl.MyRequest> requestObserver = asyncService2Stub.accessing(responseObserver);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int num = random.nextInt(10);
            sb.append(num);
        }
        String password = sb.toString();
        requestObserver.onNext(SecurityImpl.MyRequest.newBuilder().setMessage
                ("Client said: Start entering access control password").build());
        for (int i = 0; i < random.nextInt(10) +1; i++) {
            requestObserver.onNext(SecurityImpl.MyRequest.newBuilder().setMessage(password).build());
        }
        requestObserver.onCompleted();
    }

    //for Service3 function1()
    public void clientSideIntrusion() {
        logger.info("Calling gRPC unary type (from the client side)");
        try {
            SecurityImpl.MyRequest request = SecurityImpl.MyRequest.newBuilder().setMessage("(Unary RPC " +
                    "Client said:Can you check if there is any window broken, please!)").build();
            SecurityImpl.MyReply reply = blockingStubService3.withDeadlineAfter(1, TimeUnit.SECONDS)
                    .intrusion(request);
            System.out.println("Client Received: " + reply.getMessage());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }

    public static void main(String[] args) throws Exception {
        //set a variable to record the server address
        String target;

        //create a jmDNSServiceDiscovery instance to ensurers the service would be discovered
        JmDnsServiceDiscovery jmDnsServiceDiscovery = new JmDnsServiceDiscovery();

        JmDnsServiceDiscovery.find("_gRPCserver._tcp.local.");

        do {
            target = jmDnsServiceDiscovery.getLocGrpc();
            System.out.println("jmDnsServiceDiscovery: " + target);
        } while (target.length() < 2);

        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();

        try {
            SecurityClient client = new SecurityClient(channel);
            client.clientSideStreamVideoFrames();
            client.clientSideLongStaying();
            client.clientSideAccessing();
            client.clientSideIntrusion();
        }finally {
            channel.shutdown().awaitTermination(30,TimeUnit.SECONDS);
        }

    }

}
