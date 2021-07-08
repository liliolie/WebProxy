import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;

public class FileUtil {

	public static HashMap<String, File> loadCachedSites() throws IOException, ClassNotFoundException {
	 
		HashMap<String, File> cache =null;
		File cachedSites = new File("cachedSites.txt");
		if(!cachedSites.exists()){
			System.out.println("No cached sites found - creating new file");
			cachedSites.createNewFile();
		} else {
			FileInputStream fileInputStream = new FileInputStream(cachedSites);
			 
			if(fileInputStream.available()<5) {
				System.out.println("No cached sites found - in cachedSites.text");
			}else {
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream); 
				 cache = (HashMap<String,File>)objectInputStream.readObject();
				objectInputStream.close();
			}
			fileInputStream.close();
			
		}
		return cache;
	}

	public static HashMap<String, String> loadBlockedSites() throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		// Load in blocked sites from file
		HashMap<String, String> blockedSites =null;
		File blockedSitesTxtFile = new File("blockedSites.txt");
		if(!blockedSitesTxtFile.exists()){
			System.out.println("No blocked sites found - creating new file");
			blockedSitesTxtFile.createNewFile();
		} else {
			FileInputStream fileInputStream = new FileInputStream(blockedSitesTxtFile);
			if(fileInputStream.available()<5) {
				System.out.println("No cached sites found - in blockedSites.text");
			}else {
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				 blockedSites = (HashMap<String, String>)objectInputStream.readObject();
				objectInputStream.close();
			}
			fileInputStream.close();
			
		}
		return blockedSites;
	}

	public static void addCachedPage(String urlString, File fileToCache) {
	 
		
	}
	
	/*
	 * private void sendNonCachedToClient(String urlString) {
	 * 
	 * try { int fileExtensionIndex = urlString.lastIndexOf("."); String
	 * fileExtension; fileExtension = urlString.substring(fileExtensionIndex,
	 * urlString.length()); String fileName = urlString.substring(0,
	 * fileExtensionIndex); fileName = fileName.substring(fileName.indexOf('.') +
	 * 1); fileName = fileName.replace("/", "__"); fileName = fileName.replace('.',
	 * '_'); if (fileExtension.contains("/")) { fileExtension =
	 * fileExtension.replace("/", "__"); fileExtension = fileExtension.replace('.',
	 * '_'); fileExtension += ".html"; } fileName = fileName + fileExtension;
	 * 
	 * boolean caching = true; File fileToCache = null; BufferedWriter fileToCacheBW
	 * = null; int qind = fileName.indexOf("?"); if (qind > 10) { fileName =
	 * fileName.substring(0, qind); } fileToCache = new File("sitepages/" +
	 * fileName); if (!fileToCache.exists()) { fileToCache.createNewFile(); }
	 * fileToCacheBW = new BufferedWriter(new FileWriter(fileToCache));
	 * 
	 * if ((fileExtension.contains(".png")) || fileExtension.contains(".jpg") ||
	 * fileExtension.contains(".jpeg") || fileExtension.contains(".gif")) {
	 * 
	 * URL remoteURL = new URL(urlString); HttpURLConnection con =
	 * (HttpURLConnection) remoteURL.openConnection(); con.setRequestMethod("GET");
	 * con.setRequestProperty("User-Agent", "Mozilla/5.0"); int responseCode =
	 * con.getResponseCode(); System.out.println("Point 4.C Response code=" +
	 * responseCode); InputStream in = con.getInputStream(); OutputStream out = new
	 * FileOutputStream(fileToCache.getAbsoluteFile()); try { byte[] bytes = new
	 * byte[2048]; int length; while ((length = in.read(bytes)) != -1) {
	 * out.write(bytes, 0, length); } } finally { in.close(); out.close(); String
	 * line = "HTTP/1.0 200 OK\n" + "Proxy-agent: ProxyServer/1.0\n" + "\r\n";
	 * outbound.write(line); outbound.flush(); } } // File is a text file else {
	 * boolean restrictedContent = false; int totalChanages = 0; URL remoteURL = new
	 * URL(urlString); HttpURLConnection proxyToServerCon = (HttpURLConnection)
	 * remoteURL.openConnection(); proxyToServerCon.setConnectTimeout(120000);
	 * proxyToServerCon.setRequestProperty("User-Agent", "Test");
	 * proxyToServerCon.setRequestProperty("Connection", "close");
	 * proxyToServerCon.setConnectTimeout(1500); proxyToServerCon.setDoOutput(true);
	 * proxyToServerCon.setRequestProperty("Content-Type",
	 * "application/x-www-form-urlencoded");
	 * System.out.println("Point 4.C Response code=" +
	 * proxyToServerCon.getResponseCode()); StringBuilder sb = new StringBuilder();
	 * try (BufferedReader reader = new BufferedReader( new
	 * InputStreamReader(proxyToServerCon.getInputStream()));) { String nextLine =
	 * "";
	 * 
	 * while ((nextLine = reader.readLine()) != null) {
	 * 
	 * Random random = new Random();
	 * 
	 * for (int i = 0; i < ccities.size(); i++) { int rand = random.ints(0,
	 * ccities.size()).findFirst().getAsInt(); String tot =
	 * nextLine.replaceAll((String) ccities.get(i), (String) cities.get(rand)); if
	 * (tot.length() != nextLine.length()) { nextLine = tot; totalChanages++; }
	 * 
	 * }
	 * 
	 * for (int i = 0; i < restrictedWords.size(); i++) { String restrictedWord =
	 * (String) restrictedWords.get(i); if (nextLine.indexOf(restrictedWord) > 0) {
	 * restrictedContent = true; break; } }
	 * 
	 * sb.append(nextLine + newLine); } } catch (IOException e) {
	 * e.printStackTrace(); } String line = "HTTP/1.0 200 OK\n" +
	 * "Proxy-agent: ProxyServer/1.0\n" + "\r\n"; outbound.write(line);
	 * 
	 * if (restrictedContent) { NginxJr.block(urlString);
	 * outbound.write("<html><h3>Site is blocked<h3></html>"); } else
	 * outbound.write(sb.toString());
	 * System.out.println("  4.D total changes made = " + totalChanages);
	 * fileToCacheBW.write(sb.toString());
	 * 
	 * sb.setLength(0); outbound.flush();
	 * 
	 * }
	 * 
	 * if (caching) { fileToCacheBW.flush(); NginxJr.addCachedPage(urlString,
	 * fileToCache); } if (fileToCacheBW != null) fileToCacheBW.close(); if
	 * (outbound != null) outbound.close();
	 * 
	 * }
	 * 
	 * catch (Exception e) { e.printStackTrace(); } }
	 */
	public static void blockedSiteRequested(Socket user) throws IOException { 
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(user.getOutputStream()));
		String line = "HTTP/1.0 403 Access Forbidden \n" + "User-Agent: ProxyServer/1.0\n" + "\r\n";
		bufferedWriter.write(line);
		bufferedWriter.flush();
 
}

}
