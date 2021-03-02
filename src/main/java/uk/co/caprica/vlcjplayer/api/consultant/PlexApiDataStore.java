package uk.co.caprica.vlcjplayer.api.consultant;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.model.PlaylistItem;
import uk.co.caprica.vlcjplayer.api.model.plex.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.co.caprica.vlcjplayer.Application.application;

public class PlexApiDataStore {

    final static Logger log = LoggerFactory.getLogger(PlexApiDataStore.class);
    String pathFlipMap = application().getProps().getProperty("pathFlipMap");
    String plexBaseUrl = application().getProps().getProperty("plexBaseUrl");
    String plexToken = application().getProps().getProperty("plexToken");
    List<String> movieSections = Arrays.asList(application().getProps().getProperty("plexMovieSections").split(","));
    List<String> tvSections = Arrays.asList(application().getProps().getProperty("plexTvSections").split(","));


    public DualHashBidiMap<String, String> buildMovieCache() {
        return buildCache(movieSections);
    }

    public DualHashBidiMap<String, String> buildSeriesCache() {
        return buildCache(tvSections);
    }

    public DualHashBidiMap<String, String> buildCache(List<String> sections) {
        DualHashBidiMap<String, String> resultTitles = new DualHashBidiMap<>();
        //Search Movies
        try {
            for (String section : sections) {
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet getRequest = new HttpGet(
                        plexBaseUrl + "library/sections/" + section + "/all");
                getRequest.addHeader("accept", "application/json");
                getRequest.addHeader("X-Plex-Token", plexToken);

                log.info("Make API Call");
                HttpResponse response = httpClient.execute(getRequest);

                log.info("API Call returned");
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + response.getStatusLine().getStatusCode());
                }

                InputStream source = response.getEntity().getContent(); //Get the data in the entity
                JsonReader jsonReader = new JsonReader(new InputStreamReader(source));
                PlexLibrary library  = new Gson().fromJson(jsonReader, PlexLibrary.class);
                for (MetadataItem item : library.getMediaContainer().getMetadata()) {
                    resultTitles.put(item.getRatingKey(), item.getTitle());
                }
                httpClient.close();
            }

        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }
        return resultTitles;
    }

    public ArrayList<PlaylistItem> getMovie(String query) {
        ArrayList<PlaylistItem> mediaList = new ArrayList<>();
        try {
            String id = fuzzSearch(query, application().getMovieList());
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(
                    plexBaseUrl + "library/metadata/" + id);
            getRequest.addHeader("accept", "application/json");
            getRequest.addHeader("X-Plex-Token", plexToken);

            log.info("Make API Call");
            HttpResponse response = httpClient.execute(getRequest);

            log.info("API Call returned");
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            InputStream source = response.getEntity().getContent(); //Get the data in the entity
            JsonReader jsonReader = new JsonReader(new InputStreamReader(source));

            PlexLibrary library  = new Gson().fromJson(jsonReader, PlexLibrary.class);
            for (MetadataItem item : library.getMediaContainer().getMetadata()) {
                for (MediaItem mi : item.getMedia()) {
                    for (PartItem part :mi.getPart()) {
                        String movieFile = flipFile(part.getFile());
                        PlaylistItem media = new PlaylistItem();
                        media.setMrl(movieFile);
                        media.setTitle(item.getTitle());
                        mediaList.add(media);
                    }
                }
            }
            httpClient.close();

        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }
        return mediaList;
    }

    public ArrayList<PlaylistItem> getShow(String query, String epicode, boolean blnShuffle) {
        ArrayList<PlaylistItem> mediaList = new ArrayList<>();
        try {
            String id = fuzzSearch(query, application().getSeriesList());
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(
                    plexBaseUrl + "library/metadata/" + id + "/allLeaves");
            getRequest.addHeader("accept", "application/json");
            getRequest.addHeader("X-Plex-Token", plexToken);

            log.info("Make API Call");
            HttpResponse response = httpClient.execute(getRequest);

            log.info("API Call returned");
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            InputStream source = response.getEntity().getContent(); //Get the data in the entity
            JsonReader jsonReader = new JsonReader(new InputStreamReader(source));

            int requestedEpisode = -1;
            int requestedSeason = -1;
            if (epicode != null && !epicode.equals("")) {
                epicode = epicode.toUpperCase(Locale.ROOT);
                if (epicode.contains("E")) {
                    String[] epiSplit = epicode.split("E");
                    requestedSeason = Integer.parseInt(epiSplit[0].replace("S",""));
                    requestedEpisode = Integer.parseInt(epiSplit[1].replace("E",""));
                } else {
                    requestedSeason = Integer.parseInt(epicode.replace("S",""));
                }
            }
            //File targetFile = new File("series-dump.json");
            //FileUtils.copyInputStreamToFile(source, targetFile);
            PlexLibrary library  = new Gson().fromJson(jsonReader, PlexLibrary.class);
            String series = library.getMediaContainer().getParentTitle();
            for (MetadataItem item : library.getMediaContainer().getMetadata()) {
                int episodeNumber = item.getIndex();
                int seasonNumber = item.getParentIndex();
                String episode = "S" + seasonNumber + "E" + episodeNumber;
                boolean blnInclude = true;
                if (requestedSeason >= 0) {
                    if (requestedSeason == seasonNumber) {
                        if (requestedEpisode > 0) {
                            if (requestedEpisode != episodeNumber) blnInclude = false;
                        }
                    } else {
                        blnInclude = false;
                    }
                }

                if (blnInclude) {
                    for (MediaItem mi : item.getMedia()) {
                        for (PartItem part : mi.getPart()) {
                            String movieFile = flipFile(part.getFile());
                            PlaylistItem media = new PlaylistItem();
                            media.setMrl(movieFile);
                            media.setTitle(item.getTitle());
                            media.setEpicode(episode);
                            media.setSeries(series);
                            media.setType("tv");
                            mediaList.add(media);
                        }
                    }
                }
            }
            httpClient.close();


        } catch (Exception ex) {
            log.error("ERROR processing test Connection", ex);
        }
        if (blnShuffle) Collections.shuffle(mediaList);
        return mediaList;
    }

    public ArrayList<PlaylistItem> getTelevision(String query) {
        ArrayList<PlaylistItem> result = new ArrayList<>();
        //Look for and parse out exact episode
        Pattern p = Pattern.compile("([Ss]?)([0-9]{1,2})([xXeE\\.\\-]?)([0-9]{1,2})");
        Matcher m = p.matcher(query);
        boolean blnEpisodeSearch = false;
        String episodeLookup = "";
        if (m.find()) {
            episodeLookup = m.group(0);
            blnEpisodeSearch = true;
        }
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

        if (blnEpisodeSearch) {
            show = query.replace(episodeLookup, "");
            log.info("SHOW: " + show + " | EPISODE: " + episodeLookup);
            result = getShow(show, episodeLookup, blnShuffle);
        } else {
            log.info("NO EPISODE BIT PASSED");
            show = query;
            result = getShow(show.trim(), "", blnShuffle);
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

    public String fuzzSearch(String query, DualHashBidiMap<String, String> map) {
        ExtractedResult fuzzy = FuzzySearch.extractOne(query, map.values());
        log.info("Query: " + query + " Result: " + fuzzy.getString() + " (Fuzzy Confidence: " + fuzzy.getScore() + ")");
        return map.getKey(fuzzy.getString());
    }

}
