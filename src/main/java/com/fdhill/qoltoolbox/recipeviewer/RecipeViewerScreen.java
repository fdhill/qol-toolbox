package com.fdhill.qoltoolbox.recipeviewer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// ponytail: display-only MVP — no ingredient auto-transfer, first matching stack per
// ingredient (tag alternatives hidden), no scrollbar widget (mouse wheel only).
public class RecipeViewerScreen extends Screen {
	private static final int COLS = 8;
	private static final int VISIBLE_ROWS = 6;
	private static final int CELL = 18;
	private static final int PANEL_W = COLS * CELL + 210 + 14;
	private static final int PANEL_H = VISIBLE_ROWS * CELL + 32;

	private final List<RecipeEntry<CraftingRecipe>> all = new ArrayList<>();
	private List<RecipeEntry<CraftingRecipe>> filtered = List.of();
	private TextFieldWidget search;
	private int scroll;
	private int selected = -1;
	private int panelX;
	private int panelY;

	public RecipeViewerScreen() {
		super(Text.translatable("screen.qoltoolbox.recipeviewer"));
	}

	@Override
	protected void init() {
		panelX = (width - PANEL_W) / 2;
		panelY = (height - PANEL_H) / 2;
		MinecraftClient mc = MinecraftClient.getInstance();
		all.clear();
		all.addAll(mc.world.getRecipeManager().listAllOfType(RecipeType.CRAFTING));
		all.sort(Comparator.comparing(e ->
				e.value().getResult(mc.world.getRegistryManager()).getName().getString()));

		search = new TextFieldWidget(textRenderer, panelX + 6, panelY + 6, COLS * CELL + 4, 14,
				Text.translatable("screen.qoltoolbox.recipeviewer"));
		search.setChangedListener(s -> refilter());
		addDrawableChild(search);
		setInitialFocus(search);
		refilter();
	}

	private void refilter() {
		String q = search.getText().toLowerCase(Locale.ROOT).trim();
		MinecraftClient mc = MinecraftClient.getInstance();
		filtered = all.stream()
				.filter(e -> q.isEmpty() || e.value().getResult(mc.world.getRegistryManager())
						.getName().getString().toLowerCase(Locale.ROOT).contains(q))
				.toList();
		scroll = 0;
		selected = -1;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		// Skip blur + darkening — game world renders behind panel
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xF0111111);
		super.render(context, mouseX, mouseY, delta);

		int gridTop = panelY + 26;
		int hoveredIdx = -1;
		for (int row = 0; row < VISIBLE_ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				int idx = (row + scroll) * COLS + col;
				if (idx >= filtered.size()) {
					break;
				}
				int x = panelX + 6 + col * CELL;
				int y = gridTop + row * CELL;
				boolean hover = isOverCell(mouseX, mouseY, x, y);
				if (hover && hoveredIdx < 0) {
					hoveredIdx = idx;
				}
				if (idx == selected || hover) {
					context.fill(x - 1, y - 1, x + 17, y + 17, idx == selected ? 0x60FFFFFF : 0x30FFFFFF);
				}
				ItemStack result = filtered.get(idx).value().getResult(client.world.getRegistryManager());
				context.drawItem(result, x, y);
				context.drawItemInSlot(textRenderer, result, x, y);
			}
		}
		if (filtered.isEmpty()) {
			context.drawText(textRenderer, Text.translatable("text.qoltoolbox.recipe_no_match"),
					panelX + 6, gridTop + 8, 0xFFAAAAAA, false);
		}

		if (selected >= 0 && selected < filtered.size()) {
			renderDetail(context, filtered.get(selected));
		} else {
			context.drawText(textRenderer, Text.translatable("text.qoltoolbox.recipe_select_hint"),
					panelX + COLS * CELL + 16, panelY + 40, 0xFFAAAAAA, true);
		}

		if (hoveredIdx >= 0) {
			context.drawTooltip(textRenderer,
					Screen.getTooltipFromItem(client,
							filtered.get(hoveredIdx).value().getResult(client.world.getRegistryManager())),
					mouseX, mouseY);
		}
	}

	private void renderDetail(DrawContext context, RecipeEntry<CraftingRecipe> entry) {
		CraftingRecipe recipe = entry.value();
		Ingredient[] grid = toGrid(recipe);
		ItemStack result = recipe.getResult(client.world.getRegistryManager());

		int dx = panelX + COLS * CELL + 16;
		int dy = panelY + 26;
		for (int i = 0; i < 9; i++) {
			int x = dx + (i % 3) * CELL;
			int y = dy + (i / 3) * CELL;
			context.fill(x, y, x + 16, y + 16, 0xFF333333);
			if (grid[i] != null) {
				ItemStack[] stacks = grid[i].getMatchingStacks();
				if (stacks.length > 0) {
					context.drawItem(stacks[0], x, y);
					context.drawItemInSlot(textRenderer, stacks[0], x, y);
				}
			}
		}
		int ax = dx + 3 * CELL + 6;
		int ay = dy + 2 * CELL - 4;
		context.drawText(textRenderer, "->", ax, ay, 0xFFFFFF55, false);
		context.fill(ax + 18, dy + CELL, ax + 34, dy + CELL + 16, 0xFF333333);
		context.drawItem(result, ax + 18, dy + CELL);
		context.drawItemInSlot(textRenderer, result, ax + 18, dy + CELL);
		String name = textRenderer.trimToWidth(result.getName().getString(), 100);
		context.drawText(textRenderer, name, dx, dy + 3 * CELL + 6, 0xFFFFFFFF, true);
	}

	private Ingredient[] toGrid(CraftingRecipe recipe) {
		Ingredient[] grid = new Ingredient[9];
		List<Ingredient> ingredients = recipe.getIngredients();
		if (recipe instanceof ShapedRecipe shaped) {
			int w = shaped.getWidth();
			for (int r = 0; r < shaped.getHeight(); r++) {
				for (int c = 0; c < w; c++) {
					grid[r * 3 + c] = ingredients.get(r * w + c);
				}
			}
		} else {
			for (int i = 0; i < Math.min(9, ingredients.size()); i++) {
				grid[i] = ingredients.get(i);
			}
		}
		return grid;
	}

	private boolean isOverCell(double mx, double my, int x, int y) {
		return mx >= x && mx < x + 16 && my >= y && my < y + 16;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (int row = 0; row < VISIBLE_ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				int idx = (row + scroll) * COLS + col;
				if (idx >= filtered.size()) {
					break;
				}
				if (isOverCell(mouseX, mouseY, panelX + 6 + col * CELL, panelY + 26 + row * CELL)) {
					selected = idx;
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int maxScroll = Math.max(0, (filtered.size() + COLS - 1) / COLS - VISIBLE_ROWS);
		scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(verticalAmount)));
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
