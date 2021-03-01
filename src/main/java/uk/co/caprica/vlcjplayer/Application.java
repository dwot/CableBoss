/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015 Caprica Software Limited.
 */

package uk.co.caprica.vlcjplayer;

import com.google.common.eventbus.EventBus;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.component.callback.ScaledCallbackImagePainter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.renderer.RendererItem;
import uk.co.caprica.vlcjplayer.api.model.MediaItem;
import uk.co.caprica.vlcjplayer.event.TickEvent;
import uk.co.caprica.vlcjplayer.view.action.mediaplayer.MediaPlayerActions;

import javax.print.attribute.standard.Media;
import javax.swing.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Global application state.
 */
public final class Application {

    final static Logger log = LoggerFactory.getLogger(Application.class);

    private static final String RESOURCE_BUNDLE_BASE_NAME = "strings/vlcj-player";

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME);

    private static final int MAX_RECENT_MEDIA_SIZE = 10;

    private final EventBus eventBus;

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

    private final CallbackMediaPlayerComponent callbackMediaPlayerComponent;

    private final MediaPlayerActions mediaPlayerActions;

    private final ScheduledExecutorService tickService = Executors.newSingleThreadScheduledExecutor();

    private final Deque<String> recentMedia = new ArrayDeque<>(MAX_RECENT_MEDIA_SIZE);

    private final Deque<MediaItem> playlist = new ArrayDeque<>();

    private boolean isStreaming = false;

    private Properties props = new Properties();

    private DateTime ytLastStart = new DateTime();

    private ArrayList<String> seriesList = new ArrayList<>();
    private ArrayList<String> movieList = new ArrayList<>();
    private MediaItem nowPlaying = new MediaItem();

    private String currentChannel = "";

    /**
     * Video output can be "EMBEDDED" for the usual hardware-accelerated playback, or "CALLBACK" for the software or
     * "direct-rendering" approach.
     */
    private VideoOutput videoOutput = VideoOutput.CALLBACK;

    private static final class ApplicationHolder {
        private static final Application INSTANCE = new Application();
    }

    public static Application application() {
        return ApplicationHolder.INSTANCE;
    }

    public static ResourceBundle resources() {
        return resourceBundle;
    }

    private Application() {
        eventBus = new EventBus();

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        callbackMediaPlayerComponent = new CallbackMediaPlayerComponent(null, null, null, true, new ScaledCallbackImagePainter());

        mediaPlayerActions = new MediaPlayerActions(mediaPlayerComponent.mediaPlayer());

        tickService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                eventBus.post(TickEvent.INSTANCE);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void subscribe(Object subscriber) {
        eventBus.register(subscriber);
    }

    public void post(Object event) {
        // Events are always posted and processed on the Swing Event Dispatch thread
        if (SwingUtilities.isEventDispatchThread()) {
            eventBus.post(event);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    eventBus.post(event);
                }
            });
        }
    }

    public EmbeddedMediaPlayerComponent mediaPlayerComponent() {
        return mediaPlayerComponent;
    }

    public CallbackMediaPlayerComponent callbackMediaPlayerComponent() {
        return callbackMediaPlayerComponent;
    }

    public EmbeddedMediaPlayer mediaPlayer() {
        switch (videoOutput) {
            case EMBEDDED:
                return mediaPlayerComponent.mediaPlayer();
            case CALLBACK:
                return callbackMediaPlayerComponent.mediaPlayer();
            default:
                throw new IllegalStateException();
        }
    }

    public VideoOutput videoOutput() {
        return videoOutput;
    }

    public MediaPlayerActions mediaPlayerActions() {
        return mediaPlayerActions;
    }

    public void addRecentMedia(String mrl) {
        if (!recentMedia.contains(mrl)) {
            recentMedia.addFirst(mrl);
            while (recentMedia.size() > MAX_RECENT_MEDIA_SIZE) {
                recentMedia.pollLast();
            }
        }
    }

    public List<String> recentMedia() {
        return new ArrayList<>(recentMedia);
    }

    public void clearRecentMedia() {
        recentMedia.clear();
    }

    public void setRenderer(RendererItem renderer) {
        mediaPlayerComponent.mediaPlayer().setRenderer(renderer);
    }

    public Deque<MediaItem> getPlaylist()  { return playlist; }

    public void clearPlayList() { playlist.clear(); }

    public void enqueueItem(MediaItem mrl) {
        log.info("enqueueItem");
        playlist.addLast(mrl);
    }

    public MediaItem getNextPlaylist(){
        log.info("getNextPlayList");
        MediaItem result = new MediaItem();
        if (playlist.size() > 0) {
            result = playlist.removeFirst();
        }
        return result;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public void setStreaming(boolean streaming) {
        isStreaming = streaming;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public DateTime getYtLastStart() {
        return ytLastStart;
    }

    public void setYtLastStart(DateTime ytLastStart) {
        this.ytLastStart = ytLastStart;
    }

    public ArrayList<String> getSeriesList() {
        return seriesList;
    }

    public void setSeriesList(ArrayList<String> seriesList) {
        this.seriesList = seriesList;
    }

    public ArrayList<String> getMovieList() {
        return movieList;
    }

    public void setMovieList(ArrayList<String> movieList) {
        this.movieList = movieList;
    }

    public String getCurrentChannel() {
        return currentChannel;
    }

    public void setCurrentChannel(String currentChannel) {
        this.currentChannel = currentChannel;
    }

    public MediaItem getNowPlaying() {
        return nowPlaying;
    }

    public void setNowPlaying(MediaItem nowPlaying) {
        this.nowPlaying = nowPlaying;
    }
}
