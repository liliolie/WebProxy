import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

public class ProxyClient {

	private static int socketConnectionOpened = 0;
	static Properties systemProperties = new Properties();
	static String sitename="";
	private ServerSocket serverSocket;  
	static HashMap<String, File> conentCache; 
	static HashMap<String, String> blockedSites; 
	static ArrayList<Thread> servicingThreads;
	static ArrayList<String> cities = new ArrayList();
	static ArrayList<String> ccities = new ArrayList();
	static ArrayList<String> restrictedWords = new ArrayList();
	
	public static void main(String[] args) throws Exception {
		InputStream input = new FileInputStream("website.properties");
		systemProperties.load(input);
		input = new FileInputStream("cities.txt");
		Scanner sc=new Scanner(input); 
		while(sc.hasNextLine())  
		{  
			String city=sc.nextLine();
			if(city!=null)
			cities.add(city.trim());   
		}  
		input = new FileInputStream("capitalcities");
		sc=new Scanner(input); 
		while(sc.hasNextLine())  
		{  
			String ccity=sc.nextLine();
			if(ccity!=null)
			ccities.add(ccity.trim());   
		}  
		input = new FileInputStream("restrictedWords.txt");
		sc=new Scanner(input); 
		while(sc.hasNextLine())  
		{  
			String restrictedWord=sc.nextLine();
			if(restrictedWord!=null)
				restrictedWords.add(restrictedWord.trim());   
		}  
		
		System.out.println("Total Capital Cities Loaded = "+ccities.size());
		System.out.println("Total Cities Loaded = "+cities.size());
		ProxyClient proxy = new ProxyClient(Integer.parseInt( systemProperties.get("server.port")+""));
		 sitename=(String) systemProperties.get("default.website");
		 proxy.handleRequests();
		System.out.println(" ## server listening on port=" + systemProperties.get("server.port"));
	}

	public ProxyClient(int port) throws Exception {
		conentCache = new HashMap<>();
		blockedSites = new HashMap<>();
		servicingThreads = new ArrayList<>();  
		HashMap<String, File> result = FileUtil.loadCachedSites();
		if (result != null)
			conentCache = result;
		HashMap<String, String> result1 = FileUtil.loadBlockedSites();
		if (result1 != null)
			blockedSites = result1;
		System.out.println(" BlockedSites returned="+blockedSites);
		serverSocket = new ServerSocket(port);

		System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "..");
	 

	}

 
	public void handleRequests() throws Exception {

		while (true) { 
				Socket socket = serverSocket.accept();
				socketConnectionOpened++; 
				System.out.println(" New Request received on"+(new Date())+". Total client requests/connections so far = " + socketConnectionOpened);
				Thread thread = new Thread(new ProxyServer(socket,sitename,cities,ccities,restrictedWords)); 
				servicingThreads.add(thread); 
				thread.start(); 
		}
	}

   
	public static File getCachedPage(String url) {
		return conentCache.get(url);
	}

	 
	public static void addCachedPage(String urlString, File fileToCache) throws IOException {
		conentCache.put(urlString, fileToCache); 
		FileOutputStream fileStream; 
			fileStream = new FileOutputStream("cachedSites.txt");
			ObjectOutputStream objStream = new ObjectOutputStream(fileStream);
			objStream.writeObject(conentCache);
		 
	
	}

	 
	public static boolean isBlocked(String url) {
		System.out.println("$$ cHECK IF BLOCKED="+blockedSites);
		HashMap<String, String> result1;
		try {
			if(blockedSites==null) {
			result1 = FileUtil.loadBlockedSites();
			if (result1 != null)
				blockedSites = result1;
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (blockedSites.get(url) != null) {
			return true;
		} else {
			return false;
		}
	}

	public static void block(String urlString) throws  Exception {
		// TODO Auto-generated method stub
		blockedSites.put(urlString, urlString);
		FileOutputStream fileStream; 
		fileStream = new FileOutputStream("blockedSites.txt");
		ObjectOutputStream objStream = new ObjectOutputStream(fileStream);
		objStream.writeObject(blockedSites); 
	}
 

}
