classdef maxShard < tuf.db.maxEntity
    
    properties (Dependent)
        platforms % Array of all Platform entries in this shard
        sites % Array of all Site entries in this shard
        regions % Array of all Region entries in this shard
        samples % Array of all Sample entries in this shard
        collections % Array of all Collectio entries in this shard
        files % Array of all File entries in this shard
        objinfo % Array of all ObjInfo entries in this shard
    end
    
    methods
        
        function self = maxShard(varargin)
            varargin = [varargin, {'shrds'}];
            self = self@tuf.db.maxEntity(varargin{:});
        end
        
        function v = get.platforms(self)
            PlatformUids = self.getter('PlatformUids');
            v = self.maker(PlatformUids,tuf.db.maxPlatform,@tuf.db.maxPlatform);
        end
        
        function v = get.sites(self)
            SiteUids = self.getter('SiteUids');
            v = self.maker(SiteUids,tuf.db.maxSite,@tuf.db.maxSite);
        end
        
        function v = get.regions(self)
            RegionUids = self.getter('RegionUids');
            v = self.maker(RegionUids,tuf.db.maxRegion,@tuf.db.maxRegion);
        end
        
        function v = get.samples(self)
            v = self.maxgetsamps;
        end
        
        function v = get.collections(self)
            CollectionUids = self.getter('CollectionUids');
            v = self.maker(CollectionUids,tuf.db.maxCollection,@tuf.db.maxCollection);
        end
        % there are two types of files: truth file and sensor data files. this
	% return both. 
        function v = get.files(self)
		% get a list of all sensor data uids
            SDFileUids = self.getter('SDFileUids');
	    % sdfsctor is the maxFile constructor that assigns the table field to be 'sdfs', corresponding the 
	    % sdfs SensorDataFiles static field in the java instance of `database_maks_pkg.DB`
            sdfsctor = @(uid)tuf.db.maxFile(uid,'sdfs');
	    % array of `maxFile` corresponding to sensor data files
            v1 = self.maker(SDFileUids,tuf.db.maxFile,sdfsctor);
	    % similarly here
            TruthFileUids = self.getter('TruthFileUids');
            truthctor = @(uid)tuf.db.maxFile(uid,'truthfs');
            v2 = self.maker(TruthFileUids,tuf.db.maxFile,truthctor);
            v = horzcat(v1,v2);
        end
        
        function v = get.objinfo(self)
            v = [];
            tuf.log('get.objinfo on Shard is super deprecated');
        end
    end
    methods(Static)
        function v = maxgetalluids
            v = tuf.db.maxEntity.getterwithargs('shrds','All');
        end
        
        function v = getUID(name)
            v = tuf.db.maxEntity.getterwithargs('shrds','UID',name);
        end
        
    end
end
