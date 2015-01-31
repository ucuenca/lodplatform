package com.ucuenca.misctools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.h2.jdbcx.JdbcConnectionPool;

/**
 * Singleton for creating/manipulating an H2SQLDB embedded instance
 * @author santteegt
 *
 */
public class DatabaseLoader {
	
	private static JdbcConnectionPool cp;
	private static Connection conn;
	
	/**
	 * Instantiation using Singleton DP. If no instance, creates a new H2DB Connection Pool
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection()throws Exception {
		if(conn == null) {
			cp = JdbcConnectionPool.create("jdbc:h2:~/dblod", "sa", "sa");
			conn = cp.getConnection();
			System.out.println("Connection established");
		}
		return conn;
		
	}
	
	/**
	 * Close the Embedded H2DB Connection Pool
	 * @throws Exception
	 */
	public static void closeConnection()throws Exception {
		conn.close();
		conn = null;
		cp.dispose();
		System.out.println("Connection closed");
	}
	
	/**
	 * Executes a DML sentence
	 * @param sql SQL sentence (INSERT; UPDATE, DELETE)
	 * @param values SQL parameter values
	 * @return Boolean.TRUE if sql execution has succeeded
	 * @throws Exception
	 */
	public static Boolean executeUpdate(String sql, Object... values)throws Exception {
		if(conn == null) throw new SQLException("SQL CONNECTION NOT FOUND");
		PreparedStatement st = conn.prepareStatement(sql);
		if(values != null && values.length > 0) {
			for(int i=0;i<values.length;i++) {
				if(values[i] instanceof String) {
					st.setString(i+1, (String)values[i]);
					continue;
				}
				if(values[i] instanceof Integer) {
					st.setInt(i+1, (Integer)values[i]);
					continue;
				}
			}
		}
		return st.execute();

	}
	
	/**
	 * Executes a SQL Query
	 * @param sql SQL query sentence
	 * @return ResultSet
	 * @throws Exception
	 */
	public static ResultSet executeQuery(String sql)throws Exception{
		if(conn == null) throw new SQLException("SQL CONNECTION NOT FOUND");
		return conn.createStatement().executeQuery(sql);
	}
	
	/**
	 * Creates a new Table schema
	 * @param tableName Table name
	 * @param fields List of field names with its associated constraints
	 * @throws Exception
	 */
	public static void createTable(String tableName, Map<String, String> fields)throws Exception {
		if(conn == null) throw new SQLException("SQL CONNECTION NOT FOUND");
		String sql = tableName != null && tableName.length() > 0 ? 
				"CREATE TABLE IF NOT EXISTS " + tableName + " (":null;
		if(sql != null) {
			for(String fieldID: fields.keySet()){
				String constraint = fields.get(fieldID);
				sql += fieldID + " " + constraint + ",";
			}
			sql = sql.substring(0, sql.length() -1) + ")";
		}
		conn.createStatement().execute(sql);
	}
	
	/**
	 * Just for test purposes
	 * @param args
	 */
	public static void main(String []args) {
		try {
			DatabaseLoader.getConnection();
			Map<String, String> fields = new HashMap<String, String>();
			fields.put("ID", "VARCHAR(100) PRIMARY KEY");
			fields.put("NAME", "VARCHAR(100)");
			fields.put("DESCRIPTION", "VARCHAR(100)");
			DatabaseLoader.createTable("TEST", fields);
			DatabaseLoader.executeUpdate("DROP TABLE IF EXISTS TEST CASCADE");
			DatabaseLoader.executeUpdate("CREATE TABLE TEST (A VARCHAR(10), B VARCHAR(10), PRIMARY KEY(A,B))");
			DatabaseLoader.executeUpdate("DELETE FROM TEST");
			DatabaseLoader.executeUpdate("insert into TEST VALUES('1','NAME','DESCRIPTION')");
			ResultSet rs = DatabaseLoader.executeQuery("select * from OAIPMHDATA");
			long count = 0;
			while(rs.next()) {
				System.out.println("ID > " + rs.getString(1));
				count++;
			}
			System.out.println("==> " +count);
			DatabaseLoader.closeConnection();	
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
