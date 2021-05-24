package io.gamzagamza.svnkit.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.Cookie;
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

	public List<SvnConnectionVO> getUserCookie() {
		List<SvnConnectionVO> svnConnections = new ArrayList<>();

		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		Cookie[] cookies = servletRequestAttributes.getRequest().getCookies();

		StringBuilder users = null;

		if(cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("svnUsers")) {
					users = new StringBuilder();
					users.append(cookie.getValue());
				}
			}
		}

		if(users != null) {
			String[] userArr = users.toString().split("&");
			for(String userInfo : userArr) {
				SvnConnectionVO svnConnectionVO = new SvnConnectionVO();
				svnConnectionVO.setSvnUrl(userInfo.split("#")[0]);
				svnConnectionVO.setSvnUser(userInfo.split("#")[1]);
				svnConnectionVO.setSvnPassword(userInfo.split("#")[2]);

				svnConnections.add(svnConnectionVO);
			}
		}

		return svnConnections;
	}

	public void userCookieAdd(SvnConnectionVO svnConnectionVO) {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		Cookie[] cookies = servletRequestAttributes.getRequest().getCookies();

		StringBuilder users = null;

		if(cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("svnUsers")) {
					users = new StringBuilder();
					users.append(cookie.getValue());
				}
			}
		}

		if(users != null) {
			String[] usersArr = users.toString().split("&");
			boolean exist = false;
			for(String user : usersArr) {
				String[] userInfo = user.split("#");
				if(userInfo[1].equals(svnConnectionVO.getSvnUser())) {
					exist = true;
				}
			}

			if(!exist) {
				users.append("&");
				users.append(svnConnectionVO.getSvnUrl() + "#");
				users.append(svnConnectionVO.getSvnUser() + "#");
				users.append(svnConnectionVO.getSvnPassword());
			}
		} else {
			users = new StringBuilder();

			users.append(svnConnectionVO.getSvnUrl() + "#");
			users.append(svnConnectionVO.getSvnUser() + "#");
			users.append(svnConnectionVO.getSvnPassword());
		}

		Cookie usersCookie = new Cookie("svnUsers", users.toString());

		servletRequestAttributes.getResponse().addCookie(usersCookie);
	}
	
	public List<String> getDeduplicationFilePath(Long startRevision, Long endRevision, String regex) throws SVNException {
		return SvnUtils.getDeduplicationFilePathList(startRevision, endRevision, regex);
	}
	
	public void batDownload(Long startRevision, Long endRevision, String regex, List<String> projectList, List<String> pathList, HttpServletResponse response) throws SVNException {
		List<String> deduplicationFilePathList = SvnUtils.getDeduplicationFilePathList(startRevision, endRevision, regex);
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
