package uk.co.caprica.vlcjplayer.api.discord;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjplayer.api.consultant.PlexApiDataStore;
import uk.co.caprica.vlcjplayer.api.consultant.ProcessingConsultant;
import uk.co.caprica.vlcjplayer.api.model.PlaylistItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static uk.co.caprica.vlcjplayer.Application.application;

public class MessageListener extends ListenerAdapter {

    String discordTrigger = application().getProps().getProperty("discordTrigger");

    private static List<String> basicCommands = Arrays.asList(new String[] { "search", "playlist" });
    private static List<String> privCommands = Arrays.asList(new String[] { "movie", "tv", "pause",
            "next", "resume", "clear", "yt", "youtube", "audio", "sub", "play", "subtitle" });
    final static Logger log = LoggerFactory.getLogger(MessageListener.class);
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.isFromType(ChannelType.PRIVATE)) {
            log.info(String.format("[PM] %s: %s", event.getAuthor().getName(),
                    event.getMessage().getContentDisplay()));
        } else {
            log.info(String.format("[%s][%s] %s: %s", event.getGuild().getName(),
                    event.getTextChannel().getName(), event.getMember().getEffectiveName(),
                    event.getMessage().getContentDisplay()));
        }

        Message msg = event.getMessage();
        if (msg.getContentRaw().startsWith(discordTrigger)) {
            VoiceChannel voiceChannel = msg.getGuild().getMember(msg.getAuthor()).getVoiceState().getChannel();

            String argString = msg.getContentRaw().replace(discordTrigger, "").toLowerCase(Locale.ROOT);
            String[] argList = argString.split(" ");
            String command = argList[0];
            argString = "";
            for (String str : argList) {
                if (command.equals(""))  {
                    command = str;
                } else {
                    argString += str + " ";
                }
            }
            argString = argString.trim();
            log.info("Command: " + command);
            log.info("argString: "+ argString);
            String vChannel = "";
            if (voiceChannel != null && voiceChannel.getName() != null) {
                vChannel = voiceChannel.getName();
            }
            log.info("Voice Channel: " + vChannel);

            if (command.equals("play")) command = "resume";
            if (command.equals("sub")) command = "subtitle";
            if (command.equals("yt")) command = "youtube";

            ProcessingConsultant pds = new ProcessingConsultant();
            PlexApiDataStore plex = new PlexApiDataStore();

            if (command.equals("ping")) {
                long time = System.currentTimeMillis();
                event.getTextChannel().sendMessage("Pong!") /* => RestAction<Message> */
                        .queue(response /* => Message */ -> {
                            response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                        });    
            } else if (command.equals("help")) {
                event.getTextChannel().sendMessage("```" + discordTrigger + " help - this message \n" +
                        discordTrigger + " clear - stop playback & clear the playlist \n" +
                        discordTrigger + " playlist - view the playlist \n" +
                        discordTrigger + " pause - pauses playback \n" +
                        discordTrigger + " resume - unpauses playback \n" +
                        discordTrigger + " next - plays the next item in the playlist \n" +
                        discordTrigger + " sub - list subtitle tracks and the current sub track\n" +
                        discordTrigger + " audio - list audio tracks and the current audio track\n" +
                        discordTrigger + " sub n - changes subtitles to sub track n\n" +
                        discordTrigger + " audio n - changes audio to audio track n\n```" +
                        "EXAMPLES\n" +
                        "```" + discordTrigger + " movie <movie title> \n" +
                        discordTrigger + " tv <my favorite show> \n" +
                        discordTrigger + " tv <my favorite show> (rando) \n" +
                        discordTrigger + " search <movie title> \n" +
                        discordTrigger + " tv <my favorite show> s03e07 \n" +
                        discordTrigger + " yt <youtube link>\n```").queue();
            } else if (basicCommands.contains(command)) {
                if (command.equals("search")) {
                    event.getTextChannel().sendMessage("```" + pds.doFuzzSearch(argString) + "```").queue();
                } else if (command.equals("playlist")) {
                    event.getTextChannel().sendMessage("```" + pds.listPlaylist() + "```").queue();
                }
            } else if (privCommands.contains(command)) {
                if (voiceChannel != null) {
                    if (pds.allowCall(vChannel)) {
                        application().setLastChannel(msg.getChannel());
                        if (command.equals("audio")) {
                            if (NumberUtils.isCreatable(argString)) pds.setAudioTrack(Integer.parseInt(argString));
                            event.getTextChannel().sendMessage("```" + pds.listAudioTracks() + "```").queue();
                        } else if (command.equals("clear")) {
                            application().clearPlayList();
                            application().mediaPlayer().controls().stop();
                            pds.doAhkDisconnect();
                            event.getTextChannel().sendMessage("```stopped and cleared```").queue();
                        } else if (command.equals("movie")) {
                            ArrayList<PlaylistItem> media = plex.getMovie(argString);
                            event.getTextChannel().sendMessage("```" + pds.playFile(media, vChannel) + "```").queue();
                        } else if (command.equals("next")) {
                            event.getTextChannel().sendMessage("```" + pds.playNext(application().mediaPlayer()) + "```").queue();
                        } else if (command.equals("pause")) {
                            application().mediaPlayer().controls().pause();
                            event.getTextChannel().sendMessage("```paused```").queue();
                        } else if (command.equals("resume")) {
                            application().mediaPlayer().controls().play();
                            event.getTextChannel().sendMessage("```resumed```").queue();
                        } else if (command.equals("subtitle")) {
                            if (NumberUtils.isCreatable(argString)) pds.setSubTrack(Integer.parseInt(argString));
                            event.getTextChannel().sendMessage("```" + pds.listSubtitleTracks() + "```").queue();
                        } else if (command.equals("tv")) {
                            ArrayList<PlaylistItem> episodes = plex.getTelevision(argString);
                            event.getTextChannel().sendMessage("```" + pds.playFile(episodes, vChannel) + "```").queue();
                        } else if (command.equals("youtube")) {
                            if (!argString.equals("") && (argString.contains("youtube.com") || argString.contains("youtu.be"))) {
                                application().setYtLastStart(new DateTime());
                                PlaylistItem media = new PlaylistItem();
                                media.setMrl(argString);
                                media.setTitle("YOUTUBE VIDEO");
                                pds.playFile(media, vChannel);
                                event.getTextChannel().sendMessage("```Playing Youtube```").queue();
                            } else {
                                event.getTextChannel().sendMessage("```Invalid Youtube URL```").queue();
                            }
                        }
                    } else {
                        event.getTextChannel().sendMessage("```Currently Playing in another channel, sorry!```").queue();
                    }
                } else {
                    event.getTextChannel().sendMessage("```You don\'t appear to be in a voice channel I can reach.```").queue();
                }
            } else {
                event.getTextChannel().sendMessage("```Sorry I don\'t know what to do with that. Try " + discordTrigger +" help```").queue();
            }
            
        }

    }
}
