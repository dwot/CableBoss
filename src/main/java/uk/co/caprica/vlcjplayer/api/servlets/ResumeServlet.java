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

public class ResumeServlet extends HttpServlet {

    final static Logger log = LoggerFactory.getLogger(ResumeServlet.class);

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        log.info("STATUS: " + application().mediaPlayer().status().isPlaying());
        String channel = StringUtils.defaultString(request.getParameter("c"));
        String result = "";
        ProcessingConsultant pds = new ProcessingConsultant();
        if (!pds.allowCall(channel)) {
            result = "Currently Playing in another channel, sorry!";
        } else {
            application().mediaPlayer().controls().play();
            result = "resumed";
        }
        response.getWriter().println("{ \"status\": \"" + result + "\"}");

    }

}