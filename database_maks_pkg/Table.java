package database_maks_pkg;

import java.sql.*;
import java.util.ArrayList;

import static database_maks_pkg.DB.conn;

/**
 * <p>
 * "All tables are created equal. Some work harder in preseason" - Emmit Smith
 * <p>
 * Every table in the DB is subclassed from this one. This class basically functions as
 * central repository of common code.
 * <p>
 * So far each table has a set of unique(personal) strings that are used to build sql prepared statements,
 * the prepared statements themselves, a list of column names. For example the uncommittedInsert string, which is used to build &nbsp;<code>pstmtInsert</code>&nbsp; is
 * of the form
 * <p>"INSERT INTO tableName (columns[0],...,columns[n]) VALUES (?,..,?)"</p>
 *
 * @author maksim
 * @version 0.00000000000000....1
 */

abstract class Table {

    /** Name of the table duh. */
    String tableName;

    /**
     * Column names for each of the attributes of a table.
     * */
    String[] columns;
    /**
     * SQL prepared statement that is used to query for the row id.
     * */
    public PreparedStatement pstmtUid;
    public PreparedStatement pstmtUidDesc;
    /**
     * SQL prepared statement that is used to uncommittedInsert into a table.
     * */
    public PreparedStatement pstmtInsert;
    /**
     * SQL prepared statement to completely wipe a table.
     * */
    public PreparedStatement pstmtWipe;


    /**
     *
     * Insert rows into table. Uses prepared statement. Throws sql exception if collision in table or something
     * like that.
     *
     * This really isn't used anywhere anymore because inserts only happen through the psql client. It used
     * to be that inserts were done by this java app.
     *
     * @param strValues string field values to be inserted
     * @param intValues integer field values to be inserted
     * @throws SQLException
     */
    public void uncommittedInsert(String[] strValues, int[] intValues)
            throws SQLException
        {
        for (int j = 0; j < strValues.length; j++) {
            pstmtInsert.setString(j + 1, strValues[j]);
        }

        for (int j = 0; j < intValues.length; j++) {
            pstmtInsert.setInt(j + strValues.length + 1, intValues[j]);
        }
        pstmtInsert.addBatch();
    }
    //for debug purposes only; very slow (maybe not?) leave in debug mode for row fail information)
    protected void committedInsert(String[] strValues, int[] intValues)
            throws SQLException
    {
        for (int j = 0; j < strValues.length; j++) {
            pstmtInsert.setString(j + 1, strValues[j]);
        }

        for (int j = 0; j < intValues.length; j++) {
            pstmtInsert.setInt(j + strValues.length + 1, intValues[j]);
        }
        pstmtInsert.executeUpdate();
    }


    /**
     * Completely blow away the table.
     * Executes 'DELETE FROM table'
     * @param really
     */
    protected void wipeTable(boolean really) throws SQLException {
        pstmtWipe.executeUpdate();
        //db is autocommit by default
        //conn.commit();
    }


    /**
     * Get the uid for a row. Duh. If a table is uniquely identified by more than one column then this is
     * overridden.
     *
     * @param name Most have rows uniquely identified by a string field aptly called 'name'.
     *             Those tables for which rows are uniquely identified by 2 or more columns take
     *             2 arguments.
     *
     * @return     The unique row id.
     *
     * @see Samples#getUID(String, Integer)
     */
    public Integer getUID(String name) throws SQLException {
        Integer uid = -1;
        try {
            pstmtUid.setString(1, name);
            ResultSet rs = pstmtUid.executeQuery();
            if(rs.next())
                uid = (Integer) rs.getObject("uid");
            rs.close();
        } catch (SQLException e) {
            System.err.println("TABLE:"+tableName+" name="+name);
            throw(e);
        }
        return uid;
    }
    
    public Integer getUIDDesc(String desc) throws SQLException {
        Integer uid = -1;
        try {
            pstmtUidDesc.setString(1, desc);
            ResultSet rs = pstmtUidDesc.executeQuery();
            if(rs.next())
                uid = (Integer) rs.getObject("uid");
            rs.close();
        } catch (SQLException e) {
            System.err.println("TABLE:"+tableName+" name="+desc);
            throw(e);
        }
        return uid;
    }


    // this just generifies pulling a result from a table whose type you know
    // should be a string. Attribute meaning SQL column.
    protected String getStringAttrib(String qry, String col) throws SQLException {
        String attribute = null;
        ResultSet rs = null;
        rs = conn.createStatement().executeQuery(qry);
        while (rs.next())
            attribute = rs.getString(col);
        return attribute;
    }

    // this just generifies pulling a result from a table whose type the caller knows
    // but the callee doesn't (i.e. this function doesn't know what type the attribute should be).
    protected Object[] getAttribArray(String qry, String col) throws SQLException {
        ResultSet rs = null;
        rs = conn.createStatement().executeQuery(qry);
        ArrayList<Object> temp = new ArrayList<Object>();
        while(rs.next())
            temp.add(rs.getObject(col));
        rs.close();
        Object[] attrib = new Object[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            attrib[i]= temp.get(i);
        }
        return attrib;
    }

    // same thing as above except use a prepared statement to fetch the result instead of compiling
    // a one-off SQL query.
    protected Object[] getAttribArrayWPstmt(PreparedStatement pstmt, String col) throws SQLException {
        ResultSet rs = pstmt.executeQuery();
        ArrayList<Object> temp = new ArrayList<Object>();
        while(rs.next())
            temp.add(rs.getObject(col));
        rs.close();
        Object[] attrib = new Object[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            attrib[i]= temp.get(i);
        }
        return attrib;
    }


    //'name' in sqldb is the same as 'id' in matlab layer
    public String getId(int uid) throws SQLException {
        String query = "SELECT "+tableName+".name FROM " + tableName + " WHERE uid = " + uid;
        return getStringAttrib(query,"name");
    }

    //'description' in sqldb is the same as 'name' in matlab layer
    public String getName(int uid) throws SQLException {
        String query = "SELECT "+tableName+".name FROM " + tableName + " WHERE uid = " + uid;
        return getStringAttrib(query,"name");
    }
    //'name' in sqldb is the same as 'description' in matlab layer    
    public String getDescription(int uid) throws SQLException {
        String query = "SELECT "+tableName+".description FROM " + tableName + " WHERE uid = " + uid;
        return getStringAttrib(query,"description");
    }
    
    //int[]
    public int[] getAll() throws SQLException {
        //stub is a stub, because matlab method getAll calls getter and passes self.uid
        Object[] uidinobjs = getAttribArray("SELECT uid FROM " + tableName, "uid");
        int[] uids = new int[uidinobjs.length];
        for(int i=0;i<uidinobjs.length;++i)
            uids[i]=(Integer)uidinobjs[i];
        return uids;
    }



}
