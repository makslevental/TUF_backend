package database_maks_pkg;

import java.sql.SQLException;

import static database_maks_pkg.DB.conn;

/** A Platform represents a sensor platform used to collect samples.
 *
 *
 * @author maksim
 */
public class Platforms extends Table{
    public Platforms() throws SQLException {
        tableName = "platforms";

        columns = new String[]{"description","name","attributes_blob"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM platforms WHERE name=?");
        pstmtUidDesc = conn.prepareStatement("SELECT uid FROM platforms WHERE description=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO platforms ("+colsconcat+") VALUES (?,?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM platforms");
    }

    /** All samples collected on this platform.
     *
     * @param uid platform UID
     * @return sample UIDs
     * @throws SQLException
     */
    public int[] getSampleUids(int uid) throws SQLException {
        //System.out.println("i hate matlab");
        String queryStr = "SELECT DISTINCT sensor_data_files_samples_bridge.uid_samples FROM sensor_data_files " +
                "JOIN sensor_data_files_samples_bridge ON (sensor_data_files.uid = sensor_data_files_samples_bridge.uid_sensor_data_files) " +
                "WHERE sensor_data_files.uid_platforms="+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid_samples"));
    }

    /** Getter serialization of MATLAB attributes struct.
     *
     * @param uid platform UID
     * @return String of transliterated byte array of serialized struct
     * @throws SQLException
     */
    public String getAttributesBlob(int uid) throws SQLException {
        String query = "SELECT platforms.attributes_blob FROM platforms WHERE uid="+uid;
        return getStringAttrib(query, "attributes_blob");
    }

}

