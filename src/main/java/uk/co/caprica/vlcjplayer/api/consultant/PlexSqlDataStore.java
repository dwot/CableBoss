package uk.co.caprica.vlcjplayer.api.consultant;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.model.MediaItem;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.co.caprica.vlcjplayer.Application.application;

public class PlexSqlDataStore {

    final static Logger log = LoggerFactory.getLogger(PlexSqlDataStore.class);
    String dbPath = application().getProps().getProperty("plexDatabasePath");
    String pathFlipMap = application().getProps().getProperty("pathFlipMap");

    public ArrayList<String> buildMovieCache() {
        ArrayList<String> movieTitles = new ArrayList<>();
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
                    "WHERE series is null and library_sections.name = 'Movies'";
            PreparedStatement stmt = cnx.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                movieTitles.add(rs.getString("Title"));
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }
        return movieTitles;
    }

    public ArrayList<String> buildSeriesCache() {
        ArrayList<String> seriesTitles = new ArrayList<>();
        //Search TV Series
        try (Connection cnx = DriverManager.getConnection(dbPath)) {
            String sql = "SELECT DISTINCT(metadata_series.title) as Series, metadata_series.id as SeriesId, count(*) as episodes, library_sections.name FROM media_items " +
                    "                    INNER JOIN metadata_items as metadata_media " +
                    "                              ON media_items.metadata_item_id = metadata_media.id " +
                    "                    LEFT JOIN metadata_items as metadata_season " +
                    "                             ON metadata_media.parent_id = metadata_season.id " +
                    "                    LEFT JOIN metadata_items as metadata_series " +
                    "                             ON metadata_season.parent_id = metadata_series.id " +
                    "                    INNER JOIN section_locations " +
                    "                              ON media_items.section_location_id = section_locations.id " +
                    "                    INNER JOIN library_sections " +
                    "                              ON library_sections.id = section_locations.library_section_id " +
                    "                    INNER JOIN media_parts ON media_parts.media_item_id = media_items.id " +
                    "                    WHERE library_sections.name in ('TV Shows','TV Shows Jr.')" +
                    "                    GROUP BY Series order by episodes desc";

            PreparedStatement stmt = cnx.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            log.info("preparedstatement: " + stmt);
            while (rs.next()) {
                seriesTitles.add(rs.getString("Series"));
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }
        return seriesTitles;
    }

    public MediaItem getMovie(String query) {
        String movieFile = "";
        MediaItem media = new MediaItem();
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
            query = fuzzSearch(query, application().getMovieList());
            stmt.setString(1, "%" + query.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                log.info("FILE: " + rs.getString("file"));
                movieFile = flipFile(rs.getString("file"));
                media.setMrl(movieFile);
                media.setTitle(rs.getString("Title"));
            } else {
                log.info("NO RESULTS");
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }
        return media;
    }

    public MediaItem getEpisode(String show, String epicode) {
        String result = "";
        MediaItem media = new MediaItem();
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
            show = fuzzSearch(show, application().getSeriesList());
            stmt.setString(1, "%" + show.trim() + "%");
            stmt.setString(2, epicode.trim());
            log.info("SQL: " + stmt);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                log.info("FILE: " + rs.getString("file"));
                result = flipFile(rs.getString("file"));
                media.setMrl(result);
                media.setTitle(rs.getString("Title") + " - " + rs.getString("epicode"));
            } else {
                log.info("NO RESULTS");
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }
        return media;
    }

    public ArrayList<MediaItem> getShow(String show, boolean blnShuffle) {
        log.info("find show: " + show + " bln: " + blnShuffle);
        ArrayList<MediaItem> result = new ArrayList<>();
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
                    "WHERE metadata_series.title like ? and library_sections.name <> 'Movies' ";
            if (blnShuffle) {
                sql += "ORDER BY RANDOM()";
            } else {
                sql += "ORDER BY SEASON, EPISODE";
            }

            PreparedStatement stmt = cnx.prepareStatement(sql);
            show = fuzzSearch(show, application().getSeriesList());
            stmt.setString(1, "%" + show.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            log.info("preparedstatement: " + stmt);
            while (rs.next()) {
                log.info("FILE: " + rs.getString("file"));
                MediaItem media = new MediaItem();
                media.setTitle(rs.getString("Series") + " - " + rs.getString("epicode"));
                media.setMrl(flipFile(rs.getString("file")));
                result.add(media);
            }
            rs.close();
            stmt.close();
        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
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

    public String fuzzSearch(String query, ArrayList<String> list) {
        String fuzzyResult = query;
        ExtractedResult fuzzy = FuzzySearch.extractOne(query, list);
        log.info("Query: " + query + " Result: " + fuzzy.getString() + " (Fuzzy Confidence: " + fuzzy.getScore() + ")");
        fuzzyResult = fuzzy.getString();
        return fuzzyResult;
    }


    public ArrayList<MediaItem> getTelevision(String query) {
        ArrayList<MediaItem> result = new ArrayList<>();
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

}
