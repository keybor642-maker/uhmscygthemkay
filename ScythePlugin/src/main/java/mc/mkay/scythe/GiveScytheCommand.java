package mc.mkay.scythe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveScytheCommand implements CommandExecutor {

    private final ScythePlugin plugin;

    public GiveScytheCommand(ScythePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Only ops / console can run this
        if (!sender.isOp() && !(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }

        // /givescythe [player] — defaults to mkaymc
        String targetName = args.length > 0 ? args[0] : "mkaymc";
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(Component.text("Player '" + targetName + "' is not online.", NamedTextColor.RED));
            return true;
        }

        ItemStack scythe = ScytheItem.build(plugin);
        target.getInventory().addItem(scythe);

        target.sendMessage(
            Component.text("✿ ", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text("The relic has found you.", NamedTextColor.DARK_PURPLE))
        );
        sender.sendMessage(
            Component.text("Gave Bloom's Scythe to " + target.getName(), NamedTextColor.GREEN)
        );
        return true;
    }
}
