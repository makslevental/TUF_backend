package database_maks_pkg;

import java.sql.SQLException;

import static database_maks_pkg.DB.conn;

/** I have no idea? Some sort of identifier for mine objects.
 *
 */
public class Tags extends Table {
    public Tags() throws SQLException {
        tableName = "tags";
        columns = new String[]{"name"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM tags WHERE name=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO tags ("+colsconcat+") VALUES (?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM tags");
    }


}
