package uk.co.caprica.vlcjplayer.api.model.plex;

import com.google.gson.annotations.SerializedName;

public class PartItem{

	@SerializedName("duration")
	private int duration;

	@SerializedName("container")
	private String container;

	@SerializedName("file")
	private String file;

	@SerializedName("size")
	private long size;

	@SerializedName("indexes")
	private String indexes;

	@SerializedName("videoProfile")
	private String videoProfile;

	@SerializedName("id")
	private int id;

	@SerializedName("key")
	private String key;

	public int getDuration(){
		return duration;
	}

	public String getContainer(){
		return container;
	}

	public String getFile(){
		return file;
	}

	public long getSize(){
		return size;
	}

	public String getIndexes(){
		return indexes;
	}

	public String getVideoProfile(){
		return videoProfile;
	}

	public int getId(){
		return id;
	}

	public String getKey(){
		return key;
	}
}