package uk.co.caprica.vlcjplayer.api;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.consultant.ProcessingConsultant;
import uk.co.caprica.vlcjplayer.api.discord.MessageListener;

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
        } catch (Exception ex) {
            log.error("Exception: ", ex);
        }

    }
}
