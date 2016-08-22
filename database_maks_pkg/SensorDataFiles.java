package database_maks_pkg;

import java.sql.ResultSet;
import java.sql.SQLException;

import static database_maks_pkg.DB.conn;

/** Actual sensor data corresponding to a sample.
 *
 * @author maksim
 */
public class SensorDataFiles extends Files {
    public SensorDataFiles() throws SQLException {
        tableName = "sensor_data_files";

        columns = new String[]{"path","name","type","description","md5","bounding_polygon_blob","uid_platforms","uid_regions"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM sensor_data_files WHERE name=? AND path=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO sensor_data_files ("+colsconcat+") VALUES (?,?,?,?,?,?,?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM sensor_data_files");
    }

    /** A sensor data file is identified by its name (which is actually a filename) and path
     *
     * @param name  filename
     * @param path  full path to file
     * @return  Integer row id
     */
    protected Integer getUID(String name, String path) throws SQLException {
        Integer uid = -1;
        pstmtUid.setString(1,name);
        pstmtUid.setString(2,path);
        ResultSet rs = pstmtUid.executeQuery();
        if(rs.next())
            uid = (Integer) rs.getObject("uid");
        return uid;
    }

    /** Getter for type by UID
     *
     * @param uid sensor data file UID
     * @return String type property of sensor data file
     * @throws SQLException
     */
    public String getType(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT sensor_data_files.type FROM sensor_data_files WHERE uid = "+uid;
        return getStringAttrib(queryStr,"type");
    }

    /** Getter for all samples that this sensor data file is included in.
     *
     * @param uid sensor data file UID
     * @return sample UIDs
     * @throws SQLException
     */
    public int[] getSampleUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT sensor_data_files_samples_bridge.uid_samples FROM sensor_data_files_samples_bridge WHERE uid_sensor_data_files = "+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid_samples"));

    }

}
