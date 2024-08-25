package ovh.mythmc.bancobankextension.containers;

import me.dablakbandit.bank.player.info.BankItemsInfo;
import me.dablakbandit.bank.player.info.item.BankItem;
import me.dablakbandit.core.players.CorePlayerManager;
import me.dablakbandit.core.players.CorePlayers;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ovh.mythmc.banco.api.Banco;
import ovh.mythmc.banco.api.accounts.Account;
import ovh.mythmc.banco.api.bukkit.inventories.BancoContainerBukkit;
import ovh.mythmc.banco.api.bukkit.util.ItemUtil;
import ovh.mythmc.banco.api.items.BancoItem;

import java.math.BigDecimal;
import java.util.*;

public final class BankContainer extends BancoContainerBukkit {

    @Override
    public @NotNull List<ItemStack> get(UUID uuid) {
        List<ItemStack> items = new ArrayList<>();

        CorePlayers player = CorePlayerManager.getInstance().getPlayer(uuid);
        if (player == null)
            return List.of();

        BankItemsInfo bankItemsInfo = player.getInfo(BankItemsInfo.class);
        if (bankItemsInfo == null)
            return List.of();
        int maxTabs = bankItemsInfo.getMaxTabNotEmpty();
        for (int i = 0; i <= maxTabs; i++) {
            bankItemsInfo.getTabBankItems(i).forEach(bankItem -> {
                ItemStack itemStack = bankItem.getItemStack();
                itemStack.setAmount(bankItem.getAmount());
                items.add(itemStack);
            });
        }

        return items;
    }

    @Override
    public @NotNull Integer maxSize() {
        return 0;
    }

    @Override
    public @NotNull BigDecimal add(UUID uuid, BigDecimal amount) {
        BigDecimal amountGiven = BigDecimal.valueOf(0);

        for (ItemStack item : ItemUtil.convertAmountToItems(amount)) {
            BancoItem bancoItem = ItemUtil.getBancoItem(item);
            if (bancoItem != null)
                amountGiven = amountGiven.add(Banco.get().getItemManager().value(bancoItem, item.getAmount()));

            CorePlayers player = CorePlayerManager.getInstance().getPlayer(uuid);
            if (player == null) continue;

            BankItemsInfo bankItemsInfo = player.getInfo(BankItemsInfo.class);
            BankItem bankItem = new BankItem(ItemUtil.getItemStack(bancoItem, item.getAmount()), item.getAmount());
            bankItemsInfo.getTabBankItems(bankItemsInfo.getMaxTabNotEmpty()).add(bankItem);
        }

        return amountGiven;
    }

    @Override
    public @NotNull BigDecimal remove(UUID uuid, BigDecimal amount) {
        for (ItemStack item : get(uuid).reversed()) {
            if (item == null) continue;
            if (amount.compareTo(BigDecimal.valueOf(0.01)) < 0) continue;

            BigDecimal value = BigDecimal.valueOf(0);

            BancoItem bancoItem = ItemUtil.getBancoItem(item);
            if (bancoItem != null)
                value = value.add(Banco.get().getItemManager().value(bancoItem, item.getAmount()));

            if (value.compareTo(BigDecimal.valueOf(0)) > 0) {
                CorePlayers player = CorePlayerManager.getInstance().getPlayer(uuid);
                if (player == null) continue;

                BankItemsInfo bankItemsInfo = player.getInfo(BankItemsInfo.class);

                boolean removed = false;
                for (int i = 0; i <= bankItemsInfo.getTotalTabCount(); i++) {
                    for (BankItem bankItem : List.copyOf(bankItemsInfo.getTabBankItems(i))) {
                        if (removed) break;

                        if (bankItem.getItemStack().equals(item)) {
                            bankItemsInfo.getTabBankItems(i).remove(bankItem);
                            removed = true;
                        }
                    }
                }

                BigDecimal added = BigDecimal.valueOf(0);
                if (value.compareTo(amount) > 0) {
                    added = value.subtract(amount);
                    Account account = Banco.get().getAccountManager().get(uuid);
                    if (account != null)
                        add(uuid, added);
                }

                amount = amount.subtract(value.subtract(added));
            }
        }

        return amount;
    }

}
