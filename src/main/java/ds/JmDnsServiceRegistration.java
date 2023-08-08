package ds;
import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
public class JmDnsServiceRegistration {
    private static JmDNS jmdns = null;

    //register method
    public static void register(String service, int grpcPort) throws InterruptedException{
        try{
            //create a jmDNS instance for service registration and deregistration
            jmdns = JmDNS.create(InetAddress.getLocalHost());

            //set a server location by combining the local IP address and the service port
            String locGrpc = InetAddress.getLocalHost().getHostAddress() + ":" + grpcPort;

            //register a service on default multicast DNS port 5353
            //create a ServiceInfo instance
            ServiceInfo serviceInfo = ServiceInfo.create(service,locGrpc,5353,"location of gRPC service");

            //call the register method to register the service information into the jmDNS instance
            jmdns.registerService(serviceInfo);
            System.out.println("Service " + service + " Registered");

        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    //deregister method use to close the jmDNS instance
    public void stop() throws InterruptedException, IOException{
        jmdns.unregisterAllServices();
        jmdns.close();
    }
}
