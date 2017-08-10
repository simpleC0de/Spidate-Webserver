package webserver.main;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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

    private static MySQL sql;

    public HTMLServer(MySQL sql) throws Exception {
        this.sql = sql;
    }

    public void startServer(){


        if(server != null){
            System.out.println("Server already running.");

            return;
        }
        try{
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new MyHandler());
            server.start();
        }catch(Exception ex){
            ex.printStackTrace();
        }


        System.out.println("Started Server.");

    }

    public void stopServer(){

        if(server != null){
            server.stop(1);
        }

        server = null;

        System.out.println("Server stopped.");


    }

    public void restartServer(){


        if(server != null){
            server.stop(1);
        }

        startServer();
    }


    public String requestVersion(String id){

        String responseString = "";
        try{
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
            try{
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }catch(Exception ex){
                in = new BufferedReader((new InputStreamReader(con.getErrorStream())));
            }

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            JsonParser jsonParser = new JsonParser();
            JsonObject jo = (JsonObject)jsonParser.parse(response.toString());
            String status = jo.get("status").getAsString(); // Used to get the status, example: 0 or 1
            if((Integer.parseInt(status) == 1)){
                String version = jo.get("version").getAsString(); // Used to get the version, only when returning status 1 (Success)
            }

            String message = jo.get("message").getAsString(); // Used to get the message, example: NO API CALL

            responseString = response.toString();

            /*
            Thanks for using our Service.
            This was and will be a spare time open-source project.
            We're aiming to improve our skills at every corner of programming.
            If you want a reliable way of getting your plugin version, try https://spiget.org
            */
            return responseString;

        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }


    }




    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        try{


            StatusResponse response = new StatusResponse();
            Gson gson = new Gson();

            String dateTime;

            String responseString;

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            dateTime = sdf.format(cal.getTime());


            if(t.getRequestHeaders().get("User-Agent") == null || !t.getRequestHeaders().get("User-Agent").toString().equalsIgnoreCase("[SPIDATE-USER]")){
                response.setMessage("WRONG USERAGENT");
                response.setStatus(0);
                response.setVersion(null);

                responseString = gson.toJson(response);

                t.sendResponseHeaders(404, responseString.length());
                OutputStream os = t.getResponseBody();
                os.write(responseString.getBytes());
                os.close();
                System.out.println("[" +dateTime+ "] Wrong User-Agent Call");
                return;
            }

            if(!t.getRequestURI().toString().contains("api")){
                response.setMessage("NO API CALL");
                response.setStatus(0);
                response.setVersion(null);

                responseString = gson.toJson(response);

                t.sendResponseHeaders(503, responseString.length());
                OutputStream os = t.getResponseBody();
                os.write(responseString.getBytes());
                os.close();
                System.out.println("[" +dateTime+ "] No API Call");
                return;
            }

            System.out.println( "[" +dateTime+ "] New Connection from - " + t.getRemoteAddress() + "\n");

            sql.updateConnections();

            String[] requestUri = t.getRequestURI().toString().split("=");

            String pluginId = requestUri[1];
            pluginId = pluginId.replace("?", "");

            String version = sql.getVersion(pluginId);


            if(version.equalsIgnoreCase("") || version.isEmpty()){
                response.setVersion(null);
                response.setStatus(0);
                response.setMessage("ID NOT FOUND");
                responseString = gson.toJson(response);
            }else{
                response.setStatus(1);
                response.setVersion(version);
                response.setMessage("SUCCESS");
                responseString = gson.toJson(response);
            }


            t.sendResponseHeaders(200, responseString.length());
            OutputStream os = t.getResponseBody();
            os.write(responseString.getBytes());
            os.close();
        }catch(Exception ex){
             ex.printStackTrace();
        }


        }
    }

}
