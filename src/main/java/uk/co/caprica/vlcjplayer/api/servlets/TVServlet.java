package uk.co.caprica.vlcjplayer.api.servlets;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.consultant.PlexApiDataStore;
import uk.co.caprica.vlcjplayer.api.consultant.ProcessingConsultant;
import uk.co.caprica.vlcjplayer.api.model.PlaylistItem;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class TVServlet extends HttpServlet {

    final static Logger log = LoggerFactory.getLogger(TVServlet.class);

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        String status = "";

        ProcessingConsultant pds = new ProcessingConsultant();
        //PlexSqlDataStore plex = new PlexSqlDataStore();
        PlexApiDataStore plex = new PlexApiDataStore();
        String tvSearch = StringUtils.defaultString(request.getParameter("q"));
        String channel = StringUtils.defaultString(request.getParameter("c"));
        String result = "";
        if (!pds.allowCall(channel)) {
            result = "Currently Playing in another channel, sorry!";
        } else {
            ArrayList<PlaylistItem> episodes = plex.getTelevision(tvSearch);
            log.info("LIST OF TV : " + episodes.size());
            result = pds.playFile(episodes, channel);

        }
        response.getWriter().println("{ \"status\": \"" + StringEscapeUtils.escapeJson(result) + "\"}");
    }

}