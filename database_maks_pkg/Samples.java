package database_maks_pkg;

import java.sql.ResultSet;
import java.sql.SQLException;

import static database_maks_pkg.DB.conn;

/** The base unit of a shard, from which alarms are drawn.
 *
 * Samples are associated with zero or more files (which may represent
 * sensor data, ground truth, etc), and belong to zero or more
 * collections, but are originally shipped with a single shard.
 *
 */
public class Samples extends Table {

    public Samples() throws SQLException {
        tableName = "samples";

        columns = new String[]{"description","name","uid_shards","uid_regions","uid_truth_files"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");


        pstmtUid = conn.prepareStatement("SELECT uid FROM samples WHERE name=? AND uid_shards=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO samples ("+colsconcat+") VALUES (?,?,?,?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM samples");
    }


    /** A sample is uniquely identified by name and shard that the sample was "shipped" with.
     *
     * @param name sample id, typically sample#
     * @param uid_shard uid of the row in the Shards table that this sample was "shipped" with.
     * @return row id in the Samples table corresponding to this sample.
     * */
    protected Integer getUID(String name, Integer uid_shard) throws SQLException {
        Integer uid = -1;
        try {
            pstmtUid.setString(1,name);
            pstmtUid.setInt(2, uid_shard);
            ResultSet rs = pstmtUid.executeQuery();
            if(rs.next())
                uid = (Integer) rs.getObject("uid");
        } catch (SQLException e) {
            System.err.println("table=samples,name="+name+",uid_shards="+uid_shard);
            throw(e);
        }
        return uid;
    }

    //shard name in java layer is shard id in matlab layer
    /** Getter for MATLAB shard id <b>not to be confused Sqlite db UID</b> that owns a collection.
     *
     * In MATLAB land a shard's id is what I decided to call a shard's name in Java land.
     * The word id is a relic of Ken's db and name is more natural but for backwards
     * compatibility tuf.db.maxShard objects have to have id properties and so to keep the MATLAB
     * as clean as possible I did not propagate the nomenclature change up. Hence the public signature of this
     * method reflects that.
     *
     * @param uid sample UID
     * @return String tuf.db.maxShard id property.
     */
    public String getShrdId(int uid) throws SQLException {
        String qry = "SELECT name FROM shards WHERE uid=(SELECT uid_shards FROM samples WHERE uid="+uid+")";
        return getStringAttrib(qry, "name");
    }

    //shard name in java layer is shard id in matlab layer
    /** Getter for postgres db shard UID <b>not to be confused with MATLAB layer shard id</b> that owns a sample.
     *
     * @param uid sample UID
     * @return shard UID
     */
    public int getShrdUID(int uid) throws SQLException {
        String qry = "SELECT uid FROM shards WHERE uid=(SELECT uid_shards FROM samples WHERE uid=" + uid + ")";
        return (Integer)getAttribArray(qry,"uid")[0];
    }

    /** Getter for <code>regions</code> row UID corresponding to sample
     *
     * Basically this is backwards compatibility for matlab. Samples don't really have regions anymore so have to go through one of the files
     * that the sample owns. nor do regions have fucking sids (nothing has a sid anymore). so will return region uid
     *
     * @param uid sample UID
     * @return region UID
     */
    public int getRegionUid(int uid) throws SQLException {
        String queryStr = "SELECT sensor_data_files.uid_regions FROM samples " +
                "                    JOIN sensor_data_files_samples_bridge ON (samples.uid = sensor_data_files_samples_bridge.uid_samples) \n" +
                "                    JOIN sensor_data_files ON (sensor_data_files.uid = sensor_data_files_samples_bridge.uid_sensor_data_files)\n" +
                "                    WHERE samples.uid = "+uid;
        return (Integer)getAttribArray(queryStr, "uid_regions")[0];
    }

    /** Getter for <code>platforms</code> row UID corresponding to sample.
     *
     * @param uid sample UID
     * @return platform UID
     * @throws SQLException
     */
    public int getPlatformUid(int uid) throws SQLException {
        String queryStr = "SELECT sensor_data_files.uid_platforms FROM samples " +
                "                    JOIN sensor_data_files_samples_bridge ON (samples.uid = sensor_data_files_samples_bridge.uid_samples) " +
                "                    JOIN sensor_data_files ON (sensor_data_files.uid = sensor_data_files_samples_bridge.uid_sensor_data_files) " +
                "                    WHERE samples.uid = "+uid;
        int platuid = (Integer)getAttribArray(queryStr, "uid_platforms")[0];
        return (Integer)getAttribArray(queryStr, "uid_platforms")[0];
    }

    /** Getter for <code>sensor_data_files</code> rows UIDs that a sample owns
     *
     * @param uid sample UID
     * @return sensor data file UIDs
     * @throws SQLException
     */
    public int[] getSDFileUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT sensor_data_files_samples_bridge.uid_sensor_data_files FROM samples " +
                "                    JOIN sensor_data_files_samples_bridge ON (samples.uid = sensor_data_files_samples_bridge.uid_samples) " +
                "                    WHERE samples.uid = "+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid_sensor_data_files"));

    }

    /** Getter for <code>truth_files</code> rows UIDs that a sample owns
     *
     * @param uid sample UID
     * @return truth file UIDs
     * @throws SQLException
     */
    public int[] getTruthFileUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT samples.uid_truth_files FROM samples " +
                "                    WHERE samples.uid = "+uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid_truth_files"));

    }

    /** Getter for <code>collections</code> rows UIDs that a sample is included in
     *
     * @param uid sample UID
     * @return collection UIDs
     * @throws SQLException
     */
    public int[] getCollectionUids(int uid) throws SQLException {
        return Util.intUnboxArray(getAttribArray("SELECT DISTINCT uid_collections FROM collections_samples_bridge WHERE uid_samples = " + uid, "uid_collections"));
    }

}
