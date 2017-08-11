package webserver.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by root on 05.08.2017.
 */
public class HTMLServer {

	private HttpServer server;

	private final MySQL sql;

	public HTMLServer(MySQL sql) throws Exception {
		this.sql = sql;
	}

	public void startServer() {
		if (server != null) {
			System.out.println("Server already running.");
			return;
		}
		
		try {
			server = HttpServer.create(new InetSocketAddress(8080), 0);
			server.createContext("/", new MyHandler());
			server.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("Started Server.");
	}

	public void stopServer() {
		if (server != null) {
			server.stop(1);
		}

		server = null;
		
		System.out.println("Server stopped.");
	}

	public void restartServer() {
		if (server != null) {
			server.stop(1);
		}
		startServer();
	}

	@SuppressWarnings("unused")
	public String requestVersion(String id) {

		String responseString = "";
		try {
			String url = "http://localhost:8080/?=" + id;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestProperty("User-Agent", "SPIDATE-USER");
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.flush();
			wr.close();

			BufferedReader in;
			try {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} catch (Exception ex) {
				in = new BufferedReader((new InputStreamReader(con.getErrorStream())));
			}

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			in.close();

			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject) jsonParser.parse(response.toString());
			String status = jo.get("status").getAsString(); // Used to get the status, example: 0 or 1
			if ((Integer.parseInt(status) == 1)) {
				String version = jo.get("version").getAsString(); // Used to get the  version, only when returning status 1 (Success)
			}

			String message = jo.get("message").getAsString(); // Used to get the message, example: NO API CALL

			responseString = response.toString();

			/*
			 * Thanks for using our Service. This was and will be a spare time
			 * open-source project. We're aiming to improve our skills at every
			 * corner of programming. If you want a reliable way of getting your
			 * plugin version, try https://spiget.org
			 */
			return responseString;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

    class MyHandler implements HttpHandler {
    	
    	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    	private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    	private final Calendar calendar = Calendar.getInstance();
    	
    	private String getTime() {
    		return format.format(calendar.getTime());
    	}
    	
        @Override
        public void handle(HttpExchange exchange) throws IOException {
        	try {
				String dateTime = getTime();

				if (exchange.getRequestHeaders().get("User-Agent") == null || !exchange.getRequestHeaders().get("User-Agent").toString().equalsIgnoreCase("[SPIDATE-USER]")) {
					
					sendResponse(new StatusResponse(0, "WRONG USERAGENT", null), exchange, 400); //Bad Request
					
					System.out.println("[" + dateTime + "] Wrong User-Agent Call");
					return;
				}

				if (!exchange.getRequestURI().toString().contains("api")) {
					
					sendResponse(new StatusResponse(0, "NO API CALL", null), exchange, 403); //Forbidden
					
					System.out.println("[" + dateTime + "] No API Call");
					return;
				}

				System.out.println("[" + dateTime + "] New Connection from - " + exchange.getRemoteAddress() + "\n");

				sql.updateConnections();

				String[] requestUri = exchange.getRequestURI().toString().split("=");
				
				if(requestUri.length < 2) {
					
					sendResponse(new StatusResponse(0, "INVALID ARGUMENTS", null), exchange, 400); //400 Bad Request
					
					System.out.println("["+getTime()+"] Invalid Args");
					return;
				}

				String pluginId = requestUri[1];
				pluginId = pluginId.replaceAll("?", "");

				String version = sql.getVersion(pluginId);

				if (version.equalsIgnoreCase("") || version.isEmpty()) {
					
					sendResponse(new StatusResponse(0, "ID NOT FOUND", null), exchange, 404); //404 Not Found
					
				} else {
					sendResponse(new StatusResponse(1, "SUCCESS", version), exchange, 200); //200 Ok
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
        
        private void sendResponse(StatusResponse response, HttpExchange exchange, int httpstatus) {
			String responseString = gson.toJson(response);
			try {
				exchange.sendResponseHeaders(httpstatus, responseString.length());
				OutputStream os = exchange.getResponseBody();
				os.write(responseString.getBytes());
				os.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
        }
	}

}
