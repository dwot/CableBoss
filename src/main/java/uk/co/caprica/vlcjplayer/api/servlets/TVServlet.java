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
import java.util.ArrayList;

import static uk.co.caprica.vlcjplayer.Application.application;

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
        String tvSearch = StringUtils.defaultString(request.getParameter("q"));
        ArrayList<String> episodes = pds.getTelevision(tvSearch);
        String channel = StringUtils.defaultString(request.getParameter("c"));
        status = pds.playFile(episodes, channel);
        response.getWriter().println("{ \"status\": \"" + status + "\"}");
    }

}