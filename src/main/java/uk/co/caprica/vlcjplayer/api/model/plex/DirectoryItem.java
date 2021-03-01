package uk.co.caprica.vlcjplayer.api.model.plex;

import com.google.gson.annotations.SerializedName;

public class DirectoryItem{

	@SerializedName("leafCount")
	private int leafCount;

	@SerializedName("thumb")
	private String thumb;

	@SerializedName("viewedLeafCount")
	private int viewedLeafCount;

	@SerializedName("title")
	private String title;

	@SerializedName("key")
	private String key;

	public int getLeafCount(){
		return leafCount;
	}

	public String getThumb(){
		return thumb;
	}

	public int getViewedLeafCount(){
		return viewedLeafCount;
	}

	public String getTitle(){
		return title;
	}

	public String getKey(){
		return key;
	}
}