package io.gamzagamza.svnkit.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

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
		List<String> targetFilePathList = getTargetPathList(deduplicationFilePathList, projectList, pathList);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();
		String nowStr = simpleDateFormat.format(now);
		StringBuilder sb = new StringBuilder();

		targetFilePathList.forEach(filePath -> {
			String replacePath = filePath.substring(0, filePath.lastIndexOf("/"));
			String lastReplacePath = replacePath.substring(replacePath.indexOf("WEB-INF")).replace("/", "\\");

			sb.append("mkdir D:\\targetcopy\\" + nowStr + "\\" + lastReplacePath);
			sb.append("\n");
		});

		targetFilePathList.forEach(filePath -> {
			String replacePath = filePath.substring(0, filePath.lastIndexOf("/"));
			String lastReplacePath = replacePath.substring(replacePath.indexOf("WEB-INF")).replace("/", "\\");

			sb.append("copy " + filePath.replace("/", "\\") + " D:\\targetcopy\\" + nowStr + "\\" + lastReplacePath);
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
			e.printStackTrace();
		}
	}

	public static List<String> getTargetPathList(List<String> deduplicationFilePathList, List<String> projectList, List<String> pathList) {
		final String MAIN_DIR = "/WEB-INF/classes";
		final String JSP_DIR = "/WEB-INF/jsp";

		List<String> targetFilePathList = new ArrayList<>();

		for(String deduplicationFilePath : deduplicationFilePathList) {
			String targetFilePath = "";
			String type = deduplicationFilePath.split("\\.")[1];

			if(type.equals("java")) {
				targetFilePath = deduplicationFilePath.substring(deduplicationFilePath.indexOf("java") + "java".length());
				targetFilePathList.add(ifPath(deduplicationFilePath, projectList, pathList) + MAIN_DIR + targetFilePath.replace(".java", ".class"));
			} else if(type.equals("xml") || type.equals("properties")) {
				targetFilePath = deduplicationFilePath.substring(deduplicationFilePath.indexOf("resources") + "resources".length());
				targetFilePathList.add(ifPath(deduplicationFilePath, projectList, pathList) + MAIN_DIR + targetFilePath);
			} else if(type.equals("jsp")) {
				targetFilePath = deduplicationFilePath.substring(deduplicationFilePath.indexOf("jsp") + "jsp".length());
				targetFilePathList.add(ifPath(deduplicationFilePath, projectList, pathList) + JSP_DIR + targetFilePath);
			} else {
				targetFilePath = deduplicationFilePath.substring(deduplicationFilePath.indexOf("webapp") + "webapp".length());
				targetFilePathList.add(ifPath(deduplicationFilePath, projectList, pathList) + targetFilePath);
			}
		}

		return targetFilePathList;
	}

	private static String ifPath(String path, List<String> projectList, List<String> pathList) {
		Map<String, String> mapping = new HashMap<>();

		for(int i = 0; i < projectList.size(); i++) {
			mapping.put(projectList.get(i), pathList.get(i));
		}

		return mapping.get(path.split("/")[1]);
	}
}
