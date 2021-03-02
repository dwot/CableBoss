package uk.co.caprica.vlcjplayer.api;

import com.google.common.eventbus.Subscribe;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
        LocalDateTime time = new LocalDateTime();
        //log.info("TICK:" + time);
    }
}
