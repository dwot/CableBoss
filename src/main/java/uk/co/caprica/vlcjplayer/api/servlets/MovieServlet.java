package uk.co.caprica.vlcjplayer.api.servlets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.ProcessingConsultant;

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
        String status = "";

        ProcessingConsultant pds = new ProcessingConsultant();
        String movieSearch = StringUtils.defaultString(request.getParameter("q"));
        log.info("movieSearch: " + movieSearch);
        String mrl = pds.getMovie(movieSearch);
        application().addRecentMedia(mrl);
        log.info("STATUS: " + application().mediaPlayer().status().isPlaying());
        String channel = StringUtils.defaultString(request.getParameter("c"));
        if (!mrl.equals("")) {
            pds.playFile(mrl, channel);
        } else {
            status = "NO FILE FOUND";
        }
        response.getWriter().println("{ \"status\": \"" + status + "\"}");
    }

}