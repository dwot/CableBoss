package uk.co.caprica.vlcjplayer.api;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcjplayer.api.servlets.*;

import java.io.FileInputStream;
import java.util.Properties;

import static uk.co.caprica.vlcjplayer.Application.application;

public class ApiServer {
    private Server server;

    final static Logger log = LoggerFactory.getLogger(ApiServer.class);

    public void start() throws Exception {
        //Load Properties
        String propFile = System.getProperty("VLCJ_PROP_FILE");
        Properties props = new Properties();
        props.load(new FileInputStream(propFile));
        application().setProps(props);

        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8090);
        server.setConnectors(new Connector[]{connector});

        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);

        //servletHandler.addServletWithMapping(StatusServlet.class, "/status");
        //servletHandler.addServletWithMapping(Status2Servlet.class, "/status2");
        servletHandler.addServletWithMapping(MovieServlet.class, "/movie");
        servletHandler.addServletWithMapping(TVServlet.class, "/tv");
        servletHandler.addServletWithMapping(PauseServlet.class, "/pause");
        servletHandler.addServletWithMapping(ResumeServlet.class, "/resume");
        servletHandler.addServletWithMapping(ClearServlet.class, "/clear");
        servletHandler.addServletWithMapping(NextServlet.class, "/next");
        servletHandler.addServletWithMapping(SearchServlet.class, "/search");
        servletHandler.addServletWithMapping(YouTubeServlet.class, "/youtube");
        servletHandler.addServletWithMapping(ConnectServlet.class, "/connect");
        servletHandler.addServletWithMapping(AudioServlet.class, "/audio");
        servletHandler.addServletWithMapping(SubtitleServlet.class, "/subtitle");

        server.start();

        application().mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(final MediaPlayer mediaPlayer) {
                mediaPlayer.submit(new Runnable() {
                    @Override
                    public void run() {
                        String mrl = application().getNextPlaylist();
                        log.info("Play the next file: " + mrl);
                        if (!mrl.equals("")) {
                            mediaPlayer.media().play(mrl);
                        } else {
                            //Not playing anymore, let's disconnect.
                            ProcessingConsultant pds = new ProcessingConsultant();
                            pds.doAhkDisconnect();
                        }
                    }
                });
            }
        });
    }

    public void stop() throws Exception {
        server.stop();
    }

}