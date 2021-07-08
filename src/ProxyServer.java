import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

public class ProxyServer implements Runnable {

	private String websiteRequeseted = "http://www.bom.gov.au"; 
	private static final String newLine = System.getProperty("line.separator");
	private List ccities;
	private List cities;
	private List restrictedWords;
	Socket user;
	BufferedReader inbound;
	BufferedWriter outbound;

	public ProxyServer(Socket clientSocket, String sitename, ArrayList<String> cities, ArrayList<String> ccities,
			ArrayList<String> restrictedWords) throws Exception {
		this.user = clientSocket;
		this.ccities = ccities;
		this.cities = cities;
		this.restrictedWords = restrictedWords;
		websiteRequeseted = sitename;
		this.user.setSoTimeout(5000);
		inbound = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		outbound = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

	}

	@Override
	public void run() {
		String url;
		try {
			url = inbound.readLine();
			System.out.println("Point 4.b =" + url);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		String request = url.substring(0, url.indexOf(' '));
		String urlString = url.substring(url.indexOf(' ') + 1);
		urlString = websiteRequeseted + urlString + " ";
		urlString = urlString.substring(0, urlString.indexOf(' '));
		if (ProxyClient.isBlocked(urlString)) {
			try {
				FileUtil.blockedSiteRequested(user);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

		File file;
		if ((file = ProxyClient.getCachedPage(urlString)) != null) {
			System.out.println(" Page is cached, retrieving from cahce. Saving response time");
			notFirstTimeURLRequest(file);
		} else {
			firstTimeURLRequest(urlString);
		}

	}

	private void notFirstTimeURLRequest(File cachedFile) {

		try {
			System.out.println("Cached File=" + cachedFile.getName());
			String fileExtension = "";
			if (cachedFile.getName().indexOf('.') > 0)
				fileExtension = cachedFile.getName().substring(cachedFile.getName().lastIndexOf('.'));
			String response;
			if ((fileExtension.contains(".png")) || fileExtension.contains(".jpg") || fileExtension.contains(".jpeg")
					|| fileExtension.contains(".gif")) {
				BufferedImage image = ImageIO.read(cachedFile);
				if (image == null) {
					response = "HTTP/1.0 404 NOT FOUND \n" + "Proxy-agent: ProxyServer/1.0\n" + "\r\n";
					outbound.write(response);
					outbound.flush();
				} else {
					response = "HTTP/1.0 200 OK\n" + "Proxy-agent: ProxyServer/1.0\n" + "\r\n";
					outbound.write(response);
					outbound.flush();
					ImageIO.write(image, fileExtension.substring(1), user.getOutputStream());
				}
			}

			else {
				BufferedReader cachedFileBufferedReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(cachedFile)));

				response = "HTTP/1.0 200 OK\n" + "Proxy-agent: ProxyServer/1.0\n" + "\r\n";
				outbound.write(response);
				outbound.flush();

				String line;
				while ((line = cachedFileBufferedReader.readLine()) != null) {
					outbound.write(line);
				}
				outbound.flush();

				if (cachedFileBufferedReader != null) {
					cachedFileBufferedReader.close();
				}
			}
			if (outbound != null) {
				outbound.close();
			}

		} catch (IOException e) {
			System.out.println("Error Sending Cached file to client");

		}
	}

	private void firstTimeURLRequest(String urlString) {

		try {
			int fileExtensionIndex = urlString.lastIndexOf(".");
			String fileExtension;
			fileExtension = urlString.substring(fileExtensionIndex, urlString.length());
			String fileName = urlString.substring(0, fileExtensionIndex);
			fileName = fileName.substring(fileName.indexOf('.') + 1);
			fileName = fileName.replace("/", "__");
			fileName = fileName.replace('.', '_');
			if (fileExtension.contains("/")) {
				fileExtension = fileExtension.replace("/", "__");
				fileExtension = fileExtension.replace('.', '_');
				fileExtension += ".html";
			}
			fileName = fileName + fileExtension;

			boolean caching = true;
			File fileToCache = null;
			BufferedWriter fileToCacheBW = null;
			int qind = fileName.indexOf("?");
			if (qind > 10) {
				fileName = fileName.substring(0, qind);
			}
			fileToCache = new File("sitepages/" + fileName);
			if (!fileToCache.exists()) {
				fileToCache.createNewFile();
			}
			fileToCacheBW = new BufferedWriter(new FileWriter(fileToCache));

			if ((fileExtension.contains(".png")) || fileExtension.contains(".jpg") || fileExtension.contains(".jpeg")
					|| fileExtension.contains(".gif")) {

				URL remoteURL = new URL(urlString);
				HttpURLConnection con = (HttpURLConnection) remoteURL.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");
				int responseCode = con.getResponseCode();
				System.out.println("Point 4.C Response code=" + responseCode);
				InputStream in = con.getInputStream();
				OutputStream out = new FileOutputStream(fileToCache.getAbsoluteFile());
				try {
					byte[] bytes = new byte[2048];
					int length;
					while ((length = in.read(bytes)) != -1) {
						out.write(bytes, 0, length);
					}
				} finally {
					in.close();
					out.close();
					String line = "HTTP/1.0 200 OK\n" + "Proxy-agent: ProxyServer/1.0\n" + "\r\n";
					outbound.write(line);
					outbound.flush();
				}
			}
			// File is a text file
			else {
				boolean restrictedContent = false;
				int totalChanages = 0;
				URL remoteURL = new URL(urlString);
				HttpURLConnection proxyToServerCon = (HttpURLConnection) remoteURL.openConnection();
				proxyToServerCon.setConnectTimeout(120000);
				proxyToServerCon.setRequestProperty("User-Agent", "Test");
				proxyToServerCon.setRequestProperty("Connection", "close");
				proxyToServerCon.setConnectTimeout(1500);
				proxyToServerCon.setDoOutput(true);
				proxyToServerCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				System.out.println("Point 4.C Response code=" + proxyToServerCon.getResponseCode());
				StringBuilder sb = new StringBuilder();
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(proxyToServerCon.getInputStream()));) {
					String nextLine = "";

					while ((nextLine = reader.readLine()) != null) {

						Random random = new Random();

						for (int i = 0; i < ccities.size(); i++) {
							int rand = random.ints(0, ccities.size()).findFirst().getAsInt();
							String tot = nextLine.replaceAll((String) ccities.get(i), (String) cities.get(rand));
							if (tot.length() != nextLine.length()) {
								nextLine = tot;
								totalChanages++;
							}

						}

						for (int i = 0; i < restrictedWords.size(); i++) {
							String restrictedWord = (String) restrictedWords.get(i);
							if (nextLine.indexOf(restrictedWord) > 0) {
								restrictedContent = true;
								break;
							}
						}

						sb.append(nextLine + newLine);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				String line = "HTTP/1.0 200 OK\n" + "Proxy-agent: ProxyServer/1.0\n" + "\r\n";
				

				if (restrictedContent) {
					ProxyClient.block(urlString);
					  line = "HTTP/1.0 403 Access Forbidden \n" + "User-Agent: ProxyServer/1.0\n" + "\r\n";
					  outbound.write(line);
					//outbound.write("<html><h3>Site is blocked<h3></html>");
				} else {
					outbound.write(line);
					outbound.write(sb.toString());
				}
				System.out.println("  4.D total changes made = " + totalChanages);
				fileToCacheBW.write(sb.toString());

				sb.setLength(0);
				outbound.flush();

			}

			if (caching) {
				fileToCacheBW.flush();
				ProxyClient.addCachedPage(urlString, fileToCache);
			}
			if (fileToCacheBW != null)
				fileToCacheBW.close();
			if (outbound != null)
				outbound.close();

		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
