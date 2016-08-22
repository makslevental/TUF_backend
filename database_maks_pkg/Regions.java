package database_maks_pkg;

import java.sql.SQLException;

import static database_maks_pkg.DB.conn;

/** A Region represents a geographic area where samples are collected.
 *
 * It should be more specific than a Site (which is a Region is a part of).
 *
 *  @author maksim
 */
public class Regions extends Table{
    public Regions() throws SQLException {
        tableName = "regions";
        columns = new String[]{"description","name","srid","bounding_polygon_blob","uid_sites"};

        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");


        pstmtUid = conn.prepareStatement("SELECT uid FROM regions WHERE name=?");
        pstmtUid = conn.prepareStatement("SELECT uid FROM regions WHERE description=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO regions ("+colsconcat+") VALUES (?,?,?,?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM regions");
    }

    /** Getter for site where region is located.
     *
     * @param uid region UID
     * @return site UID
     * @throws SQLException
     */
    public int getSite(int uid) throws SQLException {
        String query = "SELECT regions.uid_sites FROM regions WHERE uid = " + uid;
        return (Integer)getAttribArray(query, "uid_sites")[0];
    }

    /** Getter for all samples collected in region
     *
     * @param uid region UID
     * @return sample UIDs
     * @throws SQLException
     */
    public int[] getSampleUids(int uid) throws SQLException {
        return Util.intUnboxArray(getAttribArray("SELECT DISTINCT uid FROM samples WHERE uid_regions=" + uid, "uid"));
    }
}
