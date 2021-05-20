package io.gamzagamza.svnkit.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.gamzagamza.svnkit.utils.SvnUtils;
import io.gamzagamza.svnkit.vo.SvnConnectionVO;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

@Service
public class SvnkitServiceImpl implements SvnkitService {

	public String svnConnectionTest(SvnConnectionVO svnConnectionVO) {
		if(SvnUtils.svnConnectionTest(svnConnectionVO)) {
			return "success";
		} else {
			return "fail";
		}
	}
	
	public String svnConnection(SvnConnectionVO svnConnectionVO) {
		if(SvnUtils.svnConnectionTest(svnConnectionVO)) {
			ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
			HttpSession session = servletRequestAttributes.getRequest().getSession();
			
			session.setAttribute("svnConnection", svnConnectionVO);
			return "success";
		} else {
			return "fail";
		}
	}
	
	public String svnLastRevision() throws SVNException {
		SVNRepository repository = SvnUtils.getRepository();
		
		return Long.toString(repository.getLatestRevision());
	}
	
	public List<String> getDeduplicationFilePath(Long startRevision, Long endRevision) throws SVNException {
		return SvnUtils.getDeduplicationFilePathList(startRevision, endRevision);
	}
	
	public void batDownload(Long startRevision, Long endRevision, List<String> projectList, List<String> pathList, HttpServletResponse response) throws SVNException {
		List<String> deduplicationFilePathList = SvnUtils.getDeduplicationFilePathList(startRevision, endRevision);
		List<String> targetFilePathList = SvnUtils.getTargetPathList(deduplicationFilePathList, projectList, pathList);
		
		StringBuilder sb = new StringBuilder();
		
		targetFilePathList.forEach(filePath -> {
			sb.append("copy " + filePath.replace("/", "\\") + " D:\\download\\");
			sb.append("\n");
		});
		
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename=copy.bat;");
		response.setContentLength(sb.toString().getBytes().length);
		
		try {
			response.getOutputStream().write(sb.toString().getBytes());
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
