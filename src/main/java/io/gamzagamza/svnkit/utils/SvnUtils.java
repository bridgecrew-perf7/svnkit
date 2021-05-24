package io.gamzagamza.svnkit.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import io.gamzagamza.svnkit.vo.SvnConnectionVO;
import io.gamzagamza.svnkit.vo.SvnLogVO;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class SvnUtils {

	
	public static boolean svnConnectionTest(SvnConnectionVO svnConnectionVO) {
		SVNRepositoryFactoryImpl.setup();
        SVNRepository repository;
		try {
			repository = SVNRepositoryFactoryImpl.create(SVNURL.parseURIDecoded(svnConnectionVO.getSvnUrl()));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnConnectionVO.getSvnUser(), svnConnectionVO.getSvnPassword());
	        repository.setAuthenticationManager(authManager);
	        repository.testConnection();
	        return true;
		} catch (SVNAuthenticationException e) {
			return false;
		} catch (SVNException e) {
			return false;
		}
	}
	
	public static SVNRepository svnConnection(SvnConnectionVO svnConnectionVO) {
		SVNRepositoryFactoryImpl.setup();
        SVNRepository repository;
		try {
			repository = SVNRepositoryFactoryImpl.create(SVNURL.parseURIDecoded(svnConnectionVO.getSvnUrl()));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnConnectionVO.getSvnUser(), svnConnectionVO.getSvnPassword());
	        repository.setAuthenticationManager(authManager);
	        repository.testConnection();
	        return repository;
		} catch (SVNAuthenticationException e) {
			return null;
		} catch (SVNException e) {
			return null;
		}
	}
	
	public static List<SvnLogVO> getLogs(Long startRevision, Long endRevision) throws SVNException {
		SVNRepository repository = getRepository();
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Collection<SVNLogEntry> logEntries = repository.log(new String[]{""}, null, startRevision, endRevision, true, true);
        List<SvnLogVO> logs = new ArrayList<>();

        Iterator iterator = logEntries.iterator();
        while(iterator.hasNext()) {
            SVNLogEntry logEntry = (SVNLogEntry) iterator.next();

            SvnLogVO log = new SvnLogVO();

            log.setRevision(Long.toString(logEntry.getRevision()));
            log.setAuthor(logEntry.getAuthor());
            log.setDate(simpleDateFormat.format(logEntry.getDate()));
            log.setMessage(logEntry.getMessage());

            Map<String, SVNLogEntryPath> changedPaths = logEntry.getChangedPaths();
            List<String> filePathList = new ArrayList<>();
            for (String key : changedPaths.keySet()) {
                SVNLogEntryPath svnLogEntryPath = changedPaths.get(key);
                filePathList.add(svnLogEntryPath.getPath());
            }
            log.setFilePathList(filePathList);

            logs.add(log);
        }
        
        return logs;
	}
	
	public static List<String> getDeduplicationFilePathList(Long startRevision, Long endRevision, String regex) throws SVNException {
		AntPathMatcher pathMatcher = new AntPathMatcher();

		SVNRepository repository = getRepository();
		
		Set<String> deduplicationFileList = new HashSet<>();
        Collection<SVNLogEntry> logEntries = repository.log(new String[]{""}, null, startRevision, endRevision, true, true);

        Iterator iterator = logEntries.iterator();
        while(iterator.hasNext()) {
            SVNLogEntry logEntry = (SVNLogEntry) iterator.next();

            Map<String, SVNLogEntryPath> changedPaths = logEntry.getChangedPaths();
            for (String key : changedPaths.keySet()) {
                SVNLogEntryPath svnLogEntryPath = changedPaths.get(key);
                if(pathMatcher.match(regex, svnLogEntryPath.getPath())) {
					deduplicationFileList.add(svnLogEntryPath.getPath());
				}
            }
        }

        return new ArrayList<>(deduplicationFileList);
	}
	
	public static SVNRepository getRepository() {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		HttpSession session = servletRequestAttributes.getRequest().getSession();
		
		SvnConnectionVO svnConnectionVO = (SvnConnectionVO)session.getAttribute("svnConnection");
		
		SVNRepositoryFactoryImpl.setup();
        SVNRepository repository;
		try {
			repository = SVNRepositoryFactoryImpl.create(SVNURL.parseURIDecoded(svnConnectionVO.getSvnUrl()));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnConnectionVO.getSvnUser(), svnConnectionVO.getSvnPassword());
	        repository.setAuthenticationManager(authManager);
	        repository.testConnection();
	        return repository;
		} catch (SVNAuthenticationException e) {
			return null;
		} catch (SVNException e) {
			return null;
		}
	}
}
