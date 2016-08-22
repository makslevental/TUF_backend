package database_maks_pkg;

import static database_maks_pkg.DB.conn;

import java.sql.SQLException;

public class ConfabulatedShards extends Shards{
	public ConfabulatedShards() throws SQLException {
        tableName = "shards_confab";
      
        columns = new String[]{"description","name","datenum"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM shards_confab WHERE name=?");
        pstmtUidDesc = conn.prepareStatement("SELECT uid FROM shards_confab WHERE description=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO shards_confab ("+colsconcat+") VALUES (?,?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM shards_confab");
    }
	

}
