package database_maks_pkg;
import java.sql.SQLException;
import static database_maks_pkg.DB.conn;
/** A data collection event. Contains a list of files (the data collected), which are packaged
 * together with truth files (for now) in to samples, and then included in collections.
 *
 */
public class Shards extends Table {

    public Shards() throws SQLException {
        tableName = "shards";

        columns = new String[]{"description","name","datenum"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM shards WHERE name=?");
        pstmtUidDesc = conn.prepareStatement("SELECT uid FROM shards WHERE description=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO shards ("+colsconcat+") VALUES (?,?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM shards");
    }

    /** Backwards compatible Getter for platforms associated with shard
     *
     * Shards don't contain platforms anymore so this goes through a very circuitous route to return something (anything).
     *
     * @param uid shard UID
     * @return platform UIDs
     * @throws SQLException
     */
    public int[] getPlatformUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT sensor_data_files.uid_platforms FROM samples \n" +
                "        JOIN sensor_data_files_samples_bridge ON (samples.uid = sensor_data_files_samples_bridge.uid_samples)\n" +
                "        JOIN sensor_data_files ON (sensor_data_files.uid = sensor_data_files_samples_bridge.uid_sensor_data_files)  \n" +
                "        WHERE samples.uid_shards = "+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid_platforms"));

    }

    /** Backwards compatible Getter for sites associated with shard
     *
     * Shards don't contain sites anymore so this goes through a very circuitous route to return something (anything).
     *
     * @param uid shard UID
     * @return site UIDs
     * @throws SQLException
     */
    public int[] getSiteUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT regions.uid_sites FROM regions JOIN samples ON " +
                "(regions.uid = samples.uid_regions) WHERE samples.uid_shards = "+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid_sites"));

    }

    /** Backwards compatible Getter for regions associated with shard
     *
     * Shards don't contain regions anymore so this goes through a very circuitous route to return something (anything).
     *
     * @param uid shard UID
     * @return regions UIDs
     * @throws SQLException
     */
    public int[] getRegionUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT samples.uid_regions FROM samples WHERE samples.uid_shards ="+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid_regions"));

    }

    /** Getter for samples associated with shard
     *
     * All samples contained in a shard.
     *
     * @param uid shard UID
     * @return sample UIDs
     * @throws SQLException
     */
    public int[] getSampleUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT samples.uid_regions FROM samples WHERE samples.uid_shards ="+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid_regions"));

    }

    /** Getter for collections associated with shard
     *
     * All collections contained in a shard.
     *
     * @param uid shard UID
     * @return collection UIDs
     * @throws SQLException
     */
    public int[] getCollectionUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT collections.uid FROM collections WHERE collections.uid_shards="+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid"));

    }
    //TODO-me potentially broken since i have no idea what shards files should return (order? duplicates?)
    //truthfiles follow sensor data files

    /** Getter for sensor data files shipped with shard
     *
     * This is kind of contrived since shards don't really mean anything anymore and data files exist and
     * are kept track of irrelevant of shards but anyway. Returns all sensor data files corresponding to all
     * samples that a shard shipped with.
     *
     * @param uid shard UID
     * @return sensor data file UIDs
     * @throws SQLException
     */
    public int[] getSDFileUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT sensor_data_files_samples_bridge.uid_sensor_data_files FROM " +
                "(samples JOIN sensor_data_files_samples_bridge ON " +
                "(samples.uid = sensor_data_files_samples_bridge.uid_samples)) " +
                "WHERE samples.uid_shards ="+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid_sensor_data_files"));

    }

    /** Getter for truth files shipped with shard
     *
     * This is kind of contrived since shards don't really mean anything anymore and data files exist and
     * are kept track of irrelevant of shards but anyway. Returns all truth files corresponding to all
     * samples that a shard shipped with.
     *
     * @param uid shard UID
     * @return truth file UIDs
     * @throws SQLException
     */
    public int[] getTruthFileUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT samples.uid_truth_files FROM " +
                "(samples JOIN sensor_data_files_samples_bridge ON " +
                "(samples.uid = sensor_data_files_samples_bridge.uid_samples)) " +
                "WHERE samples.uid_shards ="+uid;

        return Util.intUnboxArray(getAttribArray(queryStr, "uid_truth_files"));

    }
    //TODO-me think about objects in shards
    /*public int[] getObjectUids(int uid) throws SQLException {
        String queryStr = "SELECT collections.uid " +
                "FROM collections WHERE collections.uid_shards="+uid;
        return getAttribArray(queryStr, "collections.uid");

    }*/


}
