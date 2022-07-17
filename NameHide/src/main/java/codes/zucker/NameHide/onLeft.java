package codes.zucker.NameHide;

import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class onLeft implements Listener {


    private final Main plugin;

    public onLeft(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        for(Player p : Bukkit.getServer().getOnlinePlayers()) {
            PlayerStand stand = PlayerStand.GetStandForOwner(p);
            if (stand != null){
                stand.Remove();
            }
        }

    }

}
