package uk.co.caprica.vlcjplayer.api.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uk.co.caprica.vlcjplayer.Application.application;

public class PauseServlet extends HttpServlet {

    final static Logger log = LoggerFactory.getLogger(PauseServlet.class);

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        log.info("STATUS: " + application().mediaPlayer().status().isPlaying());
        application().mediaPlayer().controls().pause();
        response.getWriter().println("{ \"status\": \"paused\"}");

    }

}