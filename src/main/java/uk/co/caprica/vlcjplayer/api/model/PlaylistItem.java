package uk.co.caprica.vlcjplayer.api.model;

public class PlaylistItem {

	private String title;
	private String mrl;
	private String year;
	private String type;
	private String series;
	private String epicode;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMrl() {
		return mrl;
	}

	public void setMrl(String mrl) {
		this.mrl = mrl;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getEpicode() {
		return epicode;
	}

	public void setEpicode(String epicode) {
		this.epicode = epicode;
	}
}
