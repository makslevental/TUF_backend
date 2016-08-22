package database_maks_pkg;

import java.sql.SQLException;
import static database_maks_pkg.DB.conn;
/** Each sensor data file could belong to many samples
 * and a sample could have many sensor data files (in the modern "conflatulated" world).
 *
 */
public class SensorDataFilesSamplesBridge extends Bridge {
    /** A <code>sensor_data_files_samples_bridge</code> row has 4 foreign keys but only links to 2 tables.
     * This is because a <code>sensor_data_files</code> row uniquely identified by its name and path while a
     * <code>samples</code> row is uniquely identified by its name and shard is as well. So there are two "shadow keys"
     * which just means 2 attributes in the csvs correspond to one table.
     */
    public SensorDataFilesSamplesBridge() throws SQLException {
        tableName = "sensor_data_files_samples_bridge";

        columns = new String[]{"uid_sensor_data_files","uid_samples"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM sensor_data_files_samples_bridge WHERE uid_sensor_data_files=? AND uid_samples=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO sensor_data_files_samples_bridge ("+colsconcat+") VALUES (?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM sensor_data_files_samples_bridge");
    }

}
