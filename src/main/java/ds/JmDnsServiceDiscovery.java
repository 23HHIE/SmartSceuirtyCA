package ds;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class JmDnsServiceDiscovery {
    private static ServiceInfo serviceInfo = null;
    private static String locGrpc = "";
    private static JmDNS jmdns = null;

    //create a listener to implement the ServiceLister interface in order to listen for changes in the service
    private static class SecurityListener implements ServiceListener{

        //override the serviceAdded method to ensures that the added service's information can be fetched
        @Override
        public void serviceAdded(ServiceEvent event) {
            serviceInfo = event.getInfo();
            locGrpc = serviceInfo.getName().split("_",1)[0];
            System.out.println("Service added: " + serviceInfo);
        }

        //override the serviceRemoved method to ensures that the removed service's information can be fetched
        @Override
        public void serviceRemoved(ServiceEvent event) {
            serviceInfo = event.getInfo();
            locGrpc = serviceInfo.getName().split("_",1)[0];
            System.out.println("Service removed: " + serviceInfo);
        }

        //override the serviceResolved method to ensures that the removed service's information can be fetched
        @Override
        public void serviceResolved(ServiceEvent event) {
            serviceInfo = event.getInfo();
            locGrpc = serviceInfo.getName().split("_",1)[0];
            System.out.println("Service resolved: " + serviceInfo);
        }
    }

    //create a method
    public static void find(String service) throws InterruptedException{
        try {
            // create a JmDNS instance
            jmdns = JmDNS.create(InetAddress.getLocalHost());

            // add the listener above to be the listener of the service
            jmdns.addServiceListener(service,new SecurityListener());

            System.out.println("Listening services");

            // use the printStackTrace method to print and trace exceptions
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public String getLocGrpc() {
        return locGrpc;
    }
}
