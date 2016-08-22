classdef maxRegion < tuf.db.maxEntity
    % A Region represents a geographic area where samples are collected. It
    % should be more specific than a Site (which is a Region is a part of).
    %
    % See also TUF.DB.ENTITY, TUF.DB.SITE, TUF.DB.SAMPLE, TUF.GET_REGION_ENTRY
    properties
        
    end
    properties (Dependent)
        site
        samples
        site_sid
        sample_sids
    end
    
    methods
        function self = maxRegion(varargin)
            varargin = [varargin, {'regs'}];
            self = self@tuf.db.maxEntity(varargin{:});
        end
        
        function v = get.site(self)
            SiteUid = self.getter('Site');
            v = tuf.db.maxSite(SiteUid);
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
        
        function v = get.site_sid(self)
            site = self.site;
            if ~isempty(site) v = site.sid;
            else v = [];
            end
        end
        
    end
    
    methods(Static)
        function v = maxgetalluids
            v = tuf.db.maxEntity.getterwithargs('regs','All');
        end
    end
end
