package database_maks_pkg;

import javax.naming.AuthenticationException;

import java.awt.EventQueue;
import java.io.*;
import java.sql.*;
import java.util.Properties;

/** Primary interface to the postgres db - all reads and writes are through this.
 *
 * An instance doesn't correspond to an extant db, but just an extant connection to the postgres server.
 *
 */
public class DB {

    /** Architecture specific file separator.*/
    private static String FSEP = File.separator;

    /** Actual SQL connection to postgres server.
     *
     * This is static so that all tables can simply static import database_maks_pkg.DB and use the connection, rather than
     * instantiating their own copies of a DB object.
     * */
    protected static Connection conn;


    /** postgres db url
     *
     * jdbc url
     * */
    private String url;

    /** postgres connection properties
     *
     * things like username, password, timeouts`
     * */
    private Properties props;

    /** Names of tables as specified in the SQL CREATE statement and therefore in the actual postgres db. */
    public static String[] tblNames = {"sensor_data_files", "truth_files", "shards",
            "collections","samples","regions","sites",
            "platforms","platonic_objects","tags",
            "instance_objects","sensor_data_files_samples_bridge",
            "objects_tags_bridge","objects_truth_files_bridge",
            "objects_sensor_data_files_bridge","collections_samples_bridge"};


    /** Use to access collections table.
     *
     * Each of the tables is instantiated and owned by DB object.
     * This is so each of the tables has access to each of the other tables, <b>through</b> DB.
     * The pattern is essentially a convenient way to package together, and disseminate lots
     * of common methods. Having each of the tables be a class with static methods doesn't work because
     * of how Java works so this is the solution I came up with (don't hate me if it's not "best practices").
     */
    /** Use to access collections table in postgres db. @see #cols */
    public static Collections cols;

    /** Use to access shards table in postgres db. @see #cols */
    public static Shards shrds;
    public static ConfabulatedShards shrds_confb;

    /** Use to access tags table in postgres db. @see #cols */
    public static Tags tgs;

    /** Use to access sites table postgres db. @see #cols */
    public static Sites sts;

    /** Use to access regions table  postgres db. @see #cols */
    public static Regions regs;

    /** Use to access truth_files table  postgres db. @see #cols */
    public static TruthFiles truthfs;

    /** Use to access sensor_data_files table postgres db. @see #cols */
    public static SensorDataFiles sdfs;

    /** Use to access samples table postgres db. @see #cols */
    public static Samples samps;

    /** Use to access platforms table postgres db. @see #cols */
    public static Platforms plats;

    /** Use to access platonic_objects table postgres db. @see #cols */
    public static PlatonicObjects platobjs;

    /** Use to access sensor_data_files_samples_bridge table postgres db. @see #cols */
    public static SensorDataFilesSamplesBridge sdfsb;

    /** Use to access collections_samples_bridge table postgres db. @see #cols */
    public static CollectionsSamplesBridge colsampb;

    /** Use to access objects_tags_bridge table postgres db. @see #cols */
    public static ObjectsTagsBridge objtgsb;

    /** Establishes connection to postgres db backing.
     *
     * First ldap auth user to see if they belong (that's the password dialog part).
     * Then if that authenticates build a connection, check if the connection is ssl (encrypted).
     * Then link tables - meaning construct the prepared statements for each table (which has to be done
     * after the connection to the database is constructed because the prepared statement are connection dependent).
     *
     */
    public DB(String address, String dbname) throws SQLException, AuthenticationException {

        PasswordDialog p = new PasswordDialog(null, "LDAP Authentication");
        if(p.showDialog()){

            this.url = "jdbc:postgresql://" + address + "/" + dbname;
            props = new Properties();
            props.setProperty("user", p.getName());
            props.setProperty("password", p.getPass());
            props.setProperty("ssl", "true");
            props.setProperty("loginTimeout", "5");
            props.setProperty("connectTimeout", "5");
            props.setProperty("socketTimeout", "5");
            conn = DriverManager.getConnection(url, props);

            //check ssl
            ResultSet rs = conn.createStatement().executeQuery("SELECT ssl_is_used();");
            rs.next();

            System.out.println("SSL is being used: "+rs.getString(1).toUpperCase());

            linkTables();
            System.out.println("Database startup successful");
        } else {
            throw new SQLException("28000");
        }

        if(p.getName().isEmpty() || p.getPass().isEmpty())
            throw new AuthenticationException("Incorrect login/password");
    }



    /** Links DB instance to all of the table objects, and therefore all of the postgres tables.
     *
     * When each table object is instantiated it knows which postgres db it corresponds visavis its constructor (each table's). Order of
     * linking matters because, for example, bridge tables have foreign key attributes that refer to rows in the tables they bridge. So
     * the order as is corresponds to topological sort of dependence.
     */
    private void linkTables() throws SQLException {
        System.out.println("Linking tables");

        //tables that have no fkeys
        tgs = new Tags();
        sts = new Sites();
        shrds = new Shards();
        shrds_confb = new ConfabulatedShards();
        platobjs = new PlatonicObjects();
        plats = new Platforms();

        //with fkeys
        cols = new Collections();
        regs = new Regions();
        truthfs = new TruthFiles();
        samps = new Samples();

        //depends plats
        sdfs = new SensorDataFiles();

        //bridges - fully dependent
        sdfsb = new SensorDataFilesSamplesBridge();
        colsampb = new CollectionsSamplesBridge();
        objtgsb = new ObjectsTagsBridge();
    }

    public void confabulate() {
    	EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Confabulator window = new Confabulator();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
    }
    
    public boolean checkConnection() throws SQLException {
        return conn.isValid(10);
    }

    //nonsense test stuff
    public static void main(String[] args) throws Exception {
    	DB test = new DB("127.0.0.1","hhehd_niitek_tuf");
    	test.confabulate();


    }

}
