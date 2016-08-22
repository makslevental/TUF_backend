package database_maks_pkg;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static database_maks_pkg.DB.conn;
/** A collection is a set of Samples for use in TUF experiments.
 *
 * All Collections in the database_maks_pkg are listed by the TUF GUI using their
 * descriptions <b>which is name in MATLAB</b>.
 * <p>
 * While TUF collections are often associated with actual data
 * collection activities, the terms should not be confused.
 * </p>
 */
public class Collections extends Table {

    public Collections() throws SQLException {
        tableName = "collections";

        columns = new String[]{"description","name","id","uid_shards"};
        //turn columns string into "(description,name,"id","uid_shards)"
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM collections WHERE id=? AND uid_shards=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO collections ("+colsconcat+") VALUES (?,?,?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM collections");
    }


    /** Get row-id/UID corresponding to entry in postgres db <code>collections</code> table.
     *
     * A collection is uniquely identified by its id and shard that it was "shipped" with.
     *
     * @param id      id of the collection.
     * @param shard_id  Name of the shard (in MATLAB known as id) the collection was shipped with.
     * @return Integer row-id/UID
     */
    protected int getUID(String id, String shard_id) throws SQLException {
        pstmtUid.setString(1, id);
        int uid_shard = DB.shrds.getUID(shard_id);
        pstmtUid.setInt(2,uid_shard);
        return (Integer)getAttribArrayWPstmt(pstmtUid,"uid")[0];
    }

    /** Getter for all UIDs of samples owned by a collection.
     *
     * @param uid collection UID
     * @return sample UIDs.
     * */
    public int[] getSampleUids(int uid) throws SQLException {
        String queryStr = "SELECT DISTINCT collections_samples_bridge.uid_samples FROM collections " +
                "JOIN collections_samples_bridge ON (collections.uid = collections_samples_bridge.uid_collections) " +
                "WHERE collections.uid=" + uid;
        return Util.intUnboxArray(getAttribArray(queryStr, "uid_samples"));
    }

    /** Getter for MATLAB shard id <b>not to be confused postgres db UID</b> that owns a collection.
     *
     * In MATLAB land a shard's id is what I decided to call a shard's name in Java land.
     * The word id is a relic of Ken's db and name is more natural but for backwards
     * compatibility tuf.db.maxShard objects have to have id properties and so to keep the MATLAB
     * as clean as possible I did not propagate the nomenclature change up. Hence the public signature of this
     * method reflects that.
     *
     * @param uid collection UID
     * @return tuf.db.maxShard id property.
     */
    public String getShrdId(int uid) throws SQLException {
        String qry = "SELECT name FROM shards WHERE uid=(SELECT uid_shards FROM collections WHERE uid=" + uid + ")";
        return getStringAttrib(qry, "name");
    }

    /** Getter for postgres db shard UID <b>not to be confused with MATLAB layer shard id</b> that owns a collection.
     *
     * @param uid collection UID
     * @return shard table row UID
     */
    public int getShrdUID(int uid) throws SQLException {
        String qry = "SELECT uid FROM shards WHERE uid=(SELECT uid_shards FROM collections WHERE uid=" + uid + ")";
        return (Integer)getAttribArray(qry,"uid")[0];
    }
    
    public int[] getIntersectionRegionUID(int[] uids) throws SQLException {
    	StringBuilder qry = new StringBuilder();
    	String subqry = "SELECT DISTINCT uid_regions FROM samples JOIN" +
							"(SELECT uid_samples FROM collections_samples_bridge WHERE uid_collections = %d) us "+
						"on samples.uid = us.uid_samples";
    	for(int i = 0; i<uids.length-1; i++) {
    		qry.append(String.format(subqry,uids[i]));
    		qry.append(" INTERSECT ");
    	}
    	qry.append(String.format(subqry,uids[uids.length-1]));
        
        Object[] temp = getAttribArray(qry.toString(),"uid_regions");
        int[] region_uids = new int[temp.length];
        for (int i = 0; i < temp.length; i++) {
        	region_uids[i]= (int)temp[i];
        }
        return region_uids;
    }
//    String.format("%s",Arrays.toString(new int[]{1,2,3}))
    public int[] getIntersectionSamplesUidsArray(int[] coluids, int reguid) throws SQLException {
    	String subqry = "SELECT DISTINCT uid_samples FROM" +
    						"(SELECT coluid,coldesc,uid_samples FROM " + 
    								"(SELECT uid AS coluid, description AS coldesc FROM collections) AS _ " + 
    								"JOIN collections_samples_bridge ON coluid = uid_collections " +
    						"WHERE coluid IN (%s)) as __ " +
    						"JOIN samples ON uid_samples = samples.uid " +
    					"WHERE uid_regions = %d";
    	String qry = String.format(subqry,Arrays.toString(coluids).replace("[","").replace("]",""),reguid);
    	Object[] temp = getAttribArray(qry,"uid_samples");
        int[] sample_uids = new int[temp.length];
        for (int i = 0; i < temp.length; i++) {
        	sample_uids[i]= (int)temp[i];
        }
        return sample_uids;
    }
    
    public HashMap<Integer,ArrayList<Integer>> getIntersectionSamplesUidsMap(int[] coluids, int reguid) throws SQLException {
    	String subqry = "SELECT DISTINCT coluid,uid_samples FROM" +
    						"(SELECT coluid,coldesc,uid_samples FROM " + 
    								"(SELECT uid AS coluid, description AS coldesc FROM collections) AS _ " + 
    								"JOIN collections_samples_bridge ON coluid = uid_collections " +
    						"WHERE coluid IN (%s)) as __ " +
    						"JOIN samples ON uid_samples = samples.uid " +
    					"WHERE uid_regions = %d";
    	String qry = String.format(subqry,Arrays.toString(coluids).replace("[","").replace("]",""),reguid);
    	ResultSet rs = null;
        rs = conn.createStatement().executeQuery(qry);
        HashMap<Integer,ArrayList<Integer>> col_samps_mp = new HashMap<Integer,ArrayList<Integer>>();
        int coluid = -1;
        int sampuid = -1;
		while(rs.next()) {
        	coluid = rs.getInt(1);
			sampuid = rs.getInt(2);
        	if(col_samps_mp.containsKey(coluid)) {
        		ArrayList<Integer> samps = col_samps_mp.get(coluid);
        		samps.add(sampuid);
        	}
        	else {
        		col_samps_mp.put(coluid,new ArrayList<Integer>(Arrays.asList(sampuid)));
        	}
		}
        rs.close();
        return col_samps_mp;
    }

    //'name' in sqldb is the same as 'id' in matlab layer
    public String getId(int uid) throws SQLException {
        String query = "SELECT collections.id FROM collections WHERE uid = " + uid;
        return getStringAttrib(query,"id");
    }

    //'description' in sqldb is the same as 'name' in matlab layer
    public String getName(int uid) throws SQLException {
        String query = "SELECT collections.name FROM collections WHERE uid = " + uid;
        return getStringAttrib(query,"name");
    }

}