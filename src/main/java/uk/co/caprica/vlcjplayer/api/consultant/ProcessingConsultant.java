package uk.co.caprica.vlcjplayer.api.consultant;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.TrackDescription;
import uk.co.caprica.vlcjplayer.api.model.PlaylistItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static uk.co.caprica.vlcjplayer.Application.application;

public class ProcessingConsultant {

    final static Logger log = LoggerFactory.getLogger(ProcessingConsultant.class);
    String ahkPath = application().getProps().getProperty("ahkPath");
    String scriptPath = application().getProps().getProperty("scriptPath");

    public void buildCaches() {
        PlexApiDataStore plex = new PlexApiDataStore();
        application().setMovieList(plex.buildMovieCache());
        application().setSeriesList(plex.buildSeriesCache());
    }

    public String doFuzzSearch(String query) {
        String result = "";
        //Search Movies
        List<ExtractedResult> fuzzyList = FuzzySearch.extractSorted(query, application().getMovieList().values(), 70);
        result += "MOVIES\n";
        int count = 0;
        for (ExtractedResult fuzzy : fuzzyList) {
            log.info("Query: " + query + " Result: " + fuzzy.getString() + " (Fuzzy Confidence: " + fuzzy.getScore() + ")");
            if (count < 20) result += fuzzy.getString() + "\n";
            count++;
        }
        if (count > 20) result += " and " + (count - 20) + " more.\n";
        if (count == 0) result += "No Movies Found\n";
        //Search TV
        fuzzyList = FuzzySearch.extractSorted(query, application().getSeriesList().values(), 70);
        result += "\nSERIES\n";
        count = 0;
        for (ExtractedResult fuzzy : fuzzyList) {
            log.info("Query: " + query + " Result: " + fuzzy.getString() + " (Fuzzy Confidence: " + fuzzy.getScore() + ")");
            if (count < 20) result += fuzzy.getString() + "\n";
            count++;
        }
        if (count > 20) result += " and " + (count - 20) + " more.\n";
        if (count == 0) result += "\nNo Series Found\n";
        return result;
    }

    public void callAhk(String script, String arg) throws IOException, InterruptedException {
        if (Boolean.parseBoolean(StringUtils.defaultString(application().getProps().getProperty("autoConnect"), "false)"))) {
            log.info("ahk: " + ahkPath);
            log.info("script: " + scriptPath + script);
            log.info("arg: " + arg);
            Runtime.getRuntime().exec(new String[]{ahkPath, scriptPath + script, arg});
            Thread.currentThread();
            Thread.sleep(1000);
        }
    }

    public void callAhk(String script) throws IOException, InterruptedException {
        if (Boolean.parseBoolean(StringUtils.defaultString(application().getProps().getProperty("autoConnect"), "false)"))) {
            log.info("ahk: " + ahkPath);
            log.info("script: " + scriptPath + script);
            Runtime.getRuntime().exec(new String[]{ahkPath, scriptPath + script});
            Thread.currentThread();
            Thread.sleep(3000);
        }
    }

    public void doAhkDisconnect() {
        if (application().isStreaming()) {
            try {
                application().setCurrentChannel("");
                callAhk("disconnect.ahk");
            } catch (Exception e) {
                log.error("ERROR Disconnecting", e);
            }
            application().setStreaming(false);
        }
    }

    public void doAhkConnect(String channel) {
        if (!application().isStreaming()) {
            try {
                application().setCurrentChannel(channel);
                callAhk("connect.ahk",channel);
            } catch (Exception e) {
                log.error("ERROR Connecting", e);
            }
            application().setStreaming(true);
        }
    }

    public String listAudioTracks() {
        String result = "";
        List<TrackDescription> audioTracks = application().mediaPlayer().audio().trackDescriptions();
        for (TrackDescription td : audioTracks) {
            log.info("Track: " + td.description() + "(" + td.id() + ")");
            result += td.id() + " " + td.description() + " \n";
        }
        log.info("CURRENT TRACK: " + application().mediaPlayer().audio().track());
        result += "Current Track: " + application().mediaPlayer().audio().track() + " \n";
        return result;
    }

    public void setAudioTrack(int track) {
        application().mediaPlayer().audio().setTrack(track);
    }

    public String listSubtitleTracks() {
        String result = "";
        List<TrackDescription> audioTracks = application().mediaPlayer().subpictures().trackDescriptions();
        for (TrackDescription td : audioTracks) {
            log.info("Track: " + td.description() + "(" + td.id() + ")");
            result += td.id() + " " + td.description() + " \n";
        }
        log.info("CURRENT TRACK: " + application().mediaPlayer().subpictures().track());
        result += "Current Track: " + application().mediaPlayer().subpictures().track() + " \n";
        return result;
    }

    public void setSubTrack(int track) {
        application().mediaPlayer().subpictures().setTrack(track);
    }

    public String playFile(PlaylistItem media, String channel) {
        ArrayList<PlaylistItem> mediaList = new ArrayList<>();
        mediaList.add(media);
        return playFile(mediaList, channel);
    }

    public String playFile(ArrayList<PlaylistItem> mediaList, String channel) {
        String result = "";
        int count = 0;
        boolean blnAlreadyStarted = false;
        for (PlaylistItem media : mediaList) {
            String mrl = media.getMrl();
            if (!blnAlreadyStarted && !application().mediaPlayer().status().isPlaying()) {
                log.info("Start Immediately: " + mrl);

                if (media.getType().equals("tv")) {
                    result = media.getSeries() + " - " + media.getEpicode() + " - " + media.getTitle();
                } else if (media.getType().equals("movie")) {
                    result = media.getTitle() + " (" + media.getYear() + ")";
                } else {
                    result = media.getTitle();
                }
                result += "\n";
                doAhkConnect(channel);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception ex) {
                    log.error("ERROR WAITING:", ex);
                }
                application().mediaPlayer().media().play(mrl);
                log.info("VOLUME IS : " + application().mediaPlayer().audio().volume());
                //application().mediaPlayer().audio().setVolume(application().mediaPlayer().audio().volume() + delta);
            } else {
                application().enqueueItem(media);
            }
            blnAlreadyStarted = true;
            count++;
        }
        result += String.valueOf(count) + " items added to the playlist, total playlist size: " + (application().getPlaylist().size()+1);
        return result;
    }

    public boolean allowCall(String channel) {
        boolean blnResult = false;
        if (application().isStreaming()) {
            if (application().getCurrentChannel().equals(channel)) {
                blnResult = true;
            }
        } else {
            if (!channel.equals("")) blnResult = true;
        }
        return blnResult;
    }

    public String listPlaylist() {
        String result = "";
        int count = 0;
        for (PlaylistItem media : application().getPlaylist()) {
            if (count < 10) result += media.getTitle() + "\n";
            count++;
        }
        if (count > 10) result += " and " + (count - 10) + " more.\n";
        if (count == 0) result += "No Active Playlist.\n";
        return result;
    }

    public String playNext(MediaPlayer mediaPlayer) {
        PlaylistItem media = application().getNextPlaylist();
        String mrl = media.getMrl();
        log.info("Play the next file: " + mrl);
        if (!mrl.equals("")) {
            mediaPlayer.media().play(mrl);
        } else {
            //Not playing anymore, let's disconnect.
            DateTime currTime = new DateTime();
            Duration duration = new Duration(application().getYtLastStart(), currTime);
            if (duration.getStandardSeconds() > 10) {
                ProcessingConsultant pds = new ProcessingConsultant();
                log.info("DISCO FROM API SERVER");
                pds.doAhkDisconnect();
            } else {
                log.info("STARTED YOUTUBE less than 10 s ago, keep rolling");
            }
        }
        String result = "";
        if (media.getType().equals("tv")) {
            result = media.getSeries() + " - " + media.getEpicode() + " - " + media.getTitle();
        } else if (media.getType().equals("movie")) {
            result = media.getTitle() + " (" + media.getYear() + ")";
        } else {
            result = media.getTitle();
        }
        return("Playing: " + result);
    }

    public void continuePlayback() {
        log.info("CONTINUE");
        application().getLastChannel().sendMessage("Timeout reset, enjoy!").queue();
        application().setWarnMessage("");
        application().setLastCommand(new DateTime());
        if (application().getPauseStarted() != null) application().mediaPlayer().controls().play();
        application().setPauseStarted(null);
        application().setWarnStarted(null);
    }

    public void disconnectPlayback() {
        log.info("DISCONNECTING");
        application().clearPlayList();
        application().mediaPlayer().controls().stop();
        doAhkDisconnect();
        application().getLastChannel().sendMessage("Disconnected due to inactivity.").queue();
        application().setWarnMessage("");
        application().setLastCommand(null);
        application().setPauseStarted(null);
        application().setWarnStarted(null);
    }

    public void pausePlayback() {
        log.info("PAUSE");
        application().mediaPlayer().controls().pause();
        application().setPauseStarted(new DateTime());
        application().getLastChannel().sendMessage("Paused due to inactivity.").queue();
    }

}
