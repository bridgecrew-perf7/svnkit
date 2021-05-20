package io.gamzagamza.svnkit.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SvnLogVO {
	private String revision;
	private String author;
	private String date;
	private String message;
	private List<String> filePathList;
}
