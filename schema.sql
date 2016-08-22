CREATE TABLE sites ( 
    uid         SERIAL NOT NULL
                        PRIMARY KEY,
    description TEXT    NOT NULL,
    name      VARCHAR(255)    NOT NULL,
    UNIQUE ( name ) 
);

CREATE TABLE platforms ( 
    uid             SERIAL NOT NULL
                            PRIMARY KEY,
    description     TEXT    NOT NULL,
    name      VARCHAR(255)    NOT NULL,
    attributes_blob TEXT NOT NULL,
    UNIQUE ( name ) 
);

CREATE TABLE regions ( 
    uid         SERIAL NOT NULL
                        PRIMARY KEY,
    description TEXT    NOT NULL,
    name      VARCHAR(255)    NOT NULL,
    srid        TEXT    NOT NULL,
    bounding_polygon_blob TEXT,
    uid_sites   INTEGER DEFAULT 0
                        REFERENCES sites ( uid ) ON DELETE CASCADE,
    UNIQUE(name)  
);

CREATE TYPE SDFSTYPE AS -- omitted

CREATE TABLE sensor_data_files ( 
    uid           SERIAL NOT NULL PRIMARY KEY,
    path     TEXT    NOT NULL,
    fullpathhsh       CHAR(32) NULL,
    name      VARCHAR(255)    NOT NULL,
    type          SDFSTYPE    NOT NULL,
    description   TEXT    NOT NULL,
    md5           TEXT,
    bounding_polygon_blob TEXT,
    -- CASCADE specifies that when a referenced row is deleted, row(s) referencing it should be automatically deleted as well.
    -- RESTRICT prevents deletion of a referenced row. 
    -- There are two other options: SET NULL and SET DEFAULT. These cause the referencing column(s) in the referencing row(s) 
    -- to be set to nulls or their default values, respectively, when the referenced row is deleted. 
    uid_platforms INTEGER DEFAULT 0 REFERENCES platforms ( uid ) ON DELETE SET DEFAULT, 
    uid_regions   INTEGER DEFAULT 0 REFERENCES regions ( uid ) ON DELETE SET DEFAULT,
    UNIQUE ( fullpathhsh, name ) 
);

CREATE TABLE truth_files ( 
    uid         SERIAL NOT NULL PRIMARY KEY,
    path     TEXT    NOT NULL,
    fullpathhsh       CHAR(32) NULL, 
    name      VARCHAR(255)    NOT NULL,
    description TEXT    NOT NULL,
    md5         TEXT,
    uid_regions INTEGER DEFAULT 0
                        REFERENCES regions ( uid ) ON DELETE SET DEFAULT,
    UNIQUE ( fullpathhsh, name ) 
);

CREATE FUNCTION hash_path_fn() RETURNS trigger AS 
$body$
BEGIN
  	NEW.fullpathhsh = (SELECT MD5(CONCAT(NEW.path, NEW.name)));
  	RETURN NEW;
END
$body$
LANGUAGE 'plpgsql';


CREATE TRIGGER sdfs_hash BEFORE INSERT ON sensor_data_files
FOR EACH ROW
	EXECUTE PROCEDURE hash_path_fn();

CREATE TRIGGER truthfs_hash BEFORE INSERT ON truth_files
FOR EACH ROW
	EXECUTE PROCEDURE hash_path_fn();

-- using serial for the primary key keeps the primary keys distinct 
-- between base table and inheriting tables (no need for sequence)
-- using SERIAL makes shards own the underlying sequence:  It does two things: 
-- (1) make the sequence depend on the column, if you drop the column (or the table) the sequence is dropped; 
-- (2) if you truncate the table with RESTART IDENTITY, it resets the sequence
CREATE TABLE shards ( 
    uid             SERIAL NOT NULL
                            PRIMARY KEY,
    description     TEXT    NOT NULL,
    name      VARCHAR(255)    NOT NULL,
    datenum         DOUBLE PRECISION    NOT NULL,
    UNIQUE(name) 
);

CREATE FUNCTION uid_shards_fk_check_fn() RETURNS trigger AS 
$body$
BEGIN
    -- because shards_confab in herits from shards select from shards
    -- checks against shards_confab too
    IF NOT EXISTS(SELECT uid FROM shards WHERE uid = NEW.uid_shards) THEN
        RAISE EXCEPTION '% doesn''t have a valid foreign key into shards', NEW.name;
    END IF;
  	RETURN NEW;
END
$body$
LANGUAGE 'plpgsql';


CREATE TRIGGER uid_shards_fk_check_trig BEFORE INSERT ON collections
FOR EACH ROW
	EXECUTE PROCEDURE uid_shards_fk_check_fn();


CREATE TRIGGER uid_shards_fk_check_trig BEFORE INSERT ON samples
FOR EACH ROW
	EXECUTE PROCEDURE uid_shards_fk_check_fn();

CREATE FUNCTION shards_delete_fn() RETURNS trigger AS 
$body$
BEGIN
    EXECUTE 'DELETE FROM samples WHERE uid_shards=$1' USING OLD.uid;
    EXECUTE 'DELETE FROM collections WHERE uid_shards=$1' USING OLD.uid;
  	RETURN OLD;
END
$body$
LANGUAGE 'plpgsql';

CREATE TRIGGER shards_delete AFTER DELETE ON shards
FOR EACH ROW
	EXECUTE PROCEDURE shards_delete_fn();

CREATE TRIGGER shards_confab_delete AFTER DELETE ON shards_confab
FOR EACH ROW
	EXECUTE PROCEDURE shards_delete_fn();







CREATE TABLE shards_confab ( 
    UNIQUE(uid)
) INHERITS (shards);


CREATE TABLE collections (
    uid             SERIAL NOT NULL
                            PRIMARY KEY ,
    description     TEXT    NOT NULL,
    name      VARCHAR(255)    NOT NULL,
    id      VARCHAR(255)    NOT NULL,
    uid_shards      INTEGER DEFAULT 0
    UNIQUE(name, uid_shards) 
);


--CREATE TABLE collections_confab ( 
--    uid_shards      INTEGER DEFAULT 0
--                            REFERENCES shards_confab ( uid ) ON DELETE CASCADE,
--    UNIQUE(uid)
--) INHERITS (collections);



CREATE TABLE samples ( 
    uid             SERIAL NOT NULL
                            PRIMARY KEY ,
    description     TEXT    NOT NULL,
    name      VARCHAR(255)    NOT NULL,
    uid_shards      INTEGER DEFAULT 0,
    uid_regions     INTEGER DEFAULT 0
                            REFERENCES regions ( uid ) ON DELETE SET DEFAULT,
    uid_truth_files INTEGER DEFAULT 0
                            REFERENCES truth_files ( uid ) ON DELETE SET DEFAULT,
    UNIQUE ( name , uid_shards ) 
);


--CREATE TABLE samples_confab ( 
--    uid_shards      INTEGER DEFAULT 0
--                            REFERENCES shards_confab ( uid ) ON DELETE CASCADE,
--) INHERITS (samples);



CREATE TABLE platonic_objects ( 
    uid         SERIAL NOT NULL
                        PRIMARY KEY ,
    content     TEXT    NOT NULL,
    purpose     TEXT    NOT NULL,
    description TEXT    NOT NULL,
    name      VARCHAR(255)    NOT NULL,
    major_axis  DOUBLE PRECISION    NOT NULL,
    minor_axis  DOUBLE PRECISION    NOT NULL,
    props_blob  TEXT    NOT NULL,
    UNIQUE ( name ) 
);

CREATE TABLE tags ( 
    uid SERIAL NOT NULL
                PRIMARY KEY ,
    name      VARCHAR(255)    NOT NULL,
    UNIQUE ( name ) 
);


CREATE TABLE instance_objects ( 
    uid                  SERIAL PRIMARY KEY 
                                 NOT NULL,
    pitch                DOUBLE PRECISION    NOT NULL,
    roll                 DOUBLE PRECISION    NOT NULL,
    yaw                  DOUBLE PRECISION    NOT NULL,

    depth                DOUBLE PRECISION    NOT NULL,
    UTMx                 DOUBLE PRECISION    NOT NULL,
    UTMy                 DOUBLE PRECISION    NOT NULL,
    time_buried          DOUBLE PRECISION    NOT NULL,
    time_dug_up          DOUBLE PRECISION    NOT NULL,
    srid                 TEXT    NOT NULL,
    uid_platonic_objects INTEGER DEFAULT 0
                             REFERENCES platonic_objects ( uid ) ON DELETE SET DEFAULT,
    UNIQUE ( depth, UTMx, UTMy, time_buried, time_dug_up ) 
);


/*






bridges








*/

CREATE TABLE sensor_data_files_samples_bridge ( 
    uid                  SERIAL PRIMARY KEY 
                                 NOT NULL,
    uid_sensor_data_files INTEGER NOT NULL
                                  REFERENCES sensor_data_files ( uid ) ON DELETE CASCADE,
    uid_samples           INTEGER NOT NULL
                                    REFERENCES samples ( uid ) ON DELETE CASCADE,
    UNIQUE (uid_sensor_data_files, uid_samples)                                
);

--CREATE TABLE sensor_data_files_samples_bridge_confab ( 
--    uid                  SERIAL PRIMARY KEY 
--                                 NOT NULL,
--    uid_sensor_data_files INTEGER NOT NULL
--                                  REFERENCES sensor_data_files ( uid ) ON DELETE CASCADE,
--    uid_samples           INTEGER NOT NULL
--                                    REFERENCES samples_confab ( uid ) ON DELETE CASCADE,
--    UNIQUE (uid_sensor_data_files, uid_samples)
--) INHERITS (sensor_data_files_samples_bridge);

CREATE TABLE objects_tags_bridge ( 
    uid                  SERIAL PRIMARY KEY 
                                 NOT NULL,
    uid_tags    INTEGER NOT NULL
                        REFERENCES tags ( uid ) ON DELETE CASCADE,
    uid_objects INTEGER NOT NULL
                        REFERENCES platonic_objects ( uid ) ON DELETE CASCADE,
     UNIQUE (uid_tags, uid_objects)                       
);

CREATE TABLE instance_objects_truth_files_bridge ( 
    uid                  SERIAL PRIMARY KEY 
                                 NOT NULL,
    uid_truth_files      INTEGER NOT NULL
                                 REFERENCES truth_files ( uid ) ON DELETE CASCADE,
    uid_instance_objects INTEGER NOT NULL
                                 REFERENCES instance_objects ( uid ) ON DELETE CASCADE,
      UNIQUE (uid_truth_files, uid_instance_objects)                       
                               
);

CREATE TABLE objects_sensor_data_files_bridge ( 
    uid                  SERIAL PRIMARY KEY 
                                 NOT NULL,
    uid_sensor_data_files INTEGER NOT NULL
                                  REFERENCES sensor_data_files ( uid ) ON DELETE CASCADE,
    uid_instance_objects  INTEGER NOT NULL
                                  REFERENCES instance_objects ( uid ) ON DELETE CASCADE,
     UNIQUE (uid_sensor_data_files, uid_instance_objects)                       

);

CREATE TABLE collections_samples_bridge (
    uid                  SERIAL PRIMARY KEY 
                                 NOT NULL,
    uid_collections     INTEGER NOT NULL
                                REFERENCES collections ( uid ) ON DELETE CASCADE,
    uid_samples         INTEGER NOT NULL
                                REFERENCES samples ( uid ) ON DELETE CASCADE
    UNIQUE (uid_collections, uid_samples)                       
                                
);

/*








indices








*/

CREATE INDEX full_path_sensor_data_files ON sensor_data_files ( 
    name
);

CREATE INDEX full_path_truth_files ON truth_files ( 
    name
);

CREATE INDEX object_name ON platonic_objects ( 
    name
);

CREATE INDEX string_id ON tags ( 
    name
);

CREATE INDEX shard_id ON shards (
    name
);


/* staging table */

CREATE TABLE staging (
    typ     TEXT,
    a1     TEXT,
    a2     TEXT,
    a3     TEXT,
    a4     TEXT,
    a5     TEXT,
    a6     TEXT,
    a7     TEXT,
    a8     TEXT,
    a9     TEXT,
    a10     TEXT,
    a11     TEXT,
    a12     TEXT,
    a13     TEXT,    
    a14    TEXT,
    a15     TEXT,
    a16     TEXT,
    a17     TEXT,
    a18     TEXT,
    a19     TEXT,
    a20     TEXT
);

CREATE EXTENSION sslinfo;

CREATE OR REPLACE FUNCTION collateMegaCsv() RETURNS void AS
$body$
BEGIN
       
    /* base csvs - independent */
    
    RAISE NOTICE 'importing shards';
    INSERT INTO shards (description,name,datenum) 
        SELECT a1,a2,CAST (a3 as DOUBLE PRECISION) FROM 
            staging 
        WHERE typ='shds';
    DELETE FROM staging WHERE typ='shds';


    RAISE NOTICE 'importing platforms';
    INSERT INTO platforms (description,name,attributes_blob) 
        SELECT a1,a2,a3 FROM 
            staging 
        WHERE typ='plts'; 
    DELETE FROM staging WHERE typ='plts';

    RAISE NOTICE 'importing platonic_objects';
    INSERT INTO platonic_objects (content,purpose,description,name,major_axis,minor_axis,props_blob) 
        SELECT a1,a2,a3,a4,CAST (a5 as DOUBLE PRECISION),CAST (a6 as DOUBLE PRECISION),a7 FROM 
            staging 
        WHERE typ='pltob';
    DELETE FROM staging WHERE typ='pltob';

    RAISE NOTICE 'importing sites';
    INSERT INTO sites (description,name) 
        SELECT a1,a2 FROM 
            staging 
        WHERE typ='sts';
    DELETE FROM staging WHERE typ='sts';

    RAISE NOTICE 'importing tags';    
    INSERT INTO tags (name) 
        SELECT a1 FROM 
            staging 
        WHERE typ='tgs';
    DELETE FROM staging WHERE typ='tgs';

    /* base csvs - dependent */

    RAISE NOTICE 'importing regions';      
    INSERT INTO regions (description,name,srid,bounding_polygon_blob,uid_sites) 
        SELECT a1,a2,a3,a4,uid FROM 
            (SELECT * FROM staging WHERE typ='rgs') AS stg INNER JOIN sites 
            ON sites.name = stg.a5;
    DELETE FROM staging WHERE typ='rgs';

    /* dependent */ 

    RAISE NOTICE 'importing truth_files';      
    INSERT INTO truth_files (path,name,description,md5,uid_regions) 
        SELECT a1,a2,a3,a4,uid FROM 
            (SELECT * FROM staging WHERE typ='tfs') AS stg INNER JOIN regions 
            ON regions.name = stg.a5;
    DELETE FROM staging WHERE typ='tfs';

    RAISE NOTICE 'importing sensor_data_files';      
    INSERT INTO sensor_data_files (path,name,type,description,md5,bounding_polygon_blob,uid_platforms,uid_regions) 
        SELECT a1,a2,a3,a4,a5,a6,platforms.uid,regions.uid FROM 
            (SELECT * FROM staging where typ='sdfs') as stg INNER JOIN platforms ON stg.a7=platforms.name
                INNER JOIN regions ON stg.a8=regions.name;
    DELETE FROM staging WHERE typ='sdfs';




    RAISE NOTICE 'importing samples';      
    INSERT INTO samples (description,name,uid_shards,uid_regions,uid_truth_files) 
        SELECT a1,a2,shards.uid,regions.uid,truth_files.uid FROM 
            (SELECT * FROM staging where typ='samp') as stg INNER JOIN shards ON stg.a3=shards.name
                INNER JOIN regions ON stg.a4=regions.name
                    INNER JOIN truth_files ON (stg.a5=truth_files.name AND stg.a6=truth_files.path);
    DELETE FROM staging WHERE typ='samp';

    RAISE NOTICE 'importing collections';      
    INSERT INTO collections (description,name,id,uid_shards) 
        SELECT a1,a2,a3,shards.uid FROM 
            (SELECT * FROM staging where typ='cols') as stg INNER JOIN shards ON stg.a4 = shards.name;
    DELETE FROM staging WHERE typ='cols';

    /* bridges */

    RAISE NOTICE 'importing collections_samples_bridges';      
    INSERT INTO collections_samples_bridge (uid_collections,uid_samples) 
        SELECT collections.uid,samples.uid FROM 
            (SELECT * FROM staging where typ='clsb') as stg INNER JOIN collections 
            ON (stg.a1=collections.id AND (SELECT shards.uid FROM shards where shards.name = stg.a2)=collections.uid_shards)
                INNER JOIN samples
                ON (stg.a3 = samples.name AND (SELECT shards.uid FROM shards where shards.name = stg.a4)=samples.uid_shards);
    DELETE FROM staging WHERE typ='clsb';


    RAISE NOTICE 'importing sensor_data_files_samples_bridge';      
    INSERT INTO sensor_data_files_samples_bridge (uid_sensor_data_files,uid_samples) 
        SELECT sensor_data_files.uid,samples.uid FROM 
            (SELECT * FROM staging where typ='sdfsb') as stg INNER JOIN sensor_data_files 
            ON (stg.a1 = sensor_data_files.path AND stg.a2 = sensor_data_files.name)
                INNER JOIN samples
                ON (stg.a3 = samples.name AND (SELECT shards.uid FROM shards where shards.name = stg.a4)=samples.uid_shards);
    DELETE FROM staging WHERE typ='sdfsb';


    RAISE NOTICE 'importing objects_tags_bridge';      
    INSERT INTO objects_tags_bridge (uid_tags,uid_objects) 
        SELECT tags.uid,platonic_objects.uid FROM 
        (SELECT * FROM staging where typ='obtgs') as stg  INNER JOIN tags ON stg.a1 = tags.name
            INNER JOIN platonic_objects ON stg.a2 = platonic_objects.name;
    DELETE FROM staging WHERE typ='obtgs';

END 
$body$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION truncate_tables() RETURNS void AS
$body$
BEGIN
    -- RAISE NOTICE '%', 
    EXECUTE 
    (SELECT 'TRUNCATE TABLE '
        || string_agg(quote_ident(schemaname) || '.' || quote_ident(tablename), ', ')
        || ' RESTART IDENTITY CASCADE'
    FROM   pg_tables
    WHERE  schemaname = 'public'
    );
END
$body$
LANGUAGE plpgsql;




