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
import org.springframework.ui.ModelMap;
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
	private final String JSP_DIR = "vComponent/svnkit/";
	
	@Autowired
	private SvnkitService svnkitService;
	
	@GetMapping("/main.do")
	public String main() {
		return JSP_DIR + "main";
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
		
		return JSP_DIR + "work";
	}
	
	@GetMapping("/deduplicationFilePath.do")
	public ModelAndView deduplicationFilePath(@RequestParam("startRevision")Long startRevision,
													@RequestParam("endRevision")Long endRevision) throws SVNException {
		ModelAndView modelAndView = new ModelAndView();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		resultMap.put("deduplicationFilePath", svnkitService.getDeduplicationFilePath(startRevision, endRevision));
		
		modelAndView.addObject("result", resultMap);
		modelAndView.setViewName("jsonView");
		
		return modelAndView;
	}
	
	@GetMapping("/batDownload.do")
	public void batDownload(@RequestParam("startRevision")Long startRevision,
								@RequestParam("endRevision")Long endRevision,
								@RequestParam("project")List<String> projectList,
								@RequestParam("path")List<String> pathList,
								HttpServletResponse response) throws SVNException {
		svnkitService.batDownload(startRevision, endRevision, projectList, pathList, response);
	}
}
