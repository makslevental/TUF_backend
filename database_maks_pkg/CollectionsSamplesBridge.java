package database_maks_pkg;

import java.sql.SQLException;
import static database_maks_pkg.DB.conn;
/** Collections contain many samples, and particular samples can appear in many collections
 * (in the modern "conflatulated" world).
 */
public class CollectionsSamplesBridge extends Bridge{
    /** A <code>collections_samples_bridge</code> row has 4 foreign keys but only links to 2 tables.
     * This is because a <code>collections</code> row uniquely identified by its name and shard id while a
     * <code>samples</code> row is uniquely identified by its name and shard is as well. So there are two "shadow keys"
     * which just means 2 attributes in the csvs correspond to one table.
     */
    public CollectionsSamplesBridge() throws SQLException {
        tableName = "collections_samples_bridge";

        columns = new String[]{"uid_collections","uid_samples"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM collections_samples_bridge WHERE uid_collections=? AND uid_samples=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO collections_samples_bridge ("+colsconcat+") VALUES (?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM collections_samples_bridge");
    }



}

