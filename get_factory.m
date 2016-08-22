function factory = get_factory(setup_only)

persistent maksdb 
persistent checks

address = 127.0.0.1
% check connectivity
[~, projname] = tuf_project;

    % local function
    function out = checkorrebuild()
        retryCount = 0;
        while retryCount < 10
            % first time throws
            try
                isval = maksdb.checkConnection();
            catch e
                isval = false;
            end
            if ~isval
                try
                    tuf.log({'display',true,'color','error'}, ['(Re)trying connection. Attempt: ' num2str(retryCount)]);
                    maksdb = database_maks_pkg.DB(address,lower(projname));         
                catch e
                    retryCount = retryCount + 1;
                    if retryCount > 9
                        choice = questdlg(['(Re)connection failed.' 'Do you want to keep trying?'], ...
                            'Connection Error', ...
                            'Yes','No','Yes');
                        switch choice
                            case 'Yes'
                                retryCount = 0;
                            case 'No'
                                throw(e)
                        end
                    end
                end
            else
                break
            end
        end
        out = maksdb;
    end



if isempty(maksdb)
    pkgpath = fileparts(mfilename('fullpath'));
    classpath = javaclasspath;
    MaksDBpath = fullfile(pkgpath, 'MaksDB.jar');
    postgrespath = fullfile(pkgpath, 'postgresql-9.4-1201.jdbc41.jar');
    allpaths = {MaksDBpath,postgrespath};
    if ~all(ismember(allpaths, classpath))
        javaaddpath(allpaths);
    end
    if nargin < 1 || ~setup_only
        %stupid thing won't load classes compiled against newer
        %jdks won't warn you at all, will just say 'no class found'
        
        org.postgresql.Driver();
        checks = 0;
        maksdb = checkorrebuild();
        
    end
else
    maksdb = checkorrebuild();
end

factory = maksdb;

end


    
