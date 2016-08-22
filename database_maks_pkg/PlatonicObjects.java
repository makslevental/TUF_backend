package database_maks_pkg;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static database_maks_pkg.DB.conn;

/** All objects that can appear in any alarm/lane.
 *
 * This table has extensive facilities because of <code>tuf.db.maxShrapnelDatabase.list_objinfo_by_property</code> method.
 *
 * Created by maksim on 9/19/14.
 */
public class PlatonicObjects extends Table {

    PreparedStatement getUidByContentPstmt;
    PreparedStatement getUidByNamePstmt;
    PreparedStatement getUidByPurposePstmt;

    public PlatonicObjects() throws SQLException {
        tableName = "platonic_objects";

        columns = new String[]{"content","purpose","description","name","major_axis","minor_axis","props_blob"};
        String colsconcat = java.util.Arrays.toString(columns).substring(1).replaceAll("\\]$", "");

        pstmtUid = conn.prepareStatement("SELECT uid FROM platonic_objects WHERE name=?");
        pstmtUid = conn.prepareStatement("SELECT uid FROM platonic_objects WHERE description=?");
        pstmtInsert = conn.prepareStatement("INSERT INTO platonic_objects ("+colsconcat+") VALUES (?,?,?,?,?,?,?)");
        pstmtWipe = conn.prepareStatement("DELETE FROM platonic_objects");

        getUidByContentPstmt = conn.prepareStatement("SELECT uid FROM platonic_objects WHERE content =?");
        getUidByNamePstmt = conn.prepareStatement("SELECT uid FROM platonic_objects WHERE name=?");
        getUidByPurposePstmt = conn.prepareStatement("SELECT uid FROM platonic_objects WHERE purpose =?");

    }

    /** Getter for content by UID.
     *
     *
     * @param uid platonic object UID
     * @return content property of object
     * @throws SQLException
     */
    public String getContent(int uid) throws SQLException {
        String query = "SELECT platonic_objects.content FROM platonic_objects WHERE uid = " + uid;
        return getStringAttrib(query, "content");
    }

    /** Getter for Major Axis by UID
     *
     * @param uid platonic object UID
     * @return Major Axis property of object
     * @throws SQLException
     */
    public double getMajorAxis(int uid) throws SQLException {
        String query = "SELECT platonic_objects.major_axis FROM platonic_objects WHERE uid = " + uid;
        return (Double)getAttribArray(query, "major_axis")[0];
    }

    /** Getter for Minor Axis
     *
     * @param uid platonic object UID
     * @return Minor Axis property of object
     * @throws SQLException
     */
    public double getMinorAxis(int uid) throws SQLException {
        String query = "SELECT platonic_objects.minor_axis FROM platonic_objects WHERE uid = " + uid;
        return (Double)getAttribArray(query, "minor_axis")[0];
    }

    /** Getter for purpose by UID
     *
     * @param uid platonic object UID
     * @return String purpose property of object
     * @throws SQLException
     */
    public String getPurpose(int uid) throws SQLException {
        String query = "SELECT platonic_objects.purpose FROM platonic_objects WHERE uid = " + uid;
        return getStringAttrib(query, "purpose");
    }

    /** Getter for Tags
     *
     * All tags associated with object.
     *
     * @param uid platonic object UID
     * @return String array of tags for object
     * @throws SQLException
     */
    public String[] getTags(int uid) throws SQLException {
        String query = "SELECT tags.name FROM " +
                "tags JOIN objects_tags_bridge ON (objects_tags_bridge.uid_tags = tags.uid)" +
                "WHERE objects_tags_bridge.uid_objects =" + uid;
        return Util.stringUnboxArray(getAttribArray(query, "name"));
    }

    /** Getter serialization of MATLAB properties struct.
     *
     * @param uid
     * @return String of transliterated byte array of serialized struct
     * @throws SQLException
     */
    public String getPropsBlob(int uid) throws SQLException {
        String query = "SELECT platonic_objects.props_blob FROM platonic_objects WHERE uid="+uid;
        return getStringAttrib(query, "props_blob");
    }

    //better to have sql do the filter than matlab

    /** Filter for all UIDs by content string
     *
     * @param content content property of object
     * @return matching object UIDs
     * @throws SQLException
     */
    public int[] getUidByContent(String content) throws SQLException {
        getUidByContentPstmt.setString(1, content);
        return Util.intUnboxArray(getAttribArrayWPstmt(getUidByContentPstmt, "uid"));
    }

    /** Filter for all UIDs by name string
     *
     * @param name name property of object
     * @return matching object UIDs
     * @throws SQLException
     */
    public int[] getUidByName(String name) throws SQLException {
        getUidByNamePstmt.setString(1, name);
        return Util.intUnboxArray(getAttribArrayWPstmt(getUidByNamePstmt, "uid"));
    }

    /** Filter for all UIDs by list of name strings
     *
     * @param names list of name property of multiple objects
     * @return matching object UIDs
     * @throws SQLException
     */
    public int[] getUidsByName(Object[] names) throws SQLException {
        String beginQuery = "SELECT uid FROM platonic_objects WHERE name=";
        StringBuffer endQuery = new StringBuffer();
        String name = "";
        for (int i=0; i<names.length-1;i++){
            name = "'"+names[i]+"'";
            endQuery.append(name);
            endQuery.append(" OR name=");
        }
        name = "'"+names[names.length-1]+"'";
        endQuery.append(name);                          //append last element of "names"
        String sqlQuery = beginQuery + endQuery.toString();
        return Util.intUnboxArray(getAttribArray(sqlQuery, "uid"));
    }

    /** Filter for all names(objecttypes) that correspond to filterquery names
     *
     * @param filterQuery SQL WHERE clause for filtering
     * @return string array of included object names
     * @throws SQLException
     */
    public String[] getFilteredNames(String filterQuery) throws SQLException {
        return Util.stringUnboxArray(getAttribArray(filterQuery, "name"));
    }

    /** Filter for all UIDs by purpose string
     *
     * @param purpose purpose property of object
     * @return matching object UIDs
     * @throws SQLException
     */
    public int[] getUidByPurpose(String purpose) throws SQLException {
        getUidByPurposePstmt.setString(1, purpose);
        return Util.intUnboxArray(getAttribArrayWPstmt(getUidByPurposePstmt, "uid"));
    }

    /** Filter for all UIDs by tag string
     *
     * @param tag tag property of object
     * @return matching object UIDs
     * @throws SQLException
     */
    public int[] getUidByTag(String tag) throws SQLException {
        int taguid = DB.tgs.getUID("tag");
        return Util.intUnboxArray(getAttribArray("SELECT uid_objects FROM objects_tags_bridge WHERE uid_tags = " + taguid, "uid_objects"));

    }

}
