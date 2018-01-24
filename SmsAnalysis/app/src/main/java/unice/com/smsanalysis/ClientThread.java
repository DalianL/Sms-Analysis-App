package unice.com.smsanalysis;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

//import static unice.com.smsanalysis.MainActivity.SERVER_IP;

/**
 * Created by Matthieu on 29/10/2017.
 */

public class ClientThread implements Runnable {

   @Override
    public void run() {
       try {
           InetAddress serverAddr = InetAddress.getByName("");
       } catch(UnknownHostException e1) {
           e1.printStackTrace();
       } catch (IOException e1) {
           e1.printStackTrace();
       }
   }

}
