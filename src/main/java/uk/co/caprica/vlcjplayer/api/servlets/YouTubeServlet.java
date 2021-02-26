package uk.co.caprica.vlcjplayer.api.servlets;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.ProcessingConsultant;

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
            String channel = StringUtils.defaultString(request.getParameter("c"));
            if (!ytUrl.equals("")) {
                ProcessingConsultant pds = new ProcessingConsultant();
                application().setYtLastStart(new DateTime());
                pds.playFile(ytUrl, channel);
            } else {
                status = "NO FILE FOUND";
            }
        }
        response.getWriter().println("{ \"status\": \"" + status + "\"}");
    }

}