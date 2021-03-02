package uk.co.caprica.vlcjplayer.api;

import com.google.common.eventbus.Subscribe;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcjplayer.api.consultant.ProcessingConsultant;
import uk.co.caprica.vlcjplayer.api.discord.MessageListener;
import uk.co.caprica.vlcjplayer.event.TickEvent;

import java.io.FileInputStream;
import java.util.Properties;

import static uk.co.caprica.vlcjplayer.Application.application;

public class BotServer {

    final static Logger log = LoggerFactory.getLogger(BotServer.class);

    public void start() {
        try {
            //Load Properties
            String propFile = System.getProperty("VLCJ_PROP_FILE");
            System.out.println("propFile: " + propFile);
            Properties props = new Properties();
            props.load(new FileInputStream(propFile));
            application().setProps(props);

            ProcessingConsultant pds = new ProcessingConsultant();
            pds.buildCaches();
            JDA jda = new JDABuilder().createDefault(props.getProperty("discordToken")).build();
            jda.addEventListener(new MessageListener());

            application().mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void finished(final MediaPlayer mediaPlayer) {
                    mediaPlayer.submit(new Runnable() {
                        @Override
                        public void run() {
                            ProcessingConsultant pds = new ProcessingConsultant();
                            String result = "```" + pds.playNext(mediaPlayer) + "```";
                            if (application().getLastChannel() != null) application().getLastChannel().sendMessage(result).queue();
                        }
                    });
                }
            });

            application().subscribe(this);
        } catch (Exception ex) {
            log.error("Exception: ", ex);
        }

    }

    @Subscribe
    public void onTick(TickEvent tick) {
        tickProcess();
    }


    private void tickProcess() {
        int inactiveTimer = 10; //10
        int lastCommandMax = 180; //180
        int pauseMax = 30; //30

        if (application().isStreaming()) {
            DateTime time = new DateTime();
            if (application().getWarnMessage() != null && !application().getWarnMessage().equals("")) {
                ProcessingConsultant pds = new ProcessingConsultant();
                if (application().getWarnStarted() != null && application().getPauseStarted() != null) {
                    //Warning has been sent and we are paused
                    Duration warnedAgo = new Duration(application().getWarnStarted(), time);
                    Duration pausedAgo = new Duration(application().getPauseStarted(), time);
                    if (warnedAgo.getStandardMinutes() > inactiveTimer && pausedAgo.getStandardMinutes() > inactiveTimer) {
                        log.info("Warned Ago: " + warnedAgo.getStandardMinutes());
                        log.info("Paused Ago: " + pausedAgo.getStandardMinutes());
                        pds.disconnectPlayback();
                    }
                } else if (application().getWarnStarted() != null) {
                    //Warned but not paused
                    Duration warnedAgo = new Duration(application().getWarnStarted(), time);
                    if (warnedAgo.getStandardMinutes() > inactiveTimer) {
                        log.info("Warned Ago: " + warnedAgo.getStandardMinutes());
                        pds.pausePlayback();
                    }
                }
            } else {
                boolean blnSendWarning = false;
                if (application().getLastCommand() != null) {
                    Duration duration = new Duration(application().getLastCommand(), time);
                    if (duration.getStandardMinutes() > lastCommandMax) {
                        blnSendWarning = true;
                        log.info("OVER TIMED: " + application().getLastCommand());
                    }
                }
                if (application().getPauseStarted() != null) {
                    Duration duration = new Duration(application().getPauseStarted(), time);
                    if (duration.getStandardMinutes() > pauseMax) {
                        blnSendWarning = true;
                        log.info("OVER PAUSED: " + application().getPauseStarted());
                    }

                }
                if (blnSendWarning) {
                    log.info("WARNING");
                    String notice = "Hey, still watching?  If so react ✔️ or I'll disconnect in 15 mins.";
                    if (application().getLastChannel() != null) {
                        application().setWarnMessage(application().getLastChannel().sendMessage(notice).complete().getId());
                        Message message = application().getLastChannel().retrieveMessageById(application().getWarnMessage()).complete();
                        message.addReaction("✔️").queue();
                        message.addReaction("❌").queue();
                    }
                    application().setWarnStarted(time);
                }
            }
        }
    }
}
