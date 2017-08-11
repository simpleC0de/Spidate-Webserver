package webserver.main;

/**
 * Created by root on 02.08.2017.
 */
public class Main {

	private static HTMLServer server;
	private static MySQL sql;

	public static void main(String[] args) {
		sql = new MySQL();

		server = new HTMLServer(sql);

		server.startServer();
	}

}
