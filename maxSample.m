classdef maxSample < tuf.db.maxEntity
    % The base unit of a collection, from which alarms are drawn.
    % Samples are associated with zero or more files (which may represent
    % sensor data, ground truth, etc), and belong to zero or more
    % collections, though a sample with no files or in no collections is
    % unlikely to be useful. If a sample is taken from some geographical
    % region, it will also be associated with a Region.
    %
    % Note that the relationship between Samples and Files is many-to-many.
    % Sensor data files are usually one-to-one with samples, but certain
    % files (such as airborne data) may be used by several samples.
    %
    % See also TUF.DB.ENTITY, TUF.DB.FILE, TUF.DB.COLLECTION,
    % TUF.DB.REGION, TUF.GET_SAMPLE_ENTRY, TUF.LIST_SAMPLES_IN,
    % TUF.DB.PLATFORM, TUF.GET_PLATFORM
    properties (Dependent)
        files % Array of Files in this sample
        collections % Array of Collections that this sample is a part of
        region % Region where this sample was taken from
        platform % Platform that collected the sample
        
        file_sids  % Array of File SIDs in this sample
        collection_sids  % Array of Collection SIDs that this sample is a part of
        region_sid  % Region SID where this sample was taken from
        platform_sid % Platform that collected the sample
        shard % tuf.db.Shard object containing this.
        shard_id % The shard ID. The part of the sid after the '@' mark.
        old_sid
        
    end
    properties (Dependent, Hidden)
        datafiles % Convenience structure for accessing Files by type
        lsd_filename % Deprecated, generally the MDR filename.
        directory % Deprecated, generally the MDR filepath
        ground_truth_id % Deprecated, the ASC file associated with the sample.

    end
    
    methods
        
        function self = maxSample(varargin)
            varargin = [varargin, {'samps'}];
            self = self@tuf.db.maxEntity(varargin{:});
        end
        
        %samples have sensor data files, which have platforms
        function v = get.platform(self)
            PlatformUid = self.getter('PlatformUid');
            v = tuf.db.maxPlatform(PlatformUid);
        end
        function v = get.region(self)
            RegionUid = self.getter('RegionUid');
            v = tuf.db.maxRegion(RegionUid);
        end
        %last entry(ies) is truthfile uid
        function v = get.files(self)
	% get a list of all sensor data 
            SDFileUids = self.getter('SDFileUids');
		% sdfsctor is the maxFile constructor that assigns the table field to be 'sdfs', corresponding the 
	    % sdfs SensorDataFiles static field in the java instance of `database_maks_pkg.DB`
            
            sdfsctor = @(uid)tuf.db.maxFile(uid,'sdfs');
		% array of `maxFile` corresponding to sensor data files

            v1 = self.maker(SDFileUids,tuf.db.maxFile,sdfsctor);
  		% similarly here.          
            TruthFileUids = self.getter('TruthFileUids');
            truthctor = @(uid)tuf.db.maxFile(uid,'truthfs');
            v2 = self.maker(TruthFileUids,tuf.db.maxFile,truthctor);
            
            v = horzcat(v1,v2);
        end
        
        function v = get.collections(self)
            CollectionUids = self.getter('CollectionUids');
            v = self.maker(CollectionUids,tuf.db.maxCollection,@tuf.db.maxCollection);
        end
        
        function v = get.ground_truth_id(self)
            datafiles = self.datafiles;
            if isfield(datafiles, 'asc')
		    % the one `(1)` arbitrarily picks the first truth thing (because that's where we expect the one associated
		    % with the sample to be in the (potentially) array of truth things

                v = datafiles.asc(1).sid;
            elseif isfield(datafiles, 'truth')
                v = datafiles.truth(1).sid;
            else
                v = 'gt_blank@Base';
            end
        end
        
        function v = get.datafiles(self)
            v = struct();
            for file = self.files
                if isfield(v, file.type)
			% if v already has a file field of this type then add this file to that array
                    v.(file.type)(end+1) = file;
                else
			% otherwise start a new array
                    v.(file.type) = file;
                end
            end
        end
        
        function v = get.lsd_filename(self)
		% the one `(1)` arbitrarily picks the lsd filename to be that of the first in the type array
            datafiles = self.datafiles;
           if isfield(datafiles, 'mdr')
                v = datafiles.mdr(1).filename;
            elseif isfield(datafiles, 'ubin')
                v = datafiles.ubin(1).filename;
                
            elseif isfield(datafiles, 'hhehd_mdr')
                v = datafiles.hhehd_mdr(1).filename;
            elseif isfield(datafiles, 'hhehd_emi')
                v = datafiles.hhehd_emi(1).filename;
            else
                v = self.id;
            end
        end
        
        function v = get.file_sids(self)
            files = self.files;
            if ~isempty(files) v = {files.sid};
            else v = [];
            end
        end
        
        function v = get.collection_sids(self)
            cols = self.files;
            if ~isempty(cols) v = {cols.sid};
            else v = [];
            end
        end
        
        function v = get.region_sid(self)
            reg = self.region;
            if ~isempty(reg) v = reg.sid;
            else v = [];
            end
        end
        
        function v = get.platform_sid(self)
            plat = self.platform;
            if ~isempty(plat) v = plat.sid;
            else v = [];
            end
        end
        
        function v = get.directory(self)
            datafiles = self.datafiles;
            try
                file_fields = fieldnames(self.datafiles);
		% the first cell in the file_fields cell array is typically the one that refers to the data file.
		% it is that binary that we're associating with the sample and therefore it's that files directory
		% we want
                data_file_list = datafiles.(file_fields{1});
                v = data_file_list(1).directory;
            catch any
                v = self.id;
            end
            
        end
        
        function v = get_shard(self)
            shrduid = self.getter('ShrdUID');
            v = self.maker(shrduid,tuf.db.maxShard,@tuf.db.maxShard);
        end
        
        function v = get_shard_id(self)
            v = char(self.getter('ShrdId'));
        end
        
        function v = get.shard(self)
            v = self.get_shard;
        end
        
        function v = get.shard_id(self)
            v = self.get_shard_id;
        end
        
        function v = get.old_sid(self)
            v = [self.id '@' self.shard_id];
        end
        
    end
    methods(Static)
        function v = maxgetalluids
            v = tuf.db.maxEntity.getterwithargs('samps','All');
        end
    end
    
end
