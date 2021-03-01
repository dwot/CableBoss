package uk.co.caprica.vlcjplayer.api.servlets;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.consultant.ProcessingConsultant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PlaylistServlet extends HttpServlet {

    final static Logger log = LoggerFactory.getLogger(PlaylistServlet.class);

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        ProcessingConsultant pds = new ProcessingConsultant();
        String result = pds.listPlaylist();
        response.getWriter().println("{ \"status\": \"" + StringEscapeUtils.escapeJson(result) + "\"}");
    }

}