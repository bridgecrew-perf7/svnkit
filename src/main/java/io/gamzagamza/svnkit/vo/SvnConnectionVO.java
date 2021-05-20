package io.gamzagamza.svnkit.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SvnConnectionVO {
	private String svnUrl;
	private String svnUser;
	private String svnPassword;
}
