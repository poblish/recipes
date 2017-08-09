package jgravatar;

public enum GravatarDefaultImage {

	GRAVATAR_ICON(""),

	IDENTICON("identicon"),

	MONSTERID("monsterid"),

	WAVATAR("wavatar"),

	HTTP_404("404");

	private String code;

	GravatarDefaultImage(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
