package md.mirrerror.discordutils.discord.listeners;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.cache.DiscordUtilsUsersCacheManager;
import md.mirrerror.discordutils.config.messages.Message;
import md.mirrerror.discordutils.models.DiscordUtilsUser;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DiscordUnlinkListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(event.getChannelType() != ChannelType.PRIVATE) return;
        if(event.getUser().equals(Main.getInstance().getBot().getJda().getSelfUser())) return;
        if(!event.getChannel().asPrivateChannel().equals(event.getUser().openPrivateChannel().complete())) return;

        DiscordUtilsUser discordUtilsUser = DiscordUtilsUsersCacheManager.getFromCacheByUserId(event.getUser().getIdLong());
        if(!discordUtilsUser.isLinked()) return;

        long messageId = event.getMessageIdLong();
        UUID uuid = discordUtilsUser.getOfflinePlayer().getUniqueId();

        if(Main.getInstance().getBot().getUnlinkPlayers().containsKey(uuid)) {
            if(Main.getInstance().getBot().getUnlinkPlayers().get(uuid).getIdLong() == messageId) {

                if(event.getComponentId().equals("accept")) {
                    Main.getInstance().getBot().unAssignVerifiedRole(discordUtilsUser.getUser().getIdLong());

                    Main.getInstance().getBot().getUnlinkPlayers().remove(uuid);
                    if(discordUtilsUser.getOfflinePlayer().isOnline()) Message.ACCOUNT_SUCCESSFULLY_UNLINKED.send(discordUtilsUser.getOfflinePlayer().getPlayer(), true);
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        Main.getInstance().getBotSettings().COMMANDS_AFTER_UNLINKING.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", discordUtilsUser.getOfflinePlayer().getName())));
                    });

                    discordUtilsUser.unregister();
                }
                if(event.getComponentId().equals("decline")) {
                    Main.getInstance().getBot().getUnlinkPlayers().remove(uuid);
                    if(discordUtilsUser.getOfflinePlayer().isOnline()) Message.ACCOUNT_UNLINK_CANCELLED.send(discordUtilsUser.getOfflinePlayer().getPlayer(), true);
                }

                event.getChannel().deleteMessageById(event.getMessageId()).queue();

            }
        }
    }

}
