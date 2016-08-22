package database_maks_pkg;

import java.sql.SQLException;
import static database_maks_pkg.DB.conn;
/** Many to Many table between objects and tags.
 *
 */
public class ObjectsTagsBridge extends Bridge{
    /** Links <code>platonic_objects</code> and <code>tags</code>.
     *
     * A <code>platonic_objects</code> row is identified by a name attribute and likewise a <code>tags</code> row.
     */

    public ObjectsTagsBridge() throws SQLException {
        tableName = "objects_tags_bridge";

        columns = new String[]{"uid_tags","uid_objects"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM objects_tags_bridge WHERE uid_tags=? AND uid_objects=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO objects_tags_bridge ("+colsconcat+") VALUES (?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM objects_tags_bridge");
    }
}
