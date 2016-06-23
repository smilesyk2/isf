package kr.co.wisenut.dbtest;
import java.sql.*;
 
public class DriverTest {
 
	public static void main(String args[]) {
		String driver = "net.sourceforge.jtds.jdbc.Driver";
		String user = "pes2013"; 
		String pass = "pes2013";
		String dbURL = "jdbc:jtds:sqlserver://112.216.56.242:14331;loginTimeout=60;socketTimeout=60";
	
		Connection connection;
		try {
			Class.forName(driver);
			System.out.println("dddd");
			connection = DriverManager.getConnection(dbURL, user, pass);
			System.out.println("Driver found!!");
		} catch(SQLException se) {
			System.out.println("SQL Error!!");
		} catch(ClassNotFoundException cne){
			System.out.println("jdbc driver not founded!!"); 
		}
 
	}
}




