package database_maks_pkg;

import java.sql.SQLException;
import static database_maks_pkg.DB.conn;

/** A Site represents a geographic area where samples are collected.
 *
 * It should be less specific than a Region (which is a constituent part of
 * a Site). 
 * @author maksim
 */
public class Sites extends Table {
    public Sites() throws SQLException {
        tableName = "sites";

        columns = new String[]{"description","name"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM sites WHERE name=?");
        pstmtUid = conn.prepareStatement("SELECT uid FROM sites WHERE description=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO sites ("+colsconcat+") VALUES (?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM sites");

    }

    /** Getter for all regions at some site.
     *
     * @param uid site UID
     * @return region UIDs
     * @throws SQLException
     */
    public int[] getRegionUids(int uid) throws SQLException {
        return Util.intUnboxArray(getAttribArray("SELECT DISTINCT uid FROM regions WHERE uid_sites=" + uid, "uid"));
    }
}
