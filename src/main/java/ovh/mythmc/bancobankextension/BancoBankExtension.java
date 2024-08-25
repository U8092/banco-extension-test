package ovh.mythmc.bancobankextension;

import org.bukkit.plugin.java.JavaPlugin;
import ovh.mythmc.banco.api.Banco;
import ovh.mythmc.bancobankextension.containers.BankContainer;

public class BancoBankExtension extends JavaPlugin {

    private BankContainer container;

    @Override
    public void onEnable() {
        container = new BankContainer();
        Banco.get().getStorageManager().registerStorage(container);
    }

    @Override
    public void onDisable() {
        Banco.get().getStorageManager().unregisterStorage(container);
    }

}
