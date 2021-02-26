package uk.co.caprica.vlcjplayer.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.TrackDescription;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.co.caprica.vlcjplayer.Application.application;

public class ProcessingConsultant {

    final static Logger log = LoggerFactory.getLogger(ProcessingConsultant.class);
    String dbPath = application().getProps().getProperty("plexDatabasePath");
    String ahkPath = application().getProps().getProperty("ahkPath");
    String scriptPath = application().getProps().getProperty("scriptPath");
    String pathFlipMap = application().getProps().getProperty("pathFlipMap");
    String videoChannel = application().getProps().getProperty("videoChannel");

    public String doSearch(String query) {
        String result = "";
        //Search Movies
        try (Connection cnx = DriverManager.getConnection(dbPath)) {
            String sql = "SELECT" +
                    "       media_item_id as id, metadata_item_id as meta_id, library_sections.name AS Libary, metadata_series.title as Series, " +
                    "       metadata_season.'index' AS Season, metadata_media.'index' AS EPISODE, " +
                    "        media_parts.file, metadata_media.title AS Title, metadata_media.year as year FROM media_items " +
                    "INNER JOIN metadata_items as metadata_media " +
                    "          ON media_items.metadata_item_id = metadata_media.id " +
                    "LEFT JOIN metadata_items as metadata_season " +
                    "         ON metadata_media.parent_id = metadata_season.id " +
                    "LEFT JOIN metadata_items as metadata_series " +
                    "         ON metadata_season.parent_id = metadata_series.id " +
                    "INNER JOIN section_locations " +
                    "          ON media_items.section_location_id = section_locations.id " +
                    "INNER JOIN library_sections " +
                    "          ON library_sections.id = section_locations.library_section_id " +
                    "INNER JOIN media_parts ON media_parts.media_item_id = media_items.id " +
                    "WHERE series is null and library_sections.name = 'Movies' AND metadata_media.title like ? ";
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, "%" + query.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            result += "MOVIES \n```";
            while (rs.next()) {
                count++;
                if (count <= 5) {
                    log.info("FOUND " + rs.getString("Title"));
                    result += rs.getString("Title") + " (" + rs.getString("year") + ")\n";
                }
            }
            rs.close();
            stmt.close();
            if (count > 5) {
                int over = count-5;
                result += "```\nAnd " + over + " additional matching movies not shown.\n";
            }
        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }

        //Search TV Series
        try (Connection cnx = DriverManager.getConnection(dbPath)) {
            String sql = "SELECT DISTINCT(metadata_series.title) as Series, metadata_series.id as SeriesId, count(*) as episodes FROM media_items " +
                    "INNER JOIN metadata_items as metadata_media " +
                    "          ON media_items.metadata_item_id = metadata_media.id " +
                    "LEFT JOIN metadata_items as metadata_season " +
                    "         ON metadata_media.parent_id = metadata_season.id " +
                    "LEFT JOIN metadata_items as metadata_series " +
                    "         ON metadata_season.parent_id = metadata_series.id " +
                    "INNER JOIN section_locations " +
                    "          ON media_items.section_location_id = section_locations.id " +
                    "INNER JOIN library_sections " +
                    "          ON library_sections.id = section_locations.library_section_id " +
                    "INNER JOIN media_parts ON media_parts.media_item_id = media_items.id " +
                    "WHERE metadata_series.title like ? and library_sections.name <> 'Movies' " +
                    "GROUP BY Series order by episodes desc";

            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, "%" + query.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            log.info("preparedstatement: " + stmt);
            int count = 0;
            result += "TV SERIES \n```";
            while (rs.next()) {
                count++;
                if (count <= 5) {
                    log.info("FOUND: " + rs.getString("Series"));
                    result += rs.getString("Series") + "(" + rs.getString("episodes") + ")\n";
                }
            }
            rs.close();
            stmt.close();
            if (count > 5) {
                int over = count-5;
                result += "```\nAnd " + over + " additional matching tv series not shown.\n";
            }
        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }


        return result;
    }

    public String getMovie(String query) {
        String movieFile = "";
        try (Connection cnx = DriverManager.getConnection(dbPath)) {
            String sql = "SELECT" +
                    "       media_item_id as id, metadata_item_id as meta_id, library_sections.name AS Libary, metadata_series.title as Series, " +
                    "       metadata_season.'index' AS Season, metadata_media.'index' AS EPISODE, " +
                    "        media_parts.file, metadata_media.title AS Title FROM media_items " +
                    "INNER JOIN metadata_items as metadata_media " +
                    "          ON media_items.metadata_item_id = metadata_media.id " +
                    "LEFT JOIN metadata_items as metadata_season " +
                    "         ON metadata_media.parent_id = metadata_season.id " +
                    "LEFT JOIN metadata_items as metadata_series " +
                    "         ON metadata_season.parent_id = metadata_series.id " +
                    "INNER JOIN section_locations " +
                    "          ON media_items.section_location_id = section_locations.id " +
                    "INNER JOIN library_sections " +
                    "          ON library_sections.id = section_locations.library_section_id " +
                    "INNER JOIN media_parts ON media_parts.media_item_id = media_items.id " +
                    "WHERE series is null and library_sections.name = 'Movies' AND metadata_media.title like ? ";
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, "%" + query.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                log.info("FILE: " + rs.getString("file"));
                movieFile = flipFile(rs.getString("file"));
            } else {
                log.info("NO RESULTS");
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }
        return movieFile;
    }

    public String getEpisode(String show, String epicode) {
        String result = "";
        try (Connection cnx = DriverManager.getConnection(dbPath)) {
            String sql = "SELECT" +
                    "       media_item_id as id, metadata_item_id as meta_id, library_sections.name AS Libary, metadata_series.title as Series, " +
                    "       metadata_season.'index' AS Season, metadata_media.'index' AS EPISODE, " +
                    "       ('S' || printf('%02d', metadata_season.'index') || 'E' || printf('%02d', metadata_media.'index')) as epicode, " +
                    "        media_parts.file, metadata_media.title AS Title FROM media_items " +
                    "INNER JOIN metadata_items as metadata_media " +
                    "          ON media_items.metadata_item_id = metadata_media.id " +
                    "LEFT JOIN metadata_items as metadata_season " +
                    "         ON metadata_media.parent_id = metadata_season.id " +
                    "LEFT JOIN metadata_items as metadata_series " +
                    "         ON metadata_season.parent_id = metadata_series.id " +
                    "INNER JOIN section_locations " +
                    "          ON media_items.section_location_id = section_locations.id " +
                    "INNER JOIN library_sections " +
                    "          ON library_sections.id = section_locations.library_section_id " +
                    "INNER JOIN media_parts ON media_parts.media_item_id = media_items.id " +
                    "WHERE metadata_series.title like (?) and library_sections.name <> 'Movies' " +
                    " and UPPER(epicode) = UPPER(?) ";
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, "%" + show.trim() + "%");
            stmt.setString(2, epicode.trim());
            log.info("SQL: " + stmt);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                log.info("FILE: " + rs.getString("file"));
                result = flipFile(rs.getString("file"));
            } else {
                log.info("NO RESULTS");
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }
        return result;
    }

    public ArrayList<String> getShow(String show, boolean blnShuffle) {
        log.info("find show: " + show + " bln: " + blnShuffle);
        ArrayList<String> result = new ArrayList<>();
        try (Connection cnx = DriverManager.getConnection(dbPath)) {
            String sql = "SELECT" +
                    "       media_item_id as id, metadata_item_id as meta_id, library_sections.name AS Libary, metadata_series.title as Series, " +
                    "       metadata_season.'index' AS Season, metadata_media.'index' AS EPISODE, " +
                    "        media_parts.file, metadata_media.title AS Title FROM media_items " +
                    "INNER JOIN metadata_items as metadata_media " +
                    "          ON media_items.metadata_item_id = metadata_media.id " +
                    "LEFT JOIN metadata_items as metadata_season " +
                    "         ON metadata_media.parent_id = metadata_season.id " +
                    "LEFT JOIN metadata_items as metadata_series " +
                    "         ON metadata_season.parent_id = metadata_series.id " +
                    "INNER JOIN section_locations " +
                    "          ON media_items.section_location_id = section_locations.id " +
                    "INNER JOIN library_sections " +
                    "          ON library_sections.id = section_locations.library_section_id " +
                    "INNER JOIN media_parts ON media_parts.media_item_id = media_items.id " +
                    "WHERE metadata_series.title like ? and library_sections.name <> 'Movies' ";
            if (blnShuffle) {
                sql += "ORDER BY RANDOM()";
            } else {
                sql += "ORDER BY SEASON, EPISODE";
            }

            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setString(1, "%" + show.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            log.info("preparedstatement: " + stmt);
            while (rs.next()) {
                log.info("FILE: " + rs.getString("file"));
                result.add(flipFile(rs.getString("file")));
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }
        return result;
    }

    public ArrayList<String> getTelevision(String query) {
        ArrayList<String> result = new ArrayList<>();
        //Look for and parse out exact episode
        Pattern p = Pattern.compile("([Ss]?)([0-9]{1,2})([xXeE\\.\\-]?)([0-9]{1,2})");
        Matcher m = p.matcher(query);
        boolean blnEpisodeSearch = false;
        String episodeLookup = "";
        if (m.find()) {
            episodeLookup = m.group(0);
            blnEpisodeSearch = true;
        }
        if (blnEpisodeSearch) {
            String show = query.replace(episodeLookup, "");
            log.info("SHOW: " + show + " | EPISODE: " + episodeLookup);
            result.add(getEpisode(show, episodeLookup));
        } else {
            log.info("NO EPISODE BIT PASSED");
            String show = query;
            boolean blnShuffle = false;
            if (query.contains("(shuffle)")) {
                show = show.replace("(shuffle)", "");
                blnShuffle = true;
            }
            if (query.contains("(random)")) {
                show = show.replace("(random)", "");
                blnShuffle = true;
            }
            if (query.contains("(rando)")) {
                show = show.replace("(rando)", "");
                blnShuffle = true;
            }
            result = getShow(show.trim(), blnShuffle);
        }
        return result;
    }

    private String flipFile(String file) {
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Gson gson = new Gson();
        Map<String, String> flipMap = gson.fromJson(pathFlipMap, type);
        String result = file;
        for (String key : flipMap.keySet()) {
            result = result.replace(key, flipMap.get(key));
        }
        result = result.replace("/", "\\");
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
                callAhk("connect.ahk",channel);
            } catch (Exception e) {
                log.error("ERROR Connecting", e);
            }
            application().setStreaming(true);
        }
    }

    public String listAudioTracks() {
        String result = "Audio Tracks: \n";
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
        String result = "Subtitle Tracks: \n";
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

    public String playFile(String mrl, String channel) {
        ArrayList<String> mrlList = new ArrayList<>();
        mrlList.add(mrl);
        return playFile(mrlList, channel);
    }

    public String playFile(ArrayList<String> mrlList, String channel) {
        String result = "";
        int count = 0;
        boolean blnAlreadyStarted = false;
        for (String mrl : mrlList) {
            if (!blnAlreadyStarted && !application().mediaPlayer().status().isPlaying()) {
                log.info("Start Immediately: " + mrl);
                doAhkConnect(channel);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception ex) {
                    log.error("ERROR WAITING:", ex);
                }
                application().mediaPlayer().media().play(mrl);
                blnAlreadyStarted = true;
                count++;
            } else {
                log.info("Enqueue: " + mrl);
                application().enqueueItem(mrl);
                blnAlreadyStarted = true;
                count++;
            }
        }
        result = String.valueOf(count) + " items added to the playlist, total playlist size: " + (application().getPlaylist().size()+1);
        return result;
    }
}
