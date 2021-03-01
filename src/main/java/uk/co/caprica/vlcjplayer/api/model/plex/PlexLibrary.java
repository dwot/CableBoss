package uk.co.caprica.vlcjplayer.api.model.plex;

import com.google.gson.annotations.SerializedName;

public class PlexLibrary{

	@SerializedName("MediaContainer")
	private MediaContainer mediaContainer;

	public MediaContainer getMediaContainer(){
		return mediaContainer;
	}
}