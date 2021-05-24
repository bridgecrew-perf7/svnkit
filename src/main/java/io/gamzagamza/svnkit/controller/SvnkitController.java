package io.gamzagamza.svnkit.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import io.gamzagamza.svnkit.service.SvnkitService;
import io.gamzagamza.svnkit.vo.SvnConnectionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.tmatesoft.svn.core.SVNException;


@RequestMapping("/svnkit")
@Controller
public class SvnkitController {
	private final Logger LOGGER = LoggerFactory.getLogger(SvnkitController.class);
	
	@Autowired
	private SvnkitService svnkitService;
	
	@GetMapping("/main.do")
	public String main(Model model) {
		model.addAttribute("users", svnkitService.getUserCookie());
		return "main";
	}
	
	@PostMapping("/test.do")
	public ResponseEntity<?> test(SvnConnectionVO svnConnectionVO) {
		return ResponseEntity.ok().body(svnkitService.svnConnectionTest(svnConnectionVO));
	}
	
	@PostMapping("/connect.do")
	public ResponseEntity<?> connect(SvnConnectionVO svnConnectionVO) {
		return ResponseEntity.ok().body(svnkitService.svnConnection(svnConnectionVO));
	}
	
	@GetMapping("/work.do")
	public String work(ModelMap model) throws SVNException {
		model.addAttribute("lastRevision", svnkitService.svnLastRevision());
		
		return "work";
	}

	@PostMapping("/userCookieAdd.do")
	public ResponseEntity<?> userCookieAdd(SvnConnectionVO svnConnectionVO) {
		svnkitService.userCookieAdd(svnConnectionVO);
		return ResponseEntity.ok().body("success");
	}
	
	@GetMapping("/deduplicationFilePath.do")
	public ResponseEntity<?> deduplicationFilePath(@RequestParam("startRevision")Long startRevision,
													@RequestParam("endRevision")Long endRevision,
												    @RequestParam("regex")String regex) throws SVNException {
		return ResponseEntity.ok().body(svnkitService.getDeduplicationFilePath(startRevision, endRevision, regex));
	}
	
	@GetMapping("/batDownload.do")
	public void batDownload(@RequestParam("startRevision")Long startRevision,
								@RequestParam("endRevision")Long endRevision,
								@RequestParam("regex")String regex,
								@RequestParam("project")List<String> projectList,
								@RequestParam("path")List<String> pathList,
								HttpServletResponse response) throws SVNException {
		svnkitService.batDownload(startRevision, endRevision, regex, projectList, pathList, response);
	}
}
