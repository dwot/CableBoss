package uk.co.caprica.vlcjplayer.api.servlets;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.consultant.PlexSqlDataStore;
import uk.co.caprica.vlcjplayer.api.consultant.ProcessingConsultant;
import uk.co.caprica.vlcjplayer.api.model.MediaItem;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uk.co.caprica.vlcjplayer.Application.application;

public class MovieServlet extends HttpServlet {

    final static Logger log = LoggerFactory.getLogger(MovieServlet.class);

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        ProcessingConsultant pds = new ProcessingConsultant();
        PlexSqlDataStore plex = new PlexSqlDataStore();
        String movieSearch = StringUtils.defaultString(request.getParameter("q"));
        String channel = StringUtils.defaultString(request.getParameter("c"));
        String result = "";
        if (!pds.allowCall(channel)) {
            result = "Currently Playing in another channel, sorry!";
        } else {
            log.info("movieSearch: " + movieSearch);
            MediaItem media = plex.getMovie(movieSearch);
            log.info("STATUS: " + application().mediaPlayer().status().isPlaying());
            if (!media.getMrl().equals("")) {
                result += pds.playFile(media, channel);
            } else {
                result = "NO FILE FOUND";
            }
        }
        log.info("RESULT: " + result);
        response.getWriter().println("{ \"status\": \"" + StringEscapeUtils.escapeJson(result) + "\"}");
    }

}