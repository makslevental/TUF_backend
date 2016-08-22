/** Essentially a thin layer around the JDBC driver.
 *
 * Almost all classes correspond to tables in the postgres db. Exceptions are <code>Table</code>, <code>Bridge</code>, and <code>Files</code>
 * which are superclasses. Also <code>Util</code> is Java auxiliary methods.
 *
 * <p><b>Vaguely observed conventions:</b></p>
 * <p>In documentation identifiers with first letters capitalized refer to Java classes and otherwise refer to postgres db tables. For example
 * <code>Table</code> and <code>table</code>. <code>DB</code> is the Java class and postgres db is the actual postgres database.</p>
 * <p>Field suffix 'FullP' means full path</p>
 * <p>Field suffix 'F' means actual file</p>
 * <p>Field prefix 'mtlb' means passed in from MATLAB</p>
 * <p>Lower case db refers to the postgres db and uppercase DB refers to this class.</p>
 *
 * <p>The names of correspondent things might be disunified but it's a result of how the project evolved and
 * local optimizations. That is to say <code>SensorDataFilesSamplesBridge</code>,<code>sdfsb</code>,
 * <code>sensor_data_files_samples_bridge</code> all refer to conceptually the same thing
 * (class, class instance, SQL table respectively) but <code>sdfsb</code> was shortened since it would appear in code
 * often (in order to reduce visual noise) while <code>SensorDataFilesSamplesBridge</code>
 * and <code>sensor_data_files_samples_bridge</code> would not and so could be descriptive.</p>
 *
 * <p><b>Things to be aware of:</b></p>
 *
 * <p>'name' in postgres db  is the same as 'id' in MATLAB layer.</b> This is because in Ken's db there
 * were tons of fields that became obsolete and 'name' unified what was left. This has the unfortunate
 * side-effect of confusion. Sorry. </p>
 *
 * <p>'description' in Sqlite db is the same as 'name' in MATLAB layer </b> for the same reason.</p>
 *
 *
 * <p><b>Future</b></p>
 *
 * <p>Try to keep as much in the Java layer as possible. Trust me it will make your life a lot simpler to use
 * a real industrial strength programming language (Java) over MATLAB. Try to write prepared statements instead of
 * one off queries.</p>
 *
 *
 * @see database_maks_pkg.Collections#getShrdId(int) 'name' == 'id' shenanigans
 * @since 9/9/15
 * @author Maksim
 *
 * Created by maksim on 9/9/14.
 */
package database_maks_pkg;