package database_maks_pkg;

import java.sql.SQLException;

/** SQL bridge table abstract class.
 *
 * Purely bridging tables are slightly different from normal tables in that rows
 * don't represent any information beyond relationships between rows in linked tables (duh).
 * Effectively this means there are no non-integer tuple entries. Furthermore populating them
 * means only fetching UIDs for rows from other tables.
 *
 */
public abstract class Bridge extends Table {

    /** Get row-id/UID with foreign keys in bridged tables.
     *
     * Since bridges have no real data, rows are identified by the pairs of integer foreign keys.
     *
     * @param column1 UID corresponding to foreign key in first bridged table
     * @param column2 UID corresponding to foreign key in second bridged table
     * @return Integer row id
     */
    protected int getUID(int column1, int column2) throws SQLException {
        pstmtUid.setInt(1,column1);
        pstmtUid.setInt(2,column2);
        return (Integer)getAttribArrayWPstmt(pstmtUid,"uid")[0];
    }

}
