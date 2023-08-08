package ds;
import com.google.protobuf.ByteString;
import io.grpc.BindableService;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class SecurityServer{
    //create a variable named logger to record and output log information during communication
    private static final Logger logger = Logger.getLogger(SecurityServer.class.getName());

    //create a variable to set the data
    private byte[] bytesDataFromSource;

    //create a static random numbers generator
    static Random random = new Random();

    //create a private variable as an instance of gRPC server
    private Server server;

    //to start the server
    private void start() throws IOException, InterruptedException{

        //create a server instance(setting port to 0 ensures an available port to avoiding ports conflict from multiple servers)
        server = Grpc.newServerBuilderForPort(0, InsecureServerCredentials.create())
                .addService((BindableService) new MyService1Impl())
                .addService((BindableService) new MyService2Impl())
                .addService((BindableService) new MyService3Impl())
                .build()
                .start();

        //register a jmDNS with the address and the port
        JmDnsServiceRegistration.register("_gRPCserver._tcp.local.", server.getPort());

        //use logger to record the server side has been started successfully and the port to observe the running status
        logger.info("Server started, listening on " + server.getPort());

        //create a shutdown hook thread to ensure that necessary resources release would be finished when the server end
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try{
                    SecurityServer.this.stop();
                }catch (InterruptedException e){
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });

    }

    private void stop() throws InterruptedException{
        if(server!=null){
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    //a method to block the current thread
    private void blockUntilShutDown()throws InterruptedException{
        if(server!=null){
            server.awaitTermination();
        }
    }

    //launch the server
    public static void main(String[] args)throws IOException,InterruptedException {
        final SecurityServer server = new SecurityServer();
        server.start();
        server.blockUntilShutDown();
    }

    static class MyService1Impl extends MonitorGrpc.MonitorImplBase {
        public void streamVideoFrames(SecurityImpl.MyRequest request, StreamObserver<SecurityImpl.MyReply> responseObserver) {
            //日志记录gRPC通信的开始
            logger.info("Calling gRPC streaming type(from the server side)");
            byte[] byteData = generateRandomBytes();

            //用于处理客户端发的消息
            for (int i = 0; i < 10; i++) {
                SecurityImpl.MyReply reply = SecurityImpl.MyReply.newBuilder().setFrameData(ByteString.copyFrom(byteData)).build();
                responseObserver.onNext(reply);
            }

            //服务器已经完成了消息的回复，并调用responseObserver的onCompleted方法通知客户端通信结束
            responseObserver.onCompleted();
        }
        @Override
        public StreamObserver<SecurityImpl.MyRequest> longStaying(StreamObserver<SecurityImpl.MyReply> responseObserver) {
            logger.info("calling gRPC bi-directional streaming type(from the server side)");
            // MonitorImpl.MyReply reply = MonitorImpl.MyReply.newBuilder().setMessage()
            return new StreamObserver<SecurityImpl.MyRequest>() {

                //处理客户端发送的请求消息
                @Override
                public void onNext(SecurityImpl.MyRequest value) {
                    System.out.println("Server received: " + value.getMessage());
                }

                //error handling错误处理
                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                //响应客户端，并标志着双向流式结束
                @Override
                public void onCompleted() {
                    SecurityImpl.MyReply reply = SecurityImpl.MyReply.newBuilder().setMessage("Your property is safe,\n(Stream completed)").build();
                    responseObserver.onNext(reply);
                    responseObserver.onCompleted();
                }
            };
        }

    }
    static class MyService2Impl extends AccessGrpc.AccessImplBase{
        public StreamObserver<SecurityImpl.MyRequest> accessing(StreamObserver<SecurityImpl.MyReply> responseObserver){
            logger.info("Calling gRPC client streaming type (from the server side)");
            return new StreamObserver<SecurityImpl.MyRequest>() {
                @Override
                public void onNext(SecurityImpl.MyRequest value) {
                    System.out.println("Server received: " + value.getMessage());
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    SecurityImpl.MyReply reply = SecurityImpl.MyReply.newBuilder()
                            .setMessage("(Streaming completed)").build();
                    responseObserver.onNext(reply);
                    responseObserver.onCompleted();
                }
            };

        }
    }
    static class MyService3Impl extends IntrusionGrpc.IntrusionImplBase{
        public void intrusion(SecurityImpl.MyRequest request, StreamObserver<SecurityImpl.MyReply> responseObserver){
            logger.info("Calling gRPC unary type (from the sever side)");
            Random rd = new Random();
            int isBroken = rd.nextInt(2);
            SecurityImpl.MyReply reply;
            if (isBroken==0){
                reply = SecurityImpl.MyReply.newBuilder().setMessage(request.getMessage() +
                        "(Unary RPC Server said: All is well, no broken window.)").build();
            }else {
                reply = SecurityImpl.MyReply.newBuilder().setMessage(request.getMessage() +
                        "(Unary RPC Server said: Alert!Please be aware of that your window has been broken!)").build();
            }
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    private static byte[] generateRandomBytes() {
        // 生成随机的字节数组，用于模拟实时视频帧数据
        byte[] byteData = new byte[1024]; // 假设视频帧大小为 1024 字节
        Random random = new Random();
        random.nextBytes(byteData);
        return byteData;
    }
    public byte[] getBytesDataFromSource() {
        return bytesDataFromSource;
    }

    public void setBytesDataFromSource(byte[] bytesDataFromSource) {
        this.bytesDataFromSource = bytesDataFromSource;
    }
}
