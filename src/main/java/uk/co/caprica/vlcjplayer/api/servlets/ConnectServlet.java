package uk.co.caprica.vlcjplayer.api.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.ProcessingConsultant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConnectServlet extends HttpServlet {

    final static Logger log = LoggerFactory.getLogger(ConnectServlet.class);

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        ProcessingConsultant pds = new ProcessingConsultant();
        pds.doAhkConnect();
        response.getWriter().println("{ \"status\": \"connected\"}");
        
    }

}