classdef maxSite < tuf.db.maxEntity
    % A Site represents a geographic area where samples are collected. It
    % should be less specific than a Region (which is a constituent part of
    % a Site).
    %
    % Other than its description, the only property a Site has is its SRID
    % (spatial reference system ID), which identifies the coordinate system
    % which should be assumed for the site. This value is not yet used.
    %
    % See also TUF.DB.ENTITY, TUF.DB.REGION, TUF.GET_SITE_ENTRY
    properties
        srid = 0 % Spatial Reference System ID. Not yet used.
    end
    properties (Dependent)
        regions % Array of Regions at this Site.
    end
    properties
        region_sids = {} % Array of Region SIDs at this Site.
    end
    
    methods
        
        function self = maxSite(varargin)
            varargin = [varargin, {'sts'}];
	    % this is the syntax for calling the super constructor
    	    % http://www.mathworks.com/help/matlab/matlab_oop/calling-superclass-methods-on-subclass-objects.html	    
	    self = self@tuf.db.maxEntity(varargin{:});
        end
        
        function v = get.regions(self)
            RegionUids = self.getter('RegionUids');
            v = tuf.db.maxRegion;
	    % hack. turns v, which is just a tuf.db.maxRegion object, in a 1x1 array of maxRegion objects.
            v = v([]);
            for i=1:length(RegionUids)
                v(i) = tuf.db.maxRegion(RegionUids(i));
            end
        end
        
        function v = get.region_sids(self)
            regions = self.regions;
            if ~isempty(regions) v = {regions.sid};
    	else v = []; % corner case coverage
            end
        end
        
    end
    % this might not be necessary anymore. i don't remember. 
    methods(Hidden)
    % matlab workspace will continue querying over and over again
        % if this function doesn't return at least something
        function v = get_shard_id_hack(self)
           %  tuf_error('this class doesn''t support getting a shard id');
            v = 'THIS IS NOT A SHARD ID!!! sites don''t support getting a shard id';
        end    
        
        
        % matlab workspace will continue querying over and over again
        % if this function doesn't return at least something
        function v = get_shard_hack(self)
           %  tuf_error('this class doesn''t support getting a shard');
            v = 'THIS IS NOT A SHARD!!! sites don''t support getting a shard';
        end
    end       
    methods(Static)
        function v = maxgetalluids
		% note that `sts`  is the name of the static member field of the `database_maks_pkg.DB` instance
		% that corresponds to the sites table in the postgres db. this method has the signature it does
		% so as to be completely generic
   
            v = tuf.db.maxEntity.getterwithargs('sts','All');
        end
    end
end
