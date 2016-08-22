classdef maxShrapnelDatabase < handle
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
    % lol (so meta)
    
    % these are placeholder fields. an instance of maxShrapnelDatabase doesn't actually ever have
    % any of these fields but to write a gettor you have to have a corresponding field. regardless
    % the semantics of these pseudo fields should be obvious
    properties(Dependent)
        files
        regions
        samples
        collections
        sites
        objinfo
        platforms
        shards
        shard_ids
    end
    
    % hack in order to have a static field maksdb that's constructed on instantiation of `this`, the actual link to the java instance of the database_maks_pkg.DB object
%     methods (Static)
%         function out=maksdb
%             persistent mksdb;
%             if isempty(mksdb)
%                 try
%                     mksdb is an instance of `database_maks_pkg.DB`
%                     mksdb = tuf.db.get_factory(false);
%                 catch e
%                     importExcursionShards throws an exception when shards are
%                     malformed in the csvs
%                     tuf_error(e.message);
%                 end
%             end
%             out = mksdb;
%         end
%     end
    
    methods
        
        function self = maxShrapnelDatabase
            % hack. running self.maksdb does the maksdb thing (look up) but i don't need
            % the return here in the constructor
%             try
                % unneeded instance of `database_maks_pkg.DB`
                tuf.db.get_factory(false);
%             catch e
                % importExcursionShards throws an exception when shards are
                % malformed in the csvs
%                 tuf_error(e.message);
%                 throw(e)
%             end
            self = self;
        end
        
        function load_shards(self,clear_old,shardfiles)
            tuf_error('deprecated');
        end
        
        function reload_whole_db(self,clear_old,shardfiles)
            tuf_error('deprecated'); 
        end
        
        %takes shard name arg (id in matlab means name in java)
        function entry = shard(self,ids)
            % if ids arg isn't passed in then just fetch all uids of all shards (i.e. all uids of
            % rows in the Shards table).
            if ~exist('ids', 'var')
                ids = tuf.db.maxShard.maxgetalluids;
            else
                ids = double(tuf.db.maxShard.getUID(ids));
            end
            
            % creat a maxShard stub just so static getall method works right (it needs uid and
            % table - the 1 here is irrelevant but 'shrds' identifies the field of `database_maks_pkg.DB`
            % that corresponds to the Shards table in postgres).
            stub = tuf.db.maxShard(1,'shrds');
            % makerEntries takes as its third argument a constructor, so i pass in the function handle
            % to the maxShard constructor
            entry = self.makeEntries(ids,stub,@tuf.db.maxShard);
        end
        
        % useless so far. just returns something so code isn't broken
        function [ok,errors,warnings] = ready_all(self)
            ok=true;
            errors = {};
            warnings = {};
        end
        
        function report_errors(self,shard_id,errclass,msg,varargin)
            tuf_error('maxShrapnelDatabase stub');
        end
        
        function report_warnings(self,shard_id,warnclass,msg,varargin)
            tuf_error('maxShrapnelDatabase stub');
        end
        
        function disp(self)
            fprintf('[Shrapnel Database with %d shards]\n', numel(self.shard_ids));
        end
        
        
        % generate list of sids (and who cares about arguments passed in)
        function sids = list_platforms(~)
            % the way this works is simple. `cellfun` applies a function (the first arg to `cellfun`) to each element of a cell array (the second arg)
            % so the function i'm going to apply to each element of `num2cell(tuf.db.maxPlatform.maxgetalluids)` is
            % `@(x)[num2str(x) '@plats']`, which is the anonymous function that just turns the element (which is a uid, i.e. integer) into a string
            % and concatenates `@plats` to it. for example [num2str(1) '@plats'] = '1@plats'. finally `'UniformOutput', false` is some matlab nonsense
            % that i never bothered to investigate but is required here.
            sids = cellfun(@(x)[num2str(x) '@plats'],num2cell(tuf.db.maxPlatform.maxgetalluids),'UniformOutput',false);
        end
        function sids = list_samples(~)
            sids = cellfun(@(x)[num2str(x) '@samps'],num2cell(tuf.db.maxSample.maxgetalluids),'UniformOutput',false);
        end
        function sids = list_regions(~)
            sids = cellfun(@(x)[num2str(x) '@regs'],num2cell(tuf.db.maxRegion.maxgetalluids),'UniformOutput',false);
        end
        function sids = list_sites(~)
            sids = cellfun(@(x)[num2str(x) '@sts'],num2cell(tuf.db.maxSite.maxgetalluids),'UniformOutput',false);
        end
        function sids = list_collections(~)
            sids = cellfun(@(x)[num2str(x) '@cols'],num2cell(tuf.db.maxCollection.maxgetalluids),'UniformOutput',false);
        end
        
        function sids = list_files(~)
            % files come in two flavors: truth files and sensor data files.
            [sdfsuids,truthfsuids] = tuf.db.maxFile.maxgetalluids;
            sdfssids = cellfun(@(x)[num2str(x) '@sdfs'],num2cell(sdfsuids),'UniformOutput',false);
            truthfssids = cellfun(@(x)[num2str(x) '@truthfs'],num2cell(truthfsuids),'UniformOutput',false);
            sids = [sdfssids;truthfssids];
        end
        
        function sids = list_objinfo(~)
            sids = cellfun(@(x)[num2str(x) '@platobjs'],num2cell(tuf.db.maxObjInfo.maxgetalluids),'UniformOutput',false);
        end
        function entries = get_platforms(self,sids)
            if ~exist('sids', 'var'), sids = []; end
            % shrapgetter is essentially the same as getter
            entries = self.shrapgetter(sids,@self.list_platforms,tuf.db.maxPlatform);
        end
        function entries = get_samples(self,sids)
            if ~exist('sids', 'var'), sids = []; end
            entries = self.shrapgetter(sids,@self.list_samples,tuf.db.maxSample);
        end
        function entries = get_regions(self,sids)
            if ~exist('sids', 'var'), sids = []; end
            entries = self.shrapgetter(sids,@self.list_regions,tuf.db.maxRegion);
        end
        function entries = get_sites(self,sids)
            if ~exist('sids', 'var'), sids = []; end
            entries = self.shrapgetter(sids,@self.list_sites,tuf.db.maxSite);
        end
        function entries = get_collections(self,sids)
            if ~exist('sids', 'var'), sids = []; end
            entries = self.shrapgetter(sids,@self.list_collections,tuf.db.maxCollection);
        end
        
        function entries = get_objinfo(self,sids)
            if ~exist('sids', 'var'), sids = []; end
            entries = self.shrapgetter(sids,@self.list_objinfo,tuf.db.maxObjInfo);
        end
        
        
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        % files are special because it's necessary to know whether they're
        % truth files or sensor data files
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        function entries = get_files(self,sids)
            if ~exist('sids', 'var')
                sids = self.list_files;
            end
            sids = cellstr(sids);
            uids = [];
            tables = {};
            for i=1:length(sids)
                [uids(i),tables{i}] = self.unssid(sids{i});
            end
            entries = self.makeFileEntries(uids,tables,tuf.db.maxFile,@tuf.db.maxFile);
        end
        
        function sids = list_objinfo_by_property(~,prop,value)
            switch prop
                case 'content'
                    sids = cellfun(@(x)[num2str(x) '@'],num2cell(tuf.db.maxObjInfo.maxgetcontent(value)),'UniformOutput',false);
                case 'name'
                    sids = cellfun(@(x)[num2str(x) '@'],num2cell(tuf.db.maxObjInfo.maxgetname(value)),'UniformOutput',false);
                case 'notes'
                    tuf_error('not implemented')
                case 'purpose'
                    sids = cellfun(@(x)[num2str(x) '@'],num2cell(tuf.db.maxObjInfo.maxgetpurpose(value)),'UniformOutput',false);
                case 'tag'
                    sids = cellfun(@(x)[num2str(x) '@'],num2cell(tuf.db.maxObjInfo.maxgettag(value)),'UniformOutput',false);
            end
        end
        
        %similar fxn as previous fxn but handles cell array of object names
        %All props are not available in this fxn yet
        function sids = list_objsinfo_by_property(~,prop,value)
            switch prop
                case 'name'
                    sids = cellfun(@(x)[num2str(x) '@'],num2cell(tuf.db.maxObjInfo.maxgetnames(value)),'UniformOutput',false);
            end
        end
        
        function entries = get.platforms(self)
            entries = self.get_platforms;
        end
        function entries = get.samples(self)
            entries = self.get_samples;
        end
        function entries = get.regions(self)
            entries = self.get_regions;
        end
        function entries = get.sites(self)
            entries = self.get_sites;
        end
        function entries = get.collections(self)
            entries = self.get_collections;
        end
        function entries = get.files(self)
            entries = self.get_files;
        end
        function entries = get.objinfo(self)
            entries = self.get_objinfo;
        end
        function entries = get.shards(self)
            entries = self.shard;
        end
        
        %% Shard ID list (uid list not sid list!)
        function ids = get.shard_ids(self)
            ids = tuf.db.maxShard.maxgetalluids;
        end
        
        
        
        
        
    end
    
    methods (Access=private)
        % the classEntry argument is a stub while ctorPtr is a function handle to the constructor
        % for that same class
        function classEntry = makeEntries(~,uids,classEntry,ctorPtr)
            classEntry = classEntry([]);
            for i=1:length(uids)
                classEntry(i) = ctorPtr(uids(i));
            end
        end
        % file entries are different because there are two types of files (truth files and sensor data files)
        function classEntry = makeFileEntries(~,uids,tables,classEntry,ctorPtr)
            classEntry = classEntry([]);
            for i=1:length(uids)
                classEntry(i) = ctorPtr(uids(i),tables{i});
            end
        end
        % i don't know why i decided to call this function `shrapgetter` but what it does
        % is construct arrays of max* objects from a list of sids or simply returns all.
        % ie if a list of sids for object of type `type` is passed in then an entries
        % will be an array of those objects. otherwise a `lister` should be passed in and
        % will be used to fetch all sids
        function entries = shrapgetter(self,sids,lister,type)
            if isempty(sids);
                sids = lister();
            end
            % because matlab sucks and ken is an idiot the system
            % sometimes passes in char arrays of strings and sometimes
            % passes in cell arrays of strings. cellstr() is idempotent so
            % this works but it's fucking stupid.
            sids = cellstr(sids);
            uids = [];
            for i=1:length(sids)
                uids(i) = self.unssid(sids{i});
            end
            % typector becomes a function handle to the constructor of the type (max* class)
            evalc(['typector = @' class(type)]);
            entries = self.makeEntries(uids,type,typector);
        end
        
        
        function [uid,table] = unssid(self,sid)
            [id,table] = unsid(sid);
            uid = str2num(id);
        end
    end
end

