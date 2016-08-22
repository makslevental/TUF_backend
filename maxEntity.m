classdef maxEntity
    % this is the base class for all max* classes. if any of the methods are strange
    % or convoluted it's so that they are effectively heritable and generic. further
    % all the interfacing with java happens here.
    
    % the basic structure of this class hierarchy (sitting atop of the java class hierarchy)
    % is each matlab class corresponds to an instance of a java class. note i said
    % corresponds to an instance of a java class, not a class. an instance of the java object `database_maks_pkg.DB`
    % has static fields like `samps`, `cols`, `tgs`, etc. these are instances of java classes `database_maks_pkg.Samples`,
    % `database_maks_pkg.Collections`, `database_maks_pkg.Tags`, etc. these static fields then correspond to actual postgres db tables.
    % instances of the matlab classes correspond to (essentially) rows in the tables, i.e. one instance of maxSample is a row in the samples table.
    % this isn't completely accurate since samples participate in many to many relationships with collections, for example, and so have entries in
    % that bridge table.

    % class fields that are static by virtue of not being `Dependent`
    properties (Hidden=true)
        % this matches the table object owned by DB class in java layer
        % you might wonder why this is necessary given that each class is in 1-1 correspondence with a member of `database_maks_pkg.DB`.
        % the reason it is is because `maxFile` instances are either sensor data files or truth files (so there's a 1-2 mapping for `maxFile`s).
        % having this field, which is set on construction of maxFile instance, enables query the right table (through the right member
        % field of `database_maks_pkg.DB`.
        table = '' % name of table, as owned by DB object in java
        % this matches the table object owned by DB class in java layer
        % you might wonder why this is necessary given that each class is in 1-1 correspondence with a member of `database_maks_pkg.DB`.
        % the reason it is is because `maxFile` instances are either sensor data files or truth files (so there's a 1-2 mapping for `maxFile`s).
        % having this field, which is set on construction of maxFile instance, enables query the right table (through the right member
        % field of `database_maks_pkg.DB`.
        uid = ''  % uid (row id) in postgres
    end

    
    
    % lots of the properties in this class and others are holdovers from the old system.
    properties (Dependent)
        sid % Shard-qualified Identifier. this is a holdover from the old system.
        description % Descriptive text.
        name% Item name, if any
        id % The local ID. The front part of the sid.%
        
    end
    
    methods
        
        % constructor, private fields. constructor needs to set
        % table so we can distinguish between data files and truth files
        % all subclasses have a constructor that passes in the java class (which itself is associated with a postgres table)
        % the instance is associated with (hard coded by appending the identifier identifying the static field in `database_maks_pkg.DB`
        % instance that corresponds to the table, e.g. 'regs' for the regs static field of `database_maks_pkg.DB` that corresponds to the `Regions`
        % postgres table
        function self = maxEntity(varargin)
            % stupid matlab hack. simulating a default constructor
            if length(varargin)<2
                self = self;
                return
            end
            self.uid = varargin{1};
            self.table = varargin{2};
        end
        
        
        function tf = eq(self, other)
            tf = isequal(class(self), class(other)) && isequal(self.uid, other.uid);
        end
        
        function tf = ne(self, other)
            tf = ~eq(self,other);
        end
        
        function v = get.id(self)
            % this has to be cast to char because what comes back from getters is typically a java type
            v = char(self.getter('Id'));
        end
        
        function v = get.name(self)
            v = char(self.getter('Name'));
            % some things didn't have names when migrated from the old system.
            if ~isempty(v)
                % i don't remember if this is absolutely necessary but it might be a hack to get
                % matlab to cooperate.
                v = v;
            else
                v = self.id;
            end
            
        end
        
        function v = get.description(self)
            v = self.name;
        end
        
        function v = get.sid(self)
            v = [num2str(self.uid) '@' self.table]; % maybe add shard uid?
        end
        % this is so the constructors can set these fields because matlab is stupid
        % and doesn't let you set member fields unless the class derives from handle
        function self = set.uid(self,v)
            self.uid = v;
        end
        
        function self = set.table(self,v)
            self.table = v;
        end
        
    end
    
    
    methods (Hidden)
        % this is the structify method, i.e. what matlab calls when you do struct(<instance of maxEntity)>).
        function s = struct(self)
            props = properties(self);
            s = struct([]);
            for i=1:numel(self)
                for p = 1:numel(props)
                    pname = props{p};
                    if ~isobject(self(i).(pname))
                        s(i).(pname) = self(i).(pname);
                    end
                end
            end
        end
    end
    
    methods(Access=protected)
        
        % this is only used by Collection,File,Platform,Region,Shard
        % returns samples associated with ____ where blank is one of maxCollection,maxFile,maxRegion,maxShard
        function v = maxgetsamps(self)
            if ~strcmp(class(self),{'tuf.db.maxFile','tuf.db.maxCollection','tuf.db.maxPlatform','tuf.db.maxRegion','tuf.db.maxShard'})
                tuf_error(['Inappropriate get.Samples on ' class(self)]);
            end
            SampleUids = self.getter('SampleUids');
            if isempty(SampleUids)
                % this is only useful for debug
                %tuf.log([class(self) ' uid: ' num2str(self.uid) ' has no samples '],'display',false);
                v = [];
            else
                v = self.maker(SampleUids,tuf.db.maxSample,@tuf.db.maxSample);
            end
            
        end
        
        function v = getter(self,attrib)
            % basic error checking
            if isempty(self.uid)
                tuf_error(['Unidentified ' class(self)]);
            elseif isempty(self.table)
                tuf_error(['Unaffiliated ' class(self)]);
            else
                % if no errors then delegate to completely generic getterwithargs.
                % `attrib` here corresponds directly to suffixes of methods in the java
                % class that look like get____, e.g. getShardUid (so attrib == 'ShardUid')
                % the reason why table has to be passed is because the static method getterwithargs
                % can't access instance fields. then finally self.uid is essentially the row number
                % of the row corresponding to the matlab object instance in the postgres table
                % corresponding to the static member field in `database_maks_pkg.DB`
                v = self.getterwithargs(self.table,attrib,self.uid);
            end
        end
        % returns many entries
        % just basically automates creating lots of a particular instance of a class.
        % just called the constructor for a class over and over again, passing the uids
        function classEntry = maker(self,ids,classEntry,ctorPtr)
            classEntry = classEntry([]);
            for i=1:length(ids)
                classEntry(i) = ctorPtr(ids(i));
            end
        end
        
    end
    
    methods (Static = true, Access = protected)
        % actual point of contact between matlab and java
        % basically calls get____ methods of java classes
        function v = getterwithargs(varargin)
            try
                % grab the instance of `database_maks_pkg.DB`. maksdb is a java object
                maksdb = tuf.db.get_factory(false);
                % run the command `tble = maksdb.samps (for example). this sets tble
                % to be a pointer to the instance of the database_maks_pkg.Samples object that the instance
                % of `database_maks_pkg.DB` owns.
                evalc(['tble = maksdb.' varargin{1}]);
                % define f to be the member method corresponding to the getter, e.g. `f = tble.getShardUid`.
                evalc(['f = @tble.get' varargin{2}]);
                % run f on the arg or args passed to the matlab getter, e.g. f('ap hill') = tble.getShardUid('ap hill')
                v = f(varargin{3:end});
                % edge case. java sometimes returns malformed objects.
                if isempty(v) v = []; end
            catch e
                tuf_error(e.message);
            end
        end
        
    end
end
