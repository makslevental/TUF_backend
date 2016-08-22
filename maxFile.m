classdef maxFile < tuf.db.maxEntity
    % File entities represent data files containing data pertaining to
    % Samples. 
    %
    % The directory property should have Unix-style file separators -
    % MATLAB doesn't mind these even in Windows. 
    % 
    % The type property is by default the filename extension, but it may be
    % freely specified in the shard's YAML file. This makes it easier to
    % deal with situations in which several sensors all use the same
    % non-descriptive extension (say, '.DAT', or '.MDD') and you wish to be
    % able to distinguish between them.
    %
    % See also: TUF.DB.ENTITY, TUF.DB.SAMPLE
    properties (Dependent)
        directory % Location of the File, relative to Data root.
        filename % Filename within the directory
        md5 % MD5 hash. For informational purposes - TUF does not check this.
        type % Type of the file. Defaults to the extension.
        samples % Array of Samples associated with this file.
        sample_sids % Array of Sample SIDs associated with this file.
        fullpath % directory concatenated with platform specific file separator concatenated with filename
    end
    
    methods    
     
        function self = maxFile(varargin)
            self = self@tuf.db.maxEntity(varargin{:});
        end
        
        function v = get.directory(self)
            % directory in matlab is path in java
            pathwithwrongseps = char(self.getter('Path'));
	    % the file separator in the postgres db is '/' because i migrated the kendb on my linux machine
	    % for other platforms '/' needs to be replaced by the local file separator (e.g. '\' for windoze)
            v = strrep(pathwithwrongseps,'/',filesep);
        end
        
        function v = get.filename(self)
            v = char(self.getter('Filename'));
        end
        
        function v = get.type(self)
            v = char(self.getter('Type'));
        end
        
        function v = get.samples(self)
            v = self.maxgetsamps;
        end
        
        function v = get.md5(self)
		% this has to be cast to char because the return of self.getter is a java object
            v = char(self.getter('MD5'));
        end
        
        function v = get.sample_sids(self)
            samps = self.maxgetsamps;
            if ~isempty(samps) v = {samps.sid};
       	else v = [];
            end
        end
        
        function v = get.fullpath(self)
            v = [self.directory filesep self.filename];
        end
        
    end
   methods(Static)
        function [sdfsuids,truthfsuids] = maxgetalluids
            sdfsuids = tuf.db.maxEntity.getterwithargs('sdfs','All');
            truthfsuids = tuf.db.maxEntity.getterwithargs('truthfs','All');           
        end
    end
end
   

