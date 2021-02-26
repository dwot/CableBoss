package uk.co.caprica.vlcjplayer.api.servlets;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.ProcessingConsultant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SubtitleServlet extends HttpServlet {

    final static Logger log = LoggerFactory.getLogger(SubtitleServlet.class);

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        ProcessingConsultant pds = new ProcessingConsultant();
        String searchQuery = StringUtils.defaultString(request.getParameter("q"));
        log.info("searchQuery: " + searchQuery);
        String channel = StringUtils.defaultString(request.getParameter("c"));
        String result = "";
        if (!pds.allowCall(channel)) {
            result = "Currently Playing in another channel, sorry!";
        } else {
            if (NumberUtils.isCreatable(searchQuery)) pds.setSubTrack(Integer.parseInt(searchQuery));
            result = pds.listSubtitleTracks();
        }
        response.getWriter().println(result);
    }

}