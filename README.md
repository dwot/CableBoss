vlcj-player - customized for discord playback
===========

This is a forked version of [vlcj-player](https://github.com/caprica/vlcj-player) with an embedded web server (Jetty).

This webserver listens for bot commands from [CableBoss](https://github.com/dwot/CableBoss) to search for and playback 
media in a Plex library or playback a youtube video and stream this on discord.

This is done with by running on a dedicated PC using a dedicated discord (non-bot) user and using AutoHotKey scripts to 
join channels & start / stop streaming.
