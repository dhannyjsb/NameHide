package codes.zucker.NameHide;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.Vector;

public class Think implements Runnable {


    static ScoreboardManager manager = Bukkit.getScoreboardManager();
    static Scoreboard board;

    static {
        assert manager != null;
        board = manager.getNewScoreboard();
    }

    Team t = board.registerNewTeam("defaultTeam");
    boolean sneakHidesName = (boolean)ConfigurationLoader.GetValue("sneakHidesName");
    int hideNameDistance = (int)ConfigurationLoader.GetValue("sneakHideDistance");
    boolean sprintShowName = (boolean)ConfigurationLoader.GetValue("showNameWhenSprinting");

    @Override
    public void run() {
        t.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
        for(Player p : Bukkit.getServer().getOnlinePlayers()) {

            t.addEntry(p.getName());
            p.setScoreboard(board);
    
            List<Player> visiblePlayers = new ArrayList<>();

            List<Entity> entList = p.getNearbyEntities(50, 50, 50);
            for (Entity ent : entList) {
                if (!(ent instanceof Player)) continue;
                Player pl = (Player) ent;
                if (pl == p) continue;
              //  Bukkit.getLogger().severe(pl.getName());
                boolean bottom = Utils.PlayerCanSee(p, pl.getLocation().add(0, 0.5f, 0), sprintShowName && pl.isSprinting());
                boolean top = Utils.PlayerCanSee(p, pl.getLocation().add(0, 1.8f, 0), sprintShowName && pl.isSprinting());
                if (pl.isSneaking())
                    top = Utils.PlayerCanSee(p, pl.getLocation().add(0, 1.2f, 0), false);

                if (bottom || top) {
                    if (sneakHidesName && pl.isSneaking() && p.getLocation().distance(pl.getLocation()) > hideNameDistance)
                        continue;
                    visiblePlayers.add(pl);
                }
            }

            List<UUID> allPlayerSees = new ArrayList<>();
            if (PlayerStand.Stands.get(p.getUniqueId()) != null)
                for(PlayerStand stand : PlayerStand.Stands.get(p.getUniqueId()))
                    allPlayerSees.add(stand.target.getUniqueId());

            for(Player pl : visiblePlayers) {
                boolean isCitizensNPC = pl.hasMetadata("NPC");
                allPlayerSees.remove(pl.getUniqueId());
                EntityArmorStand id = PlayerStand.GetStandEntityForPlayer(p, pl);
                Vector pos = pl.getLocation().toVector();

                float offset = 1.85f;
                if (pl.isSneaking())
                    offset = 1.4f;
                if (id == null) {

                    if (!isCitizensNPC){
                    EntityArmorStand stand = new EntityArmorStand(((CraftWorld)p.getWorld()).getHandle().getMinecraftWorld(), pos.getX(), pos.getY() + offset, pos.getZ());
                    stand.j(true);
                    stand.b(IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + pl.getName() + "\"}"));
                    stand.n(true);
                    stand.t(true);
    
                    PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(stand);
                    ((CraftPlayer)p).getHandle().b.a(packet);
    
                    DataWatcher watcher = stand.ai();
                    PacketPlayOutEntityMetadata data = new PacketPlayOutEntityMetadata(stand.ae(), watcher, false);
                    ((CraftPlayer)p).getHandle().b.a(data);
    
                    new PlayerStand(p, pl, stand);
                    }
                } else {
                    id.a(pos.getX(), pos.getY() + offset, pos.getZ(), 0, 0);
                    PacketPlayOutEntityTeleport posPacket = new PacketPlayOutEntityTeleport(id);
                    ((CraftPlayer)p).getHandle().b.a(posPacket);
                }
            }

            for(UUID uuid : allPlayerSees) {
                PlayerStand stand = PlayerStand.GetStandForPlayer(p, Bukkit.getPlayer(uuid));
                if (stand != null){
                    stand.Remove();
                    //ukkit.getConsoleSender().sendMessage("§8+-----------------------REMOVE ?" + stand);
                }
            }

        }
    }
}