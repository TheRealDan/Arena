package me.therealdan.battlearena.mechanics.lobby;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.setup.SetupHandler;
import me.therealdan.battlearena.util.Icon;
import me.therealdan.battlearena.util.PlayerHandler;
import me.therealdan.battlearena.util.WXYZ;
import me.therealdan.battlearena.util.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class Lobby implements Listener {

    private static Lobby lobby;

    private YamlFile yamlFile;

    private WXYZ spawnpoint;
    private ItemStack createBattleIcon;
    private boolean teleportOnJoin;

    private HashSet<UUID> uiOpen = new HashSet<>();

    private Lobby() {
        teleportOnJoin = BattleArena.getInstance().getConfig().getBoolean("Teleport_On_Join");

        if (getYamlFile().getData().contains("Lobby.Spawnpoint"))
            spawnpoint = new WXYZ(getYamlFile().getData().getString("Lobby.Spawnpoint"));

        Bukkit.getScheduler().scheduleSyncRepeatingTask(BattleArena.getInstance(), () -> tick(), 100, 1);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (contains(player)) {
                player.setFoodLevel(20);
                player.setFireTicks(0);
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    public void join(Player player) {
        if (spawnpoint == null) return;
        player.teleport(getSpawnpoint());
        PlayerHandler.refresh(player);
    }

    public void open(Player player) {
        int size = 9;
        while (size < Battle.values().size() + 1) size += 9;
        if (size > 54) size = 54;
        Inventory inventory = Bukkit.createInventory(null, size, "");

        for (Battle battle : Battle.values())
            if (battle.isOpen())
                inventory.addItem(getJoinBattleIcon(battle));

        inventory.setItem(size - 1, getCreateBattleIcon());

        player.openInventory(inventory);
        uiOpen.add(player.getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!hasUIOpen(player)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (!event.getCurrentItem().hasItemMeta()) return;
        if (!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        if (getCreateBattleIcon().isSimilar(event.getCurrentItem())) {
            SetupHandler.getInstance().open(player);
        } else {
            for (Battle battle : Battle.values()) {
                if (battle.isOpen()) {
                    if (event.getCurrentItem().isSimilar(getJoinBattleIcon(battle))) {
                        battle.add(player);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!hasUIOpen(player)) return;
        uiOpen.remove(player.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (teleportOnJoin)
            join(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (contains(player))
            if (player.getGameMode().equals(GameMode.SURVIVAL))
                event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (contains(player))
            if (player.getGameMode().equals(GameMode.SURVIVAL))
                event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;

        Player player = event.getPlayer();
        if (contains(player))
            if (player.getGameMode().equals(GameMode.SURVIVAL))
                event.setCancelled(true);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (contains(player))
            if (player.getGameMode().equals(GameMode.SURVIVAL))
                event.setCancelled(true);
    }

    @EventHandler
    public void onEntity(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        if (contains(player))
            if (player.getGameMode().equals(GameMode.SURVIVAL))
                event.setCancelled(true);
    }

    @EventHandler
    public void onHanging(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player)) return;
        Player player = (Player) event.getRemover();
        if (contains(player))
            if (player.getGameMode().equals(GameMode.SURVIVAL))
                event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCombat(EntityDamageByEntityEvent event) {
        Player player = null;
        if (event.getDamager() instanceof Player) player = (Player) event.getDamager();
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) player = (Player) projectile.getShooter();
        }

        if (player == null) return;

        if (contains(player)) {
            if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                event.setCancelled(true);
                if (event.getDamager() instanceof Projectile) event.getDamager().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!contains(player)) return;

        event.setCancelled(true);
    }

    public void setSpawnpoint(Location location) {
        this.spawnpoint = new WXYZ(location);
    }

    public void unload() {
        if (spawnpoint != null)
            getYamlFile().getData().set("Lobby.Spawnpoint", spawnpoint.getWxyz());

        getYamlFile().save();
    }

    public boolean contains(Player player) {
        if (spawnpoint == null) return false;
        if (Battle.get(player) != null) return false;

        return spawnpoint.getLocation().getWorld() == player.getLocation().getWorld();
    }

    public boolean hasUIOpen(Player player) {
        return uiOpen.contains(player.getUniqueId());
    }

    public Location getSpawnpoint() {
        if (spawnpoint == null) return null;
        return spawnpoint.getLocation();
    }

    private ItemStack getJoinBattleIcon(Battle battle) {
        List<String> lore = new ArrayList<>();
        ItemStack icon = Icon.build(BattleArena.getInstance().getConfig(), "Join_UI.Join_Battle.Icon");
        ItemMeta itemMeta = icon.getItemMeta();

        String players = Integer.toString(battle.getPlayers().size());

        for (String line : itemMeta.getLore()) {
            lore.add(line
                    .replace("%players%", players)
                    .replace("%battle%", battle.getBattleType().getName())
                    .replace("%arena%", battle.getArena().getName())
                    .replace("%recommendedplayers%", battle.getArena().getRecommendedPlayers())
            );
        }
        itemMeta.setLore(lore);

        icon.setItemMeta(itemMeta);
        return icon;
    }

    private ItemStack getCreateBattleIcon() {
        if (createBattleIcon == null)
            createBattleIcon = Icon.build(BattleArena.getInstance().getConfig(), "Join_UI.Create_Battle.Icon", true);
        return createBattleIcon;
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers())
            if (contains(player))
                players.add(player);
        return players;
    }

    private YamlFile getYamlFile() {
        if (yamlFile == null) yamlFile = new YamlFile("data/lobby.yml");
        return yamlFile;
    }

    public static Lobby getInstance() {
        if (lobby == null) lobby = new Lobby();
        return lobby;
    }
}