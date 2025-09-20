package me.gomania.beautifulbans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class BeautifulBans extends JavaPlugin implements CommandExecutor {

    private String banCommand;
    private String banBroadcast;
    private String banReasonFormat;
    private final Random random = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.banCommand = getConfig().getString("ban-command", "ban %player% %reason% %time%");
        this.banBroadcast = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("ban-broadcast", "&cИгрок %player% был красиво забанен!"));
        this.banReasonFormat = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("ban-reason-chat", "Причина: %reason%"));
        getCommand("beautifulban").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) && args.length < 1) {
            sender.sendMessage("§cИспользование: /beautifulban <ник> <причина> <время>");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cИспользование: /beautifulban <ник> <причина> <время>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cИгрок не найден.");
            return true;
        }

        String reason = args.length > 1 ? args[1] : "Нарушение правил";
        String time = args.length > 2 ? args[2] : "Навсегда";

        startBanAnimation(target, reason, time);
        return true;
    }

    private void startBanAnimation(Player target, String reason, String time) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks < 40) { // 2 секунды анимации
                    Color color = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                    DustOptions dust = new DustOptions(color, 1.5F);

                    target.getWorld().spawnParticle(
                            Particle.REDSTONE,
                            target.getLocation().add(0, 1, 0),
                            20,
                            0.5, 0.5, 0.5,
                            dust
                    );
                    target.setVelocity(target.getVelocity().setY(0.3));
                } else {
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
                    target.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, target.getLocation(), 1);

                    target.setHealth(0.0);

                    String cmd = banCommand.replace("%player%", target.getName())
                            .replace("%reason%", reason)
                            .replace("%time%", time);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);

                    // Сообщение с кастомной причиной
                    String reasonMessage = banReasonFormat.replace("%reason%", reason).replace("%player%", target.getName());
                    Bukkit.broadcastMessage(banBroadcast.replace("%player%", target.getName()));
                    Bukkit.broadcastMessage(reasonMessage);

                    cancel();
                }
                ticks += 5;
            }
        }.runTaskTimer(this, 0L, 5L);
    }
}
