package webserver.main;

import java.sql.*;

/**
 * Created by root on 04.08.2017.
 */
public class MySQL {

	private Connection conn;
	private String hostname;
	private String user;
	private String password;
	private String database;
	private int port;

	public MySQL() {
		hostname = "";
		port = 3306;
		database = "";
		user = "";
		password = "";
		openConnection();

		for (int i = 1; i < 41; i++) {
			queryUpdate("CREATE TABLE IF NOT EXISTS resource_" + i + " (LINK varchar(255), AUTHOR varchar(255), VERSION varchar(255), NAME varchar(255))");
		}

		queryUpdate("CREATE TABLE IF NOT EXISTS allResources (LINK varchar(255), AUTHOR varchar(255), VERSION varchar(255), ID varchar(32));");
		queryUpdate("CREATE TABLE IF NOT EXISTS allConns (CONNECTIONS long);");

		queryUpdate("CREATE TABLE IF NOT EXISTS allMembers (ID varchar(255), LINK varchar(255), NAME varchar(255))");

		long outCome = getConnections();

		if (outCome < 0) {
			queryUpdate("INSERT INTO allConns (CONNECTIONS) VALUES (0);");
		}
	}

	public Connection openConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager
					.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?user=" + user + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8");
			conn = con;

			return conn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return conn;
	}

	public long getConnections() {
		try {
			if (!getConnection().isValid(2000)) {
				openConnection();
			}

			Connection conn = getConnection();
			PreparedStatement st = conn.prepareStatement("SELECT CONNECTIONS FROM allConns");
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				return rs.getLong("CONNECTIONS"); //TODO Potential resource leak: 'conn' may not be closed at this location
			}

			return -1;
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}

	public void updateConnections() {
		try {
			if (!getConnection().isValid(2000)) {
				openConnection();
			}

			long start = getConnections();
			if (start < 0) {
				return;
			}

			queryUpdate("UPDATE allConns SET CONNECTIONS = CONNECTIONS + 1;");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Connection getConnection() {
		return conn;
	}

	public boolean hasConnection() {
		try {
			return conn != null || conn.isValid(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public void closeRessources(ResultSet rs, PreparedStatement st) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {

			}
		}
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {

			}
		}
	}

	public void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			conn = null;
		}
	}

	public Thread queryUpdate(final String query) {
		Thread thread = new Thread() {

			@Override
			public void run() {
				try {
					if (!getConnection().isValid(2000)) {
						openConnection();
					}
					PreparedStatement st = null;
					Connection conn = getConnection(); // TODO Potential resource leak: 'conn' may not be closed
					try {
						st = conn.prepareStatement(query);
						st.executeUpdate();
					} catch (SQLException e) {
						System.err.println("Failed to send update '" + query + "'.");
						e.printStackTrace();
					} finally {
						closeRessources(null, st);
						// Replacement for Deprecated Method stop. It will signal the Thread to stop, if it took longer than 50ms it will force it to stop.
						this.join(50);
					}
				} catch (SQLException | InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		};

		thread.start();
		return thread;
	}

	public String getVersion(String pluginId) {
		try {
			String respond = "";

			if (!getConnection().isValid(2000)) {
				openConnection();
			}

			Connection conn = getConnection();
			PreparedStatement st = conn.prepareStatement("SELECT VERSION FROM allResources WHERE ID = '" + pluginId + "';");
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				respond = rs.getString("VERSION");
			}

			return respond; //TODO Potential resource leak: 'st' may not be closed at this location
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

}
