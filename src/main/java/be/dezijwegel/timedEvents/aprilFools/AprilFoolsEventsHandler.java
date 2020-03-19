package be.dezijwegel.timedEvents.aprilFools;

import be.dezijwegel.Runnables.PlaySoundRunnable;
import be.dezijwegel.Runnables.SendMessageRunnable;
import be.dezijwegel.customEvents.PlayersDidNotSleepEvent;
import be.dezijwegel.customEvents.TimeSetToDayEvent;
import be.dezijwegel.files.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AprilFoolsEventsHandler implements Listener {


    private Plugin plugin;
    private Lang lang;

    private Set<UUID> creeperPrankedList;   // A Set of player IDs that have already been creeper pranked
    private Set<UUID> explosionPrankedList; // A set of player IDs that have already been pranked with an explosion

    private Random random;
    private long lastNightPrank = 0;

    public AprilFoolsEventsHandler(Plugin plugin, Lang lang)
    {
        this.plugin = plugin;
        this.lang = lang;

        creeperPrankedList = new HashSet<>();
        explosionPrankedList = new HashSet<>();

        random = new Random();
    }


    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event)
    {
        UUID playerID = event.getPlayer().getUniqueId();

        // Only do the prank once for entering the bed
        if ( ! creeperPrankedList.contains(playerID)) {
            doCreeperSoundPrank(event.getPlayer());
            creeperPrankedList.add(playerID);
        }
    }


    @EventHandler
    public void onDidNotSleep(PlayersDidNotSleepEvent event)
    {
        for (Player p : event.getPlayers())
        {
            if (explosionPrankedList.contains(p.getUniqueId()))     // If player has been explosion pranked already
            {
                doCreeperSoundPrank(p);                             // Do the creeper prank
            } else {                                                // Player has NOT been explosion pranked
                doExplosionPrank(p);                                // Do explosion prank
                explosionPrankedList.add(p.getUniqueId());          // Add user to the pranked list
            }
        }
    }

    @EventHandler
    public void onTimeSetToDay(TimeSetToDayEvent event)
    {
        long now = System.currentTimeMillis();
        int randNum = random.nextInt(100);
        if (randNum < 3 && now - lastNightPrank > 50000) {                          // Approx. 1 in 50 times this prank happens
            doTimePrank(event.getWorld());
            lastNightPrank = System.currentTimeMillis();
        }
    }

    private void doCreeperSoundPrank(Player player)
    {
        BukkitRunnable hiss = new PlaySoundRunnable(player, Sound.ENTITY_CREEPER_PRIMED);
        BukkitRunnable boom = new PlaySoundRunnable(player, Sound.ENTITY_GENERIC_EXPLODE);
        BukkitRunnable msg = new SendMessageRunnable(lang, player, "april_fools_creeper_prank");

        hiss.runTask(plugin);
        boom.runTaskLater(plugin, 25L);
        msg.runTaskLater(plugin, 30L);
    }


    private void doExplosionPrank(Player player)
    {
        // Create explosion
        Location loc = player.getLocation();
        player.getWorld().createExplosion(loc, (float) 0.3, false, false);    // NO fire or explosion damage!

        // Send message
        lang.sendMessage("april_fools_explosion_prank", player);
    }


    private void doTimePrank(World world)
    {
        // Set the time to night
        SetTimeNightRunnable setNight = new SetTimeNightRunnable(world, lang);
        setNight.runTaskLater(plugin, 60L);
    }
}
