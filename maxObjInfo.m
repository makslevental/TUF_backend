classdef maxObjInfo < tuf.db.maxEntity
    % ObjInfo entities are essentially templates describing classes of
    % objects that one might find in ground truth. Since ground truth has
    % not yet been integrated into the database schema, ObjInfo entities
    % are not yet associated with any other entities. 
    %
    % An ObjInfo entry comes with two types of data - *tags* and
    % *properties*. Tags are a simple set of identifiers that can be used
    % to mark objects. For instance, all object classes representing lengths
    % of wire possess the "wire" tag. 
    % Properties are key/value pairs. The "majoraxis" property possessed by
    % most objects indicates the object's radius as a number. The "content"
    % property indicates the material class - usually "low", "non", or
    % "metal". 
    %
    % Certain properties have convenience accessors and/or special behaviors
    % due to their use with outside truth/scoring formats and programs.
    % Apart from these, there is no pre-determined set of tags or
    % properties - anything in the "tags" key of the YAML entry will be
    % a tag, and any unrecognized key will be interpreted as a property.
    % 
    % See also: TUF.DB.ENTITY, TUF.GET_OBJECT_ENTRY
    properties

        
    end
        
    properties (Dependent)
        content % obj.props.content, or '?'
        purpose % obj.props.purpose, or '?'
        majoraxis % obj.props.majoraxis, or 0        
        minoraxis % obj.props.minoraxis, or 0
        tags % Cell array of lowercase tags
        props % Struct whose fields are the object's properties.
    end
    
    methods
        
        function self = maxObjInfo(varargin)
            varargin = [varargin, {'platobjs'}];
            self = self@tuf.db.maxEntity(varargin{:});
        end
        
        function v = get.content(self)
            v = char(self.getter('Content'));  
        end
        function v = get.purpose(self)
            v = char(self.getter('Purpose'));       
        end
        function v = get.majoraxis(self)
            v = double(self.getter('MajorAxis'));  
        end
        function v = get.minoraxis(self)
            v = double(self.getter('MinorAxis'));           
        end
        
        function v = get.tags(self)
            temp = self.getter('Tags');
            if ~isempty(temp) v = cell(temp)';
            else v = {};
            end
        end
        
        function v = get.props(self)
            strarr = char(self.getter('PropsBlob'));  
        	    % i don't remember why this should be an evalc instead of just an assignment
		    % but there's a reason (otherwise i wouldn't have done that duh).
		    evalc(['arr = ' strarr]);
	    % the getArrayFromByteSteam call is an undocumented matlab method (that is probably a java call)
		    % that de-serializes the byte stream (chars) stored in the postgres db back to a matlab array
            v = getArrayFromByteStream(uint8(arr)); 
        end
        

        
    end    
    methods(Hidden)
   end   
    methods (Static)
        function v = maxgetcontent(value)
            v = tuf.db.maxEntity.getterwithargs('platobjs','UidByContent',value);
        end
        
        function v = maxgetname(value)
            v = tuf.db.maxEntity.getterwithargs('platobjs','UidByName',value);
        end
        
        %gus change for multiple UIDs
        function v = maxgetnames(value)
            v = tuf.db.maxEntity.getterwithargs('platobjs','UidsByName',value);
        end
        
        function v = maxapply_targetfilter(value)
            v = tuf.db.maxEntity.getterwithargs('platobjs','FilteredNames',value);
        end
        
        function v = maxgetpurpose(value)
            v = tuf.db.maxEntity.getterwithargs('platobjs','UidByPurpose',value);
        end
        
        function v = maxgettag(value)
            v = tuf.db.maxEntity.getterwithargs('platobjs','UidByTag',value);
        end
        
        function v = maxgetalluids
            v = tuf.db.maxEntity.getterwithargs('platobjs','All');
        end    
    end
end
