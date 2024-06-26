package de.hysky.skyblocker.skyblock.quicknav;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class QuickNav {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuickNav.class);

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().quickNav.enableQuickNav && screen instanceof HandledScreen<?> && client.player != null && !client.player.isCreative()) {
                String screenTitle = screen.getTitle().getString().trim();
                List<QuickNavButton> buttons = QuickNav.init(screenTitle);
                for (QuickNavButton button : buttons) Screens.getButtons(screen).add(button);
            }
        });
    }

    public static List<QuickNavButton> init(String screenTitle) {
        List<QuickNavButton> buttons = new ArrayList<>();
        QuickNavigationConfig data = SkyblockerConfigManager.get().quickNav;
        try {
            if (data.button1.render) buttons.add(parseButton(data.button1, screenTitle, 0));
            if (data.button2.render) buttons.add(parseButton(data.button2, screenTitle, 1));
            if (data.button3.render) buttons.add(parseButton(data.button3, screenTitle, 2));
            if (data.button4.render) buttons.add(parseButton(data.button4, screenTitle, 3));
            if (data.button5.render) buttons.add(parseButton(data.button5, screenTitle, 4));
            if (data.button6.render) buttons.add(parseButton(data.button6, screenTitle, 5));
            if (data.button7.render) buttons.add(parseButton(data.button7, screenTitle, 6));
            if (data.button8.render) buttons.add(parseButton(data.button8, screenTitle, 7));
            if (data.button9.render) buttons.add(parseButton(data.button9, screenTitle, 8));
            if (data.button10.render) buttons.add(parseButton(data.button10, screenTitle, 9));
            if (data.button11.render) buttons.add(parseButton(data.button11, screenTitle, 10));
            if (data.button12.render) buttons.add(parseButton(data.button12, screenTitle, 11));
        } catch (CommandSyntaxException e) {
            LOGGER.error("[Skyblocker] Failed to initialize Quick Nav Button", e);
        }
        return buttons;
    }

    private static QuickNavButton parseButton(QuickNavigationConfig.QuickNavItem buttonInfo, String screenTitle, int id) throws CommandSyntaxException {
        QuickNavigationConfig.ItemData itemData = buttonInfo.item;
        ItemStack stack = ItemStackComponentizationFixer.fromComponentsString(itemData.id, Math.clamp(itemData.count, 1, 99), itemData.components);
        boolean uiTitleMatches = false;

        try {
            uiTitleMatches = screenTitle.matches(buttonInfo.uiTitle);
        } catch (PatternSyntaxException e) {
            LOGGER.error("[Skyblocker] Failed to parse Quick Nav Button", e);
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.sendMessage(Text.of(Formatting.RED + "[Skyblocker] Invalid regex in quicknav button " + (id + 1) + "!"), false);
            }
        }
        return new QuickNavButton(id,
                uiTitleMatches,
                buttonInfo.clickEvent,
                stack);
    }
}
