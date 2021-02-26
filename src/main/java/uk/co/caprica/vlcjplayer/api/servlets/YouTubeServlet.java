package uk.co.caprica.vlcjplayer.api.servlets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uk.co.caprica.vlcjplayer.Application.application;

public class YouTubeServlet extends HttpServlet {

    final static Logger log = LoggerFactory.getLogger(YouTubeServlet.class);

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        String status = "";

        String ytUrl = StringUtils.defaultString(request.getParameter("q")).trim();
        log.info("ytUrl: " + ytUrl);
        if (!ytUrl.equals("") && (ytUrl.contains("youtube.com") || ytUrl.contains("youtu.be"))) {
            application().addRecentMedia(ytUrl);
            log.info("STATUS: " + application().mediaPlayer().status().isPlaying());
            if (!ytUrl.equals("")) {
                if (!application().mediaPlayer().status().isPlaying()) {
                    log.info("Start Movie Immediately: " + ytUrl);
                    application().mediaPlayer().media().play(ytUrl);
                    status = "YouTube started.";
                } else {
                    log.info("Enqueue Movie: " + ytUrl);
                    application().enqueueItem(ytUrl);
                    status = "YouTube added to queue (#" + application().getPlaylist().size() + ").";
                }
            } else {
                status = "NO FILE FOUND";
            }
        }
        response.getWriter().println("{ \"status\": \"" + status + "\"}");
    }

}