package database_maks_pkg;

import java.sql.ResultSet;
import java.sql.SQLException;

import static database_maks_pkg.DB.conn;

/** Truth data for particular samples.
 *
 * Really this should be a property of regions at particular times, instead of samples. Soon it will be.
 *
 */
public class TruthFiles extends Files{
    public TruthFiles() throws SQLException {
        tableName = "truth_files";

        columns = new String[]{"path","name","description","md5","uid_regions"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM truth_files WHERE name=? AND path=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO truth_files ("+colsconcat+") VALUES (?,?,?,?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM truth_files");

    }

    protected Integer getUID(String name, String path) throws SQLException {
        Integer uid = -1;
        try {
            pstmtUid.setString(1,name);
            pstmtUid.setString(2,path);
            ResultSet rs = pstmtUid.executeQuery();
            if(rs.next())
                uid = (Integer) rs.getObject("uid");
        } catch (SQLException e) {
            System.err.println("table=truth_files,name="+name+",path="+path);
            throw(e);
        }
        return uid;
    }

    /** Getter for all sample UIDs that use this truth file.
     *
     * @param uid truth file UID
     * @return sample UIDs
     * @throws SQLException
     */
    public int[] getSampleUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT samples.uid FROM samples WHERE uid_truth_files = "+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid"));
    }
    //backwards support

    /** Getter for "truth type" type.
     *
     * For backwards compatibility with HMDS.
     *
     * @param uid truth file UID
     * @return String type
     */
    public String getType(int uid){ return "asc"; }
}
