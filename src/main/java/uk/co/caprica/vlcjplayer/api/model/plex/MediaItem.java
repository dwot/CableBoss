package uk.co.caprica.vlcjplayer.api.model.plex;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class MediaItem{

	@SerializedName("container")
	private String container;

	@SerializedName("videoProfile")
	private String videoProfile;

	@SerializedName("bitrate")
	private int bitrate;

	@SerializedName("aspectRatio")
	private double aspectRatio;

	@SerializedName("audioCodec")
	private String audioCodec;

	@SerializedName("videoFrameRate")
	private String videoFrameRate;

	@SerializedName("duration")
	private int duration;

	@SerializedName("audioChannels")
	private int audioChannels;

	@SerializedName("Part")
	private List<PartItem> part;

	@SerializedName("width")
	private int width;

	@SerializedName("id")
	private int id;

	@SerializedName("videoResolution")
	private String videoResolution;

	@SerializedName("height")
	private int height;

	@SerializedName("videoCodec")
	private String videoCodec;

	public String getContainer(){
		return container;
	}

	public String getVideoProfile(){
		return videoProfile;
	}

	public int getBitrate(){
		return bitrate;
	}

	public double getAspectRatio(){
		return aspectRatio;
	}

	public String getAudioCodec(){
		return audioCodec;
	}

	public String getVideoFrameRate(){
		return videoFrameRate;
	}

	public int getDuration(){
		return duration;
	}

	public int getAudioChannels(){
		return audioChannels;
	}

	public List<PartItem> getPart(){
		return part;
	}

	public int getWidth(){
		return width;
	}

	public int getId(){
		return id;
	}

	public String getVideoResolution(){
		return videoResolution;
	}

	public int getHeight(){
		return height;
	}

	public String getVideoCodec(){
		return videoCodec;
	}
}