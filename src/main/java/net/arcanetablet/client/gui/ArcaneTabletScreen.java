package net.arcanetablet.client.gui;

import net.arcanetablet.client.ArcaneTabletClient;
import net.arcanetablet.data.ArcaneAction;
import net.arcanetablet.data.ArcaneCategory;
import net.arcanetablet.network.BankActionPayload;
import net.arcanetablet.network.ExecuteActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ArcaneTabletScreen extends Screen {

    private final ItemStack tabletStack;
    private ArcaneCategory activeCategory = ArcaneCategory.ENVIRONMENT;
    private int pageIndex = 0;

    private int guiLeft;
    private int guiTop;
    private final int guiWidth = 380;
    private final int guiHeight = 230;

    // Hover tooltip tracking
    private final List<Text> currentTooltip = new ArrayList<>();

    public ArcaneTabletScreen(Text title, ItemStack tabletStack) {
        super(title);
        this.tabletStack = tabletStack;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - this.guiWidth) / 2;
        this.guiTop = (this.height - this.guiHeight) / 2;

        rebuildGui();
    }

    public void refreshStatus() {
        rebuildGui();
    }

    private void rebuildGui() {
        this.clearChildren();

        // 1. Category Tab Buttons
        ArcaneCategory[] categories = ArcaneCategory.values();
        int tabWidth = 86;
        int tabHeight = 18;
        int tabSpacing = 4;
        int tabsStartX = guiLeft + 10;
        int tabsY = guiTop + 26;

        for (int i = 0; i < categories.length; i++) {
            ArcaneCategory cat = categories[i];
            boolean selected = (cat == activeCategory);
            int x = tabsStartX + i * (tabWidth + tabSpacing);

            String prefix = switch (cat) {
                case ENVIRONMENT -> "☁ ";
                case NAVIGATION -> "🧭 ";
                case SANCTUARY -> "🛡 ";
                case PRESERVATION -> "🔮 ";
            };

            ButtonWidget tabBtn = ButtonWidget.builder(
                    Text.literal(prefix + cat.getDisplayName()).formatted(selected ? Formatting.AQUA : Formatting.GRAY),
                    button -> {
                        activeCategory = cat;
                        pageIndex = 0;
                        if (client != null && client.player != null) {
                            client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.2f);
                        }
                        rebuildGui();
                    }
            ).dimensions(x, tabsY, tabWidth, tabHeight).build();

            this.addDrawableChild(tabBtn);
        }

        // 2. XP Bank Controls (Right Panel Clean Grid)
        int panelX = guiLeft + guiWidth - 122;
        int row1Y = guiTop + 50 + 46;
        int row2Y = guiTop + 50 + 63;

        // Row 1 Deposit Buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("+1"), b -> sendBankAction(0)).dimensions(panelX + 6, row1Y, 24, 14).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+5"), b -> sendBankAction(1)).dimensions(panelX + 33, row1Y, 24, 14).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("ALL"), b -> sendBankAction(4)).dimensions(panelX + 60, row1Y, 44, 14).build());

        // Row 2 Withdraw Buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("-1"), b -> sendBankAction(2)).dimensions(panelX + 6, row2Y, 24, 14).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("-5"), b -> sendBankAction(3)).dimensions(panelX + 33, row2Y, 24, 14).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("DRAIN"), b -> sendBankAction(5)).dimensions(panelX + 60, row2Y, 44, 14).build());

        // 3. Action Cards for Active Category
        List<ArcaneAction> categoryActions = new ArrayList<>();
        for (ArcaneAction action : ArcaneAction.values()) {
            if (action.getCategory() == activeCategory) {
                categoryActions.add(action);
            }
        }

        int pageSize = 4;
        int totalPages = (int) Math.ceil((double) categoryActions.size() / pageSize);
        if (pageIndex >= totalPages) pageIndex = 0;

        int startIndex = pageIndex * pageSize;
        int endIndex = Math.min(startIndex + pageSize, categoryActions.size());

        int startY = guiTop + 50;
        int cardWidth = 245;
        int cardHeight = 32;
        int cardSpacing = 3;

        int playerLvl = (client != null && client.player != null) ? client.player.experienceLevel : 0;
        int bankLvl = ArcaneTabletClient.BANK_LEVELS;

        for (int i = startIndex; i < endIndex; i++) {
            ArcaneAction action = categoryActions.get(i);
            int displayIdx = i - startIndex;
            int cardY = startY + displayIdx * (cardHeight + cardSpacing);
            int cardX = guiLeft + 12;

            boolean unlocked = ArcaneTabletClient.UNLOCKED_ACTIONS.contains(action.getId());
            int cost = action.getLevelCost();
            boolean canAfford = (playerLvl >= cost) || (bankLvl >= cost) || ((playerLvl + bankLvl) >= cost);

            boolean canExecute = unlocked && canAfford;

            int btnW = 60;
            int btnH = 20;
            int btnX = cardX + cardWidth - btnW - 6;
            int btnY = cardY + 6;

            ButtonWidget execBtn = ButtonWidget.builder(
                    Text.literal(canExecute ? "ENGAGE" : (unlocked ? "NO XP" : "LOCKED"))
                            .formatted(canExecute ? Formatting.GREEN : (unlocked ? Formatting.YELLOW : Formatting.RED)),
                    button -> {
                        if (canExecute) {
                            ClientPlayNetworking.send(new ExecuteActionPayload(action.getId(), bankLvl >= cost));
                        }
                    }
            ).dimensions(btnX, btnY, btnW, btnH).build();

            execBtn.active = canExecute;
            this.addDrawableChild(execBtn);
        }

        // Paging Buttons if category has > 4 actions
        if (totalPages > 1) {
            int pBtnY = guiTop + 191;
            ButtonWidget prevBtn = ButtonWidget.builder(Text.literal("◀"), b -> {
                if (pageIndex > 0) {
                    pageIndex--;
                    rebuildGui();
                }
            }).dimensions(guiLeft + 12, pBtnY, 20, 14).build();
            prevBtn.active = (pageIndex > 0);
            addDrawableChild(prevBtn);

            ButtonWidget nextBtn = ButtonWidget.builder(Text.literal("▶"), b -> {
                if (pageIndex < totalPages - 1) {
                    pageIndex++;
                    rebuildGui();
                }
            }).dimensions(guiLeft + 92, pBtnY, 20, 14).build();
            nextBtn.active = (pageIndex < totalPages - 1);
            addDrawableChild(nextBtn);
        }

        // Close Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("✖"), button -> close())
                .dimensions(guiLeft + guiWidth - 22, guiTop + 6, 16, 16).build());
    }

    private void sendBankAction(int type) {
        ClientPlayNetworking.send(new BankActionPayload(type));
    }

    private void drawHoloBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        currentTooltip.clear();

        // 1. Dim World Background
        context.fill(0, 0, this.width, this.height, 0x99000000);

        // 2. Futuristic Hologram Frame
        renderHologramBackdrop(context);

        // 3. Render Buttons & Components first
        super.render(context, mouseX, mouseY, delta);

        // 4. Render Headers & Telemetry
        renderHeaders(context);

        // 5. Render Active Category Action Cards (Text & Overlays)
        renderActionCards(context, mouseX, mouseY);

        // 6. Render Bank Panel (Header, Stats, and Status Box)
        renderBankPanel(context, mouseX, mouseY);

        // 7. Render Bottom Status Feed & Compass
        renderFooterStatus(context);

        // 8. Draw Tooltip
        if (!currentTooltip.isEmpty()) {
            context.drawTooltip(textRenderer, currentTooltip, mouseX, mouseY);
        }
    }

    private void renderHologramBackdrop(DrawContext context) {
        // Holographic Glass Body
        context.fillGradient(guiLeft, guiTop, guiLeft + guiWidth, guiTop + guiHeight, 0xF0040E14, 0xF00A1C28);

        // Neon Outer Border (Cyan glow)
        int borderColor = 0xFF00E5FF;
        drawHoloBorder(context, guiLeft, guiTop, guiWidth, guiHeight, borderColor);
        drawHoloBorder(context, guiLeft + 1, guiTop + 1, guiWidth - 2, guiHeight - 2, 0x5500E5FF);

        // Corner accents
        int accent = 0xFF00FFFF;
        context.fill(guiLeft - 1, guiTop - 1, guiLeft + 6, guiTop + 2, accent);
        context.fill(guiLeft - 1, guiTop - 1, guiLeft + 2, guiTop + 6, accent);

        context.fill(guiLeft + guiWidth - 6, guiTop - 1, guiLeft + guiWidth + 1, guiTop + 2, accent);
        context.fill(guiLeft + guiWidth - 2, guiTop - 1, guiLeft + guiWidth + 1, guiTop + 6, accent);

        context.fill(guiLeft - 1, guiTop + guiHeight - 2, guiLeft + 6, guiTop + guiHeight + 1, accent);
        context.fill(guiLeft - 1, guiTop + guiHeight - 6, guiLeft + 2, guiTop + guiHeight + 1, accent);

        context.fill(guiLeft + guiWidth - 6, guiTop + guiHeight - 2, guiLeft + guiWidth + 1, guiTop + guiHeight + 1, accent);
        context.fill(guiLeft + guiWidth - 2, guiTop + guiHeight - 6, guiLeft + guiWidth + 1, guiTop + guiHeight + 1, accent);

        // Top banner separator
        context.fill(guiLeft + 4, guiTop + 23, guiLeft + guiWidth - 4, guiTop + 24, 0x4400E5FF);
        // Bottom footer separator
        context.fill(guiLeft + 4, guiTop + guiHeight - 20, guiLeft + guiWidth - 4, guiTop + guiHeight - 19, 0x4400E5FF);
    }

    private void renderHeaders(DrawContext context) {
        // Left Title
        context.drawText(textRenderer, Text.literal("⚡ ARCANE TABLET").formatted(Formatting.AQUA, Formatting.BOLD), guiLeft + 12, guiTop + 8, 0xFF00E5FF, true);

        // Right Telemetry (Clean separated positioning)
        if (client != null && client.player != null) {
            BlockPos pPos = client.player.getBlockPos();
            String dim = client.player.getEntityWorld().getRegistryKey().getValue().getPath().toUpperCase();
            String telemetry = String.format("[%d, %d, %d] | %s", pPos.getX(), pPos.getY(), pPos.getZ(), dim);
            int textW = textRenderer.getWidth(telemetry);
            context.drawText(textRenderer, Text.literal(telemetry).formatted(Formatting.DARK_AQUA), guiLeft + guiWidth - textW - 28, guiTop + 9, 0xFF00A0B0, false);
        }
    }

    private void renderBankPanel(DrawContext context, int mouseX, int mouseY) {
        int panelX = guiLeft + guiWidth - 122;
        int panelY = guiTop + 50;
        int panelW = 110;
        int panelH = 155;

        // Sub-panel box
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0x55001520);
        drawHoloBorder(context, panelX, panelY, panelW, panelH, 0x7700E5FF);

        // Panel Title
        context.drawText(textRenderer, Text.literal("⚡ ENERGY CORE").formatted(Formatting.AQUA, Formatting.BOLD), panelX + 8, panelY + 6, 0xFF00E5FF, false);
        context.fill(panelX + 4, panelY + 16, panelX + panelW - 4, panelY + 17, 0x4400E5FF);

        // Stat readouts
        int pLvl = (client != null && client.player != null) ? client.player.experienceLevel : 0;
        int bLvl = ArcaneTabletClient.BANK_LEVELS;

        context.drawText(textRenderer, Text.literal("Player: §e" + pLvl + " Lvl"), panelX + 8, panelY + 21, 0xFFCCCCCC, false);
        context.drawText(textRenderer, Text.literal("Tablet: §a" + bLvl + " Lvl"), panelX + 8, panelY + 33, 0xFF55FF55, false);

        // Active Buff / Status bottom box
        int subBoxY = panelY + 82;
        int subBoxH = panelH - 86;
        context.fill(panelX + 4, subBoxY, panelX + panelW - 4, subBoxY + subBoxH, 0x44000000);
        drawHoloBorder(context, panelX + 4, subBoxY, panelW - 8, subBoxH, 0x4400E5FF);

        if (ArcaneTabletClient.TETHER_SEC_REMAINING > 0) {
            context.drawText(textRenderer, Text.literal("§d🔮 TETHER").formatted(Formatting.BOLD), panelX + 8, subBoxY + 6, 0xFFB388FF, false);
            long mins = ArcaneTabletClient.TETHER_SEC_REMAINING / 60;
            long secs = ArcaneTabletClient.TETHER_SEC_REMAINING % 60;
            context.drawText(textRenderer, Text.literal(String.format("§f%02d:%02d §7remaining", mins, secs)), panelX + 8, subBoxY + 22, 0xFFDDDDDD, false);
            context.drawText(textRenderer, Text.literal("§a✔ Saved On Death"), panelX + 8, subBoxY + 38, 0xFF55FF55, false);
        } else if (activeCategory == ArcaneCategory.NAVIGATION) {
            context.drawText(textRenderer, Text.literal("§e🧭 RADAR").formatted(Formatting.BOLD), panelX + 8, subBoxY + 6, 0xFFFFD700, false);
            if (!ArcaneTabletClient.LAST_SCAN_STRUCT.isEmpty() && ArcaneTabletClient.LAST_SCAN_POS != null) {
                String sName = ArcaneTabletClient.LAST_SCAN_STRUCT;
                if (sName.length() > 13) sName = sName.substring(0, 11) + "..";
                context.drawText(textRenderer, Text.literal("§a" + sName), panelX + 8, subBoxY + 20, 0xFF55FF55, false);
                context.drawText(textRenderer, Text.literal("§7" + ArcaneTabletClient.LAST_SCAN_DIST + "m away"), panelX + 8, subBoxY + 34, 0xFFCCCCCC, false);
                context.drawText(textRenderer, Text.literal("§8[" + ArcaneTabletClient.LAST_SCAN_POS.getX() + ", " + ArcaneTabletClient.LAST_SCAN_POS.getZ() + "]"), panelX + 8, subBoxY + 47, 0xFF888888, false);
            } else {
                context.drawText(textRenderer, Text.literal("§7Radar: §fIdle"), panelX + 8, subBoxY + 22, 0xFFCCCCCC, false);
                context.drawText(textRenderer, Text.literal("§8Ready to scan"), panelX + 8, subBoxY + 38, 0xFF888888, false);
            }
        } else {
            int totalEnergy = pLvl + bLvl;
            context.drawText(textRenderer, Text.literal("§b⚡ CORE ONLINE").formatted(Formatting.BOLD), panelX + 8, subBoxY + 6, 0xFF00E5FF, false);
            context.drawText(textRenderer, Text.literal("§7Pool: §e" + totalEnergy + " Lvl §7avail"), panelX + 8, subBoxY + 22, 0xFFEEEEEE, false);
            context.drawText(textRenderer, Text.literal("§a✔ Soul XP Linked"), panelX + 8, subBoxY + 38, 0xFF55FF55, false);
        }
    }

    private void renderActionCards(DrawContext context, int mouseX, int mouseY) {
        List<ArcaneAction> categoryActions = new ArrayList<>();
        for (ArcaneAction action : ArcaneAction.values()) {
            if (action.getCategory() == activeCategory) {
                categoryActions.add(action);
            }
        }

        int pageSize = 4;
        int totalPages = (int) Math.ceil((double) categoryActions.size() / pageSize);
        if (pageIndex >= totalPages) pageIndex = 0;

        int startIndex = pageIndex * pageSize;
        int endIndex = Math.min(startIndex + pageSize, categoryActions.size());

        int startY = guiTop + 50;
        int cardWidth = 245;
        int cardHeight = 32;
        int cardSpacing = 3;

        int playerLvl = (client != null && client.player != null) ? client.player.experienceLevel : 0;
        int bankLvl = ArcaneTabletClient.BANK_LEVELS;

        for (int i = startIndex; i < endIndex; i++) {
            ArcaneAction action = categoryActions.get(i);
            int displayIdx = i - startIndex;
            int cardY = startY + displayIdx * (cardHeight + cardSpacing);
            int cardX = guiLeft + 12;

            boolean isHovered = mouseX >= cardX && mouseX <= cardX + cardWidth && mouseY >= cardY && mouseY <= cardY + cardHeight;
            boolean unlocked = ArcaneTabletClient.UNLOCKED_ACTIONS.contains(action.getId());
            int cost = action.getLevelCost();
            boolean canAfford = (playerLvl >= cost) || (bankLvl >= cost) || ((playerLvl + bankLvl) >= cost);

            // Card background
            int bgColor = isHovered ? 0x66003344 : 0x44001a24;
            context.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, bgColor);

            int cardBorder = unlocked ? (canAfford ? 0x8800E5FF : 0x88E5A000) : 0x66882222;
            drawHoloBorder(context, cardX, cardY, cardWidth, cardHeight, cardBorder);

            // Cost Badge (aligned to left of button)
            String costText = cost + " LVL";
            int costW = textRenderer.getWidth(costText);
            int btnX = cardX + cardWidth - 60 - 6;
            int costX = btnX - costW - 6;
            context.drawText(textRenderer, Text.literal(costText).formatted(Formatting.GOLD), costX, cardY + 4, 0xFFFFAA00, false);

            // Action Title (bounded by cost badge position)
            int titleColor = unlocked ? 0xFFFFFFFF : 0xFF888888;
            int maxTitleWidth = costX - (cardX + 6) - 4;
            String titleStr = textRenderer.trimToWidth(action.getTitle(), maxTitleWidth);
            context.drawText(textRenderer, Text.literal(titleStr).formatted(unlocked ? Formatting.WHITE : Formatting.DARK_GRAY, Formatting.BOLD), cardX + 6, cardY + 4, titleColor, false);

            // Action Description (bounded by button position)
            int maxDescWidth = btnX - (cardX + 6) - 4;
            String descStr = textRenderer.trimToWidth(action.getDescription(), maxDescWidth);
            context.drawText(textRenderer, Text.literal(descStr).formatted(Formatting.GRAY), cardX + 6, cardY + 17, 0xFFAAAAAA, false);

            // Hover tooltip for requirements & details
            if (isHovered && mouseX < cardX + cardWidth - 65) {
                currentTooltip.add(Text.literal(action.getTitle()).formatted(Formatting.AQUA, Formatting.BOLD));
                currentTooltip.add(Text.literal(action.getDescription()).formatted(Formatting.WHITE));
                currentTooltip.add(Text.literal("§6Energy Cost: §e" + cost + " XP Levels"));
                if (unlocked) {
                    currentTooltip.add(Text.literal("§a✔ Unlocked & Operational").formatted(Formatting.GREEN));
                    currentTooltip.add(Text.literal("§7Advancement: §f" + action.getAdvancementName()).formatted(Formatting.GRAY));
                } else {
                    currentTooltip.add(Text.literal("§c✖ LOCKED - Requires Advancement:").formatted(Formatting.RED));
                    currentTooltip.add(Text.literal("§e" + action.getAdvancementName()).formatted(Formatting.YELLOW));
                }
            }
        }

        // Paging page number text
        if (totalPages > 1) {
            String pageStr = String.format("PAGE %d / %d", pageIndex + 1, totalPages);
            context.drawText(textRenderer, Text.literal(pageStr).formatted(Formatting.AQUA), guiLeft + 36, guiTop + 195, 0xFF00E5FF, false);
        }
    }

    private void renderFooterStatus(DrawContext context) {
        int footerY = guiTop + guiHeight - 14;
        int statusColor = switch (ArcaneTabletClient.STATUS_CODE) {
            case 1 -> 0xFF55FF55; // Green
            case 2 -> 0xFFFF5555; // Red
            default -> 0xFF00E5FF; // Cyan
        };

        String msg = "STATUS: " + ArcaneTabletClient.STATUS_MESSAGE;
        if (msg.length() > 36) msg = msg.substring(0, 34) + "..";
        context.drawText(textRenderer, Text.literal(msg), guiLeft + 12, footerY, statusColor, true);

        // Dynamic World Mode Indicator on bottom right
        boolean isOp = ArcaneTabletClient.IS_OP;
        String modeText = isOp ? "§7WORLD: §eCHEATS ON §6[OP]" : "§7WORLD: §aPURE SURVIVAL §b[LEGIT]";
        int badgeW = textRenderer.getWidth(Text.literal(modeText));
        int badgeX = guiLeft + guiWidth - badgeW - 14;
        context.drawText(textRenderer, Text.literal(modeText), badgeX, footerY, 0xFFFFFFFF, false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
