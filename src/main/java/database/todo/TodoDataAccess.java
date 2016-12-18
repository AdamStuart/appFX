package database.todo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TodoDataAccess
{

	private Connection conn;
	private static final String todoTable = "TODO";

	public TodoDataAccess() throws SQLException, ClassNotFoundException
	{

		// Class.forName("org.hsqldb.jdbc.JDBCDriver" );
		Class.forName("org.h2.Driver");
		conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
		// conn = DriverManager.getConnection(
		// "jdbc:hsqldb:file:db/TODOS_DB;ifexists=true;shutdown=true", "", "");

		conn.setAutoCommit(true);
		conn.setReadOnly(false);
	}

	public void closeDb() throws SQLException
	{

		conn.close();
	}

	public List<Todo> getAllRows() throws SQLException
	{

		String sql = "SELECT * FROM " + todoTable + " ORDER BY name";
		PreparedStatement pstmnt = conn.prepareStatement(sql);
		ResultSet rs = pstmnt.executeQuery();
		List<Todo> list = new ArrayList<>();

		while (rs.next())
		{

			int i = rs.getInt("id");
			String s1 = rs.getString("name");
			String s2 = rs.getString("description");
			list.add(new Todo(i, s1, s2));
		}

		pstmnt.close(); // also closes related result set
		return list;
	}

	public boolean nameExists(Todo todo) throws SQLException
	{
		String sql = "SELECT COUNT(id) FROM " + todoTable + " WHERE name = ? AND id <> ?";
		PreparedStatement pstmnt = conn.prepareStatement(sql);
		pstmnt.setString(1, todo.getName());
		pstmnt.setInt(2, todo.getId());
		ResultSet rs = pstmnt.executeQuery();
		rs.next();
		int count = rs.getInt(1);
		pstmnt.close();

		return (count > 0);
	}

	public int insertRow(Todo todo) throws SQLException
	{

		String dml = "INSERT INTO " + todoTable + " VALUES (DEFAULT, ?, ?)";
		PreparedStatement pstmnt = conn.prepareStatement(dml, PreparedStatement.RETURN_GENERATED_KEYS);
		pstmnt.setString(1, todo.getName());
		pstmnt.setString(2, todo.getDesc());
		pstmnt.executeUpdate(); // returns insert count

		// get identity column value
		ResultSet rs = pstmnt.getGeneratedKeys();
		rs.next();
		int id = rs.getInt(1);

		pstmnt.close();
		return id;
	}

	public void updateRow(Todo todo) throws SQLException
	{

		String dml = "UPDATE " + todoTable + " SET name = ?, description = ? " + " WHERE id = ?";
		PreparedStatement pstmnt = conn.prepareStatement(dml);
		pstmnt.setString(1, todo.getName());
		pstmnt.setString(2, todo.getDesc());
		pstmnt.setInt(3, todo.getId());
		pstmnt.executeUpdate(); // returns update count
		pstmnt.close();
	}

	public void deleteRow(Todo todo) throws SQLException
	{

		String dml = "DELETE FROM " + todoTable + " WHERE id = ?";
		PreparedStatement pstmnt = conn.prepareStatement(dml);
		pstmnt.setInt(1, todo.getId());
		pstmnt.executeUpdate(); // returns delete count (0 for none)
		pstmnt.close();
	}
}