package uk.co.caprica.vlcjplayer.api.model.plex;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class MediaContainer{

	@SerializedName("mediaTagPrefix")
	private String mediaTagPrefix;

	@SerializedName("identifier")
	private String identifier;

	@SerializedName("art")
	private String art;

	@SerializedName("thumb")
	private String thumb;

	@SerializedName("librarySectionID")
	private int librarySectionID;

	@SerializedName("allowSync")
	private boolean allowSync;

	@SerializedName("Metadata")
	private List<MetadataItem> metadata;

	@SerializedName("Directory")
	private List<DirectoryItem> directory;

	@SerializedName("title1")
	private String title1;

	@SerializedName("title2")
	private String title2;

	@SerializedName("viewMode")
	private int viewMode;

	@SerializedName("librarySectionUUID")
	private String librarySectionUUID;

	@SerializedName("mediaTagVersion")
	private int mediaTagVersion;

	@SerializedName("viewGroup")
	private String viewGroup;

	@SerializedName("size")
	private int size;

	@SerializedName("librarySectionTitle")
	private String librarySectionTitle;

	@SerializedName("parentTitle")
	private String parentTitle;


	public String getMediaTagPrefix(){
		return mediaTagPrefix;
	}

	public String getIdentifier(){
		return identifier;
	}

	public String getArt(){
		return art;
	}

	public String getThumb(){
		return thumb;
	}

	public int getLibrarySectionID(){
		return librarySectionID;
	}

	public boolean isAllowSync(){
		return allowSync;
	}

	public List<MetadataItem> getMetadata(){
		return metadata;
	}

	public String getTitle1(){
		return title1;
	}

	public String getTitle2(){
		return title2;
	}

	public int getViewMode(){
		return viewMode;
	}

	public String getLibrarySectionUUID(){
		return librarySectionUUID;
	}

	public int getMediaTagVersion(){
		return mediaTagVersion;
	}

	public String getViewGroup(){
		return viewGroup;
	}

	public int getSize(){
		return size;
	}

	public String getLibrarySectionTitle(){
		return librarySectionTitle;
	}

	public List<DirectoryItem> getDirectory() {
		return directory;
	}

	public String getParentTitle() {
		return parentTitle;
	}
}