import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

public class TestRemoteUrl {
	 private static final String newLine  = System.getProperty("line.separator");
	public static void main( String[] args )
    { 
        String server = "http://www.bom.gov.au"; 
        String path = "/index.php"; 

        System.out.println( "Loading contents of URL: " + server );
        
        try{
        	
         
                 HttpURLConnection urlc = (HttpURLConnection) (new URL(server).openConnection());
                 
 
                 
                 urlc.setRequestProperty("User-Agent", "Test");
                 urlc.setRequestProperty("Connection", "close");
                 urlc.setConnectTimeout(1500);
                 urlc.setDoOutput(true);
                 urlc.connect();
               System.out.println(readStream(urlc.getInputStream()));
                 
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
	
	private static String readStream(InputStream in) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
            String nextLine = "";
            while ((nextLine = reader.readLine()) != null) {
                sb.append(nextLine + newLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
 
