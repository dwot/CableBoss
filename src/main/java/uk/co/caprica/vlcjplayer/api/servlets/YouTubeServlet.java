package uk.co.caprica.vlcjplayer.api.servlets;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.consultant.ProcessingConsultant;
import uk.co.caprica.vlcjplayer.api.model.MediaItem;

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
        String ytUrl = StringUtils.defaultString(request.getParameter("q")).trim();
        String channel = StringUtils.defaultString(request.getParameter("c"));
        String result = "";
        ProcessingConsultant pds = new ProcessingConsultant();
        if (!pds.allowCall(channel)) {
            result = "Currently Playing in another channel, sorry!";
        } else {
            log.info("ytUrl: " + ytUrl);
            if (!ytUrl.equals("") && (ytUrl.contains("youtube.com") || ytUrl.contains("youtu.be"))) {
                application().addRecentMedia(ytUrl);
                log.info("STATUS: " + application().mediaPlayer().status().isPlaying());
                if (!ytUrl.equals("")) {
                    application().setYtLastStart(new DateTime());
                    MediaItem media = new MediaItem();
                    media.setMrl(ytUrl);
                    media.setTitle("YOUTUBE VIDEO");
                    pds.playFile(media, channel);
                    result = "Playing Youtube";
                } else {
                    result = "NO FILE FOUND";
                }
            }
        }
        response.getWriter().println("{ \"status\": \"" + StringEscapeUtils.escapeJson(result) + "\"}");
    }

}