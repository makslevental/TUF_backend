classdef maxPlatform < tuf.db.maxEntity
    % A Platform represents a sensor platform used to collect samples.  
    %
    % See also TUF.DB.ENTITY, TUF.DB.SAMPLE, TUF.GET_PLATFORM
    properties (Dependent)        
        samples
        sample_sids
        attributes % Struct whose fields are the platforms's attributes. 
    end
    
    methods   
        function self = maxPlatform(varargin)
            varargin = [varargin, {'plats'}];
            self = self@tuf.db.maxEntity(varargin{:});
        end
        
        function v = get.samples(self)
            v = self.maxgetsamps;
        end
        
        function v = get.sample_sids(self)
            samps = self.maxgetsamps;
            if ~isempty(samps) v = {samps.sid};
            else v = [];
            end
        end
        
        function v = get.attributes(self)
            strarr = char(self.getter('AttributesBlob'));  
            if isempty(strarr)
                v = [];
            else
		    % i don't remember why this should be an evalc instead of just an assignment
		    % but there's a reason (otherwise i wouldn't have done that duh).
        evalc(['arr = ' strarr]);
 		    % the getArrayFromByteSteam call is an undocumented matlab method (that is probably a java call)
		    % that de-serializes the byte stream (chars) stored in the postgres db back to a matlab array
                       v = getArrayFromByteStream(uint8(arr)); 
            end
       end
        
   end
  
   methods(Static)
        function v = maxgetalluids
            v = tuf.db.maxEntity.getterwithargs('plats','All');
        end
    end
end
