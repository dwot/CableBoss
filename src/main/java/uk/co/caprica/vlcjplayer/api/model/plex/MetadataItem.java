package uk.co.caprica.vlcjplayer.api.model.plex;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class MetadataItem{

	@SerializedName("studio")
	private String studio;

	@SerializedName("addedAt")
	private int addedAt;

	@SerializedName("year")
	private int year;

	@SerializedName("thumb")
	private String thumb;

	@SerializedName("rating")
	private double rating;

	@SerializedName("ratingKey")
	private String ratingKey;

	@SerializedName("primaryExtraKey")
	private String primaryExtraKey;

	@SerializedName("type")
	private String type;

	@SerializedName("title")
	private String title;

	@SerializedName("ratingImage")
	private String ratingImage;

	@SerializedName("duration")
	private int duration;

	@SerializedName("key")
	private String key;

	@SerializedName("updatedAt")
	private int updatedAt;

	@SerializedName("summary")
	private String summary;

	@SerializedName("art")
	private String art;

	@SerializedName("audienceRating")
	private double audienceRating;

	@SerializedName("Media")
	private List<MediaItem> media;

	@SerializedName("Director")
	private List<DirectorItem> director;

	@SerializedName("originallyAvailableAt")
	private String originallyAvailableAt;

	@SerializedName("Role")
	private List<RoleItem> role;

	@SerializedName("guid")
	private String guid;

	@SerializedName("tagline")
	private String tagline;

	@SerializedName("Country")
	private List<CountryItem> country;

	@SerializedName("contentRating")
	private String contentRating;

	@SerializedName("Genre")
	private List<GenreItem> genre;

	@SerializedName("chapterSource")
	private String chapterSource;

	@SerializedName("audienceRatingImage")
	private String audienceRatingImage;

	@SerializedName("Writer")
	private List<WriterItem> writer;

	@SerializedName("lastViewedAt")
	private int lastViewedAt;

	@SerializedName("viewCount")
	private int viewCount;

	@SerializedName("titleSort")
	private String titleSort;

	@SerializedName("Collection")
	private List<CollectionItem> collection;

	@SerializedName("originalTitle")
	private String originalTitle;

	@SerializedName("viewOffset")
	private int viewOffset;

	@SerializedName("index")
	private int index;

	@SerializedName("parentIndex")
	private int parentIndex;

	public String getStudio(){
		return studio;
	}

	public int getAddedAt(){
		return addedAt;
	}

	public int getYear(){
		return year;
	}

	public String getThumb(){
		return thumb;
	}

	public double getRating(){
		return rating;
	}

	public String getRatingKey(){
		return ratingKey;
	}

	public String getPrimaryExtraKey(){
		return primaryExtraKey;
	}

	public String getType(){
		return type;
	}

	public String getTitle(){
		return title;
	}

	public String getRatingImage(){
		return ratingImage;
	}

	public int getDuration(){
		return duration;
	}

	public String getKey(){
		return key;
	}

	public int getUpdatedAt(){
		return updatedAt;
	}

	public String getSummary(){
		return summary;
	}

	public String getArt(){
		return art;
	}

	public double getAudienceRating(){
		return audienceRating;
	}

	public List<MediaItem> getMedia(){
		return media;
	}

	public List<DirectorItem> getDirector(){
		return director;
	}

	public String getOriginallyAvailableAt(){
		return originallyAvailableAt;
	}

	public List<RoleItem> getRole(){
		return role;
	}

	public String getGuid(){
		return guid;
	}

	public String getTagline(){
		return tagline;
	}

	public List<CountryItem> getCountry(){
		return country;
	}

	public String getContentRating(){
		return contentRating;
	}

	public List<GenreItem> getGenre(){
		return genre;
	}

	public String getChapterSource(){
		return chapterSource;
	}

	public String getAudienceRatingImage(){
		return audienceRatingImage;
	}

	public List<WriterItem> getWriter(){
		return writer;
	}

	public int getLastViewedAt(){
		return lastViewedAt;
	}

	public int getViewCount(){
		return viewCount;
	}

	public String getTitleSort(){
		return titleSort;
	}

	public List<CollectionItem> getCollection(){
		return collection;
	}

	public String getOriginalTitle(){
		return originalTitle;
	}

	public int getViewOffset(){
		return viewOffset;
	}

	public int getIndex() {
		return index;
	}

	public int getParentIndex() {
		return parentIndex;
	}
}