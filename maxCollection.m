classdef maxCollection < tuf.db.maxEntity
    % A Collection is a set of Samples for use in TUF experiments.
    % All Collections in the database are listed by the TUF GUI using their
    % descriptions. 
    %
    % While TUF collections are often associated with actual data
    % collection activities, the terms should not be confused.
    %
    % See also TUF.DB.ENTITY, TUF.DB.SAMPLE, TUF.GET_COLLECTION_ENTRY,
    % TUF.LIST_COLLECTIONS, TUF.LIST_COLLECTIONS_LIKE, TUF.LIST_SAMPLES_IN
    
    % DB object in java owns
    % public static Collections cols;
    % public static Shards shrds;
    % public static Tags tgs;
    % public static Sites sts;
    % public static Regions regs;
    % public static TruthFiles truthfs;
    % public static SensorDataFiles sdfs;
    % public static Samples samps;
    % public static Platforms plats;
    % public static PlatonicObjects platobjs;
    
    properties (Dependent)
        samples % Array of Samples in this collection     
        sample_sids % Array of Sample SIDs in this collection
        shard % tuf.db.Shard object containing this.
        shard_id % The shard ID. The part of the sid after the '@' mark.
        old_sid % this is a hack that enables identifying .... something? i forget
    end
    methods
        % constructor that calls constructor in base class maxEntity
    	function self = maxCollection(varargin)
            varargin = [varargin, {'cols'}];
	    % this calls the base class constructor with the new the new varargin (varargin passed into the maxCollection constructed
	    % with the name of the object pointer in the java DB object associated with the collections table, i.e. cols.
	    % the @ syntax is exactly the syntax for calling the super class constructor
	    % http://www.mathworks.com/help/matlab/matlab_oop/calling-superclass-methods-on-subclass-objects.html
            self = self@tuf.db.maxEntity(varargin{:});
        end
        
        function v = get.samples(self)
            v = self.maxgetsamps;
        end
        
        function v = get.sample_sids(self)
            samps = self.maxgetsamps;
            if ~isempty(samps) v = {samps.sid};
	    	else v = []; % this is to cover a corner case and patchs a bug. which one? i don't remember
            end
        end 
    
        function v = get.shard_id(self)
            v = char(self.getter('ShrdId'));
        end        
        
        function v = get.shard(self)
            shrduid = self.getter('ShrdUID');
            v = self.maker(shrduid,tuf.db.maxShard,@tuf.db.maxShard);
        end
	% this simulates the old understanding of sids. i forgot what i used it for exactly
	% but it's a convenient way to distinguish things
        function v = get.old_sid(self)
            v = [self.id '@' self.shard_id];
        end
    end
   
    methods(Static)
        function v = maxgetalluids
		% note that `cols` is the name of the static member field of the `database_maks_pkg.DB` instance
		% that corresponds to the collections table in the postgres db. this method has the signature it does
		% so as to be completely generic
            v = tuf.db.maxEntity.getterwithargs('cols','All');
        end
    end
end
