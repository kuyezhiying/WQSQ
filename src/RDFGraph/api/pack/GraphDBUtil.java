package RDFGraph.api.pack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GraphDBUtil {

	private static Connection conn = null;
	
	public static Connection getConnection() {
		Connection connection = null;
		try {
			Class.forName("org.neo4j.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:neo4j://localhost:7474/", "neo4j", "123456");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}
	
    public boolean nodeExist(String nodeURI) {
    	boolean exist = false;
    	Connection con = getConnection();
    	String nodeExistQuery = "MATCH (a {uri : '" + nodeURI + "}) RETURN a";
		try(PreparedStatement stmt = con.prepareStatement(nodeExistQuery)) {
			try(java.sql.ResultSet rs = stmt.executeQuery()) {
		    	if(rs.next()) {
		    		exist = true;
		    	}
		    	rs.close();
			}
			con.close();
		} catch (SQLException e) {
			System.out.println("Exception in judging if a node exis.");
			e.printStackTrace();
		}
		return exist;
    }
    
    public void createUniqueNode(String nodeURI) {
    	Connection con = getConnection();
    	String createUniqueNodeQuery = "MATCH (a {uri : '" + nodeURI + "}) RETURN a";
		try(PreparedStatement stmt = con.prepareStatement(createUniqueNodeQuery)) {
			stmt.executeQuery();
			con.close();
		} catch (SQLException e) {
			System.out.println("Exception in creating a unique node.");
			e.printStackTrace();
		}
    }
    
    public void createRelationship(String lftNodeURI, String relationURI, String rgtNodeURI) {
    	
    }

    public static void main(String[] args) {

		// Get connecttion
		conn = getConnection();
	
		//String query = "MATCH (:Movie {title:{1}})<-[:ACTED_IN]-(a:Person) RETURN a.name as actor";
		String query2 = "MATCH (nineties:Movie) WHERE nineties.released > 1990 AND nineties.released < 2000 RETURN nineties.title as actor";

		try (PreparedStatement stmt = conn.prepareStatement(query2)) {
	
		    stmt.setString(1,"The Matrix");
	
		    try (ResultSet rs = stmt.executeQuery()) {
		        while(rs.next()) {
		             System.out.println(rs.getString("actor"));
		        }
		    }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
