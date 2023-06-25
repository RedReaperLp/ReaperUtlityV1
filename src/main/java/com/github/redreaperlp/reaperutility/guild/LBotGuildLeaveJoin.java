package com.github.redreaperlp.reaperutility.guild;

import com.github.redreaperlp.reaperutility.Main;
import com.github.redreaperlp.reaperutility.storage.JStorage;
import com.github.redreaperlp.reaperutility.storage.server.JGuild;
import com.github.redreaperlp.reaperutility.util.Color;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LBotGuildLeaveJoin extends ListenerAdapter {
    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        new Color.Print("The bot left the Guild ", Color.RED).append(event.getGuild().getName(), Color.YELLOW).append(" with the ID ", Color.RED).append(event.getGuild().getId(), Color.YELLOW).printInfo();
        JStorage.instance().removeGuild(event.getGuild().getIdLong());
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        new Color.Print("The bot joined the Guild ", Color.GREEN).append(event.getGuild().getName(), Color.YELLOW).append(" with the ID ", Color.GREEN).append(event.getGuild().getId(), Color.YELLOW).printInfo();
        JStorage.instance().addGuild(new JGuild(event.getGuild().getIdLong()));
        Main.prepareCommands(event.getGuild());
    }
}
