package webserver.main;

import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 02.08.2017.
 */
public class Main {



    private static HashMap<String, List<String>> resources = new HashMap<>();

    private static HTMLServer server;


    private static MySQL sql;

    public static void main(String[] args) {
        sql = new MySQL();

        try{
            server = new HTMLServer(sql);
        }catch(Exception ex){

        }

        server.startServer();

    }




}
