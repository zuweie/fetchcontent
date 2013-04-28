package oz.fetchcontent.datax;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import oz.fetchcontent.main.Log;
import oz.fetchcontent.main.config;
import oz.fetchcontent.main.rs;

public class Datax {
	
	public static Datax getInstance (){
		if (Datax.instance == null){
			Datax.instance = new Datax();
		}
		return Datax.instance;
	}
	public Datax(){
		connpool = new ArrayList<Connection>();
	}
	public Connection getConnector() throws SQLException{
		Connection conn = null;
		
		synchronized(connpool){
			if (!connpool.isEmpty()){
				return connpool.remove(0);
			}
		}
		try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = java.sql.DriverManager.getConnection(config.MYSQL_CONNURL, config.MYSQL_USER, config.MYSQL_PWD);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				Log.e(e.getMessage(), rs.EXCEPTIONCODE);
				throw new SQLException(e);
			}
		
		return conn;
	}
	
	public int updateDatatoDB(String sql) throws SQLException{
		Connection conn = null;
		Statement  stmt = null;
		int ret = -1;
		try {
			conn = getConnector();
			stmt = conn.createStatement();
			stmt.execute(sql);
			revertConnector(conn);
			return ret;
		} catch (SQLException e) {
			if (stmt != null){
				stmt.close();
				stmt = null;
			}
			if (conn != null)
				revertConnector(conn);
			
			throw new SQLException(e);
		}
	}
	
	public List<List<kv> > queryDataviaDB(String sql, Datafetcher df) throws SQLException{
		Connection conn = null;
		Statement  stmt = null;
		ResultSet  rest = null;
		List<List<kv> >   kvz  = null;
		try {
			conn = getConnector();
			stmt = conn.createStatement();
			rest = stmt.executeQuery(sql);
			if (rest.next()){
				kvz = new ArrayList<List<kv> >();
				do{
					df.execute(rest, kvz);
				}while(rest.next());
			}
			
			rest.close();
			stmt.close();
			revertConnector(conn);
			return kvz;
			
		} catch (SQLException e) {
			if (rest != null)
				rest.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				revertConnector(conn);
			throw new SQLException(e);
		}
	}
	
	public int checkRecExist(String sql, boolean checkexist) throws SQLException{
		Connection conn = null;
		Statement  stmt = null;
		ResultSet  rest = null;
		
		try {
			conn = getConnector();
			stmt = conn.createStatement();
			rest = stmt.executeQuery(sql);
			int count = 0;
			if (checkexist){
				if (rest.next())
					count = 1;
			}else{
				while(rest.next()){
					count++;
				}
			}
			
			rest.close();
			stmt.close();
			revertConnector(conn);
			return count;
			
		} catch (SQLException e) {
			if (rest != null)
				rest.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				revertConnector(conn);
			throw new SQLException(e);
		}
	}
	
	public void revertConnector(Connection conn){
		synchronized(connpool){
			connpool.add(conn);
		}
	}
	
	public void destoryConnector(){
		// when the website close invoke the function;
		synchronized(connpool){
			for (int i=0; i<connpool.size(); ++i){
				Connection conn = connpool.get(i);
				try {
					conn.close();
				} catch (SQLException e) {
					Log.e(e.getMessage(), rs.EXCEPTIONCODE);
				}
			}
			connpool.clear();
		}
	}
	
	List<Connection> connpool = null;
	static Datax instance = null;
}
