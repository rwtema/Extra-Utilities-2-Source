package com.rwtema.extrautils2.gui.backend;


import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.model.Textures;
import com.rwtema.extrautils2.compatibility.ClientHelper112;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.GuiContainerCompat;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.utils.client.GLStateAttributes;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class DynamicGui extends GuiContainerCompat {
	public static final ResourceLocation texWidgets = new ResourceLocation(ExtraUtils2.RESOURCE_FOLDER, "textures/gui_widget.png");
	public final DynamicContainer container;
	public int mouseX;
	public int mouseY;
	TIntObjectHashMap<WidgetButton> buttonWidgets = new TIntObjectHashMap<>();

	public DynamicGui(DynamicContainer container) {
		super(container);
		container.isClient = true;
		this.container = container;
		container.loadGuiDimensions(this);

	}

	public int nextButtonID() {
		return buttonList.size();
	}

	@Override
	public void initGui() {
		super.initGui();

		int y = 0;
		for (DynamicWindow dynamicWindow : container.getWindowWidgets().keySet()) {
			if (dynamicWindow != null) {
				Set<IWidget> iWidgets = container.getWindowWidgets().get(dynamicWindow);

				dynamicWindow.crop(iWidgets);
				if (dynamicWindow.side == DynamicWindow.WindowSide.LEFT) {
					dynamicWindow.x = -dynamicWindow.w;
				} else
					dynamicWindow.x = container.width;
				dynamicWindow.y = y;

				for (IWidget widget : iWidgets) {
					if (widget instanceof Slot) {
						Slot slot = (Slot) widget;
						slot.xPos = widget.getX() + dynamicWindow.x + 1;
						slot.yPos = widget.getY() + dynamicWindow.y + 1;
					}
				}
			}
		}

		container.loadGuiDimensions(this);

		for (IWidget widget : container.getWidgets()) {
			widget.addToGui(this);
		}
	}

	public void addButton(WidgetButton widgetButton, GuiButton button) {
		buttonWidgets.put(button.id, widgetButton);
		buttonList.add(button);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		for (IWidgetClientTick clientTick : container.getWidgetClientTick()) {
			clientTick.updateClient();
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);

		if (button.enabled) {
			WidgetButton widgetButton = buttonWidgets.get(button.id);
			if (widgetButton != null)
				widgetButton.onClientClick();
		}
	}

	public float getZLevel() {
		return this.zLevel;
	}

	public int getXSize() {
		return xSize;
	}

	public int getYSize() {
		return ySize;
	}

	public FontRenderer getFontRenderer() {
		return super.fontRenderer;
	}

	public ResourceLocation getWidgetTexture() {
		return texWidgets;
	}

	public void drawScaledBox(ResourceLocation location, int x, int y, int w, int h) {
		this.mc.renderEngine.bindTexture(location);
		int dx = w / 2;
		int dy = h / 2;
		drawTexturedModalRect(guiLeft + x, guiTop + y, 0, 0, dx, dy);
		drawTexturedModalRect(guiLeft + x + dx, guiTop + y, 256 - dx, 0, dx, dy);
		drawTexturedModalRect(guiLeft + x, guiTop + y + dy, 0, 256 - dy, dx, dy);
		drawTexturedModalRect(guiLeft + x + dx, guiTop + y + dy, 256 - dx, 256 - dy, dx, dy);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int a, int b) {
		GlStateManager.color(1, 1, 1);

		for (DynamicWindow dynamicWindow : container.getWindowWidgets().keySet()) {
			if (dynamicWindow == null) {
				if (!container.drawBackgroundOverride(this)) {
					this.mc.renderEngine.bindTexture(DynamicContainer.texBackground);
					drawBasicBackground(this.guiLeft, this.guiTop, this.xSize, this.ySize);
				}

				this.mc.renderEngine.bindTexture(getWidgetTexture());
				GlStateManager.color(1, 1, 1);
				GLStateAttributes glStateAttributes = GLStateAttributes.loadStates();
				for (IWidget widget : container.getWindowWidgets().get(null)) {
					widget.renderBackground(this.mc.renderEngine, this, this.guiLeft, this.guiTop);
					glStateAttributes.restore();
				}
			} else {
				if (!dynamicWindow.drawBackgroundOverride(this, container)) {
					this.mc.renderEngine.bindTexture(DynamicContainer.texBackground);
					GlStateManager.color(dynamicWindow.r, dynamicWindow.g, dynamicWindow.b);
					drawBasicBackground(guiLeft + dynamicWindow.x, guiTop + dynamicWindow.y, dynamicWindow.w, dynamicWindow.h);
				}

				this.mc.renderEngine.bindTexture(getWidgetTexture());
				GlStateManager.color(1, 1, 1);
				GLStateAttributes glStateAttributes = GLStateAttributes.loadStates();
				for (IWidget widget : container.getWindowWidgets().get(dynamicWindow)) {
					widget.renderBackground(this.mc.renderEngine, this, guiLeft + dynamicWindow.x, guiTop + dynamicWindow.y);
					glStateAttributes.restore();
				}
			}


		}


	}

	public void drawBasicBackground(int x, int y, int w, int h) {
		int w2 = w >> 1;
		int h2 = h >> 1;
		int w3 = w - w2;
		int h3 = h - h2;
		drawTexturedModalRect(x, y, 0, 0, w2, h2);
		drawTexturedModalRect(x + w2, y, 256 - w3, 0, w3, h2);
		drawTexturedModalRect(x, y + h2, 0, 256 - h3, w2, h3);
		drawTexturedModalRect(x + w2, y + h2, 256 - w3, 256 - h3, w3, h3);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		List<String> tooltip = null;
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GLStateAttributes glStateAttributes = GLStateAttributes.loadStates();

		for (DynamicWindow dynamicWindow : container.getWindowWidgets().keySet()) {
			if (dynamicWindow == null) {
				tooltip = renderForeground(mouseX, mouseY, tooltip, null, guiLeft, guiTop);
			} else {
				tooltip = renderForeground(mouseX, mouseY, tooltip, dynamicWindow, guiLeft + dynamicWindow.x, guiTop + dynamicWindow.y);
			}
			glStateAttributes.restore();
		}

		if (tooltip != null) {
			FontRenderer fontRenderer = getFontRenderer();
			ArrayList<String> strings = new ArrayList<>();
			int w = 1000;
			for (String s : tooltip) {
				strings.addAll(fontRenderer.listFormattedStringToWidth(s, w));
			}

			GlStateManager.pushMatrix();
			GlStateManager.translate(- guiLeft, -guiTop, 0);
			drawHoveringText(strings, mouseX, mouseY, fontRenderer);
			GlStateManager.popMatrix();
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
		}

		RenderHelper.enableGUIStandardItemLighting();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableRescaleNormal();
	}

	private List<String> renderForeground(int mouseX, int mouseY, List<String> tooltip, DynamicWindow dynamicWindow, int guiX, int guiY) {
		for (IWidget widget : container.getWindowWidgets().get(dynamicWindow)) {
			this.mc.renderEngine.bindTexture(getWidgetTexture());
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			widget.renderForeground(this.mc.renderEngine, this, guiX - guiLeft, guiY - guiTop);

			if (isInArea(mouseX, mouseY, widget)) {
				List<String> t = widget.getToolTip();

				if (t != null) {
					if (tooltip == null) tooltip = new ArrayList<>();
					tooltip.addAll(t);
				}
			}
		}
		return tooltip;
	}

	public boolean isInArea(int x, int y, IWidget w) {
		DynamicWindow dynamicWindow = container.getWindowOwner().get(w);
		if (dynamicWindow == null) {
			x -= guiLeft;
			y -= guiTop;
		} else {
			x -= dynamicWindow.x + guiLeft;
			y -= dynamicWindow.y + guiTop;
		}

		return x >= w.getX() && x < (w.getX() + w.getW()) && y >= w.getY() && y < (w.getY() + w.getH());
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		for (IWidgetKeyInput input : container.getWidgetKeyInputs()) {
			if (input.keyTyped(typedChar, keyCode)) {
				return;
			}
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() throws IOException {
		mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		for (IWidgetMouseInput widgetMouseInput : container.getWidgetMouseInputs()) {
			widgetMouseInput.mouseTick(x, y, isInArea(x, y, widgetMouseInput));
		}

		super.handleMouseInput();

		int i = Mouse.getEventDWheel();

		for (IWidgetMouseInput widgetMouseInput : container.getWidgetMouseInputs()) {
			widgetMouseInput.mouseWheelScroll(i, isInArea(x, y, widgetMouseInput));
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		super.mouseReleased(mouseX, mouseY, mouseButton);
		for (IWidgetMouseInput mouseInput : container.getWidgetMouseInputs()) {
			mouseInput.mouseReleased(mouseX, mouseY, mouseButton, isInArea(mouseX, mouseY, mouseInput));
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		for (IWidgetMouseInput mouseInput : container.getWidgetMouseInputs()) {
			mouseInput.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick, isInArea(mouseX, mouseY, mouseInput));
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for (IWidgetMouseInput mouseInput : container.getWidgetMouseInputs()) {
			mouseInput.mouseClicked(mouseX, mouseY, mouseButton, isInArea(mouseX, mouseY, mouseInput));
		}
	}


	@Override
	public void drawHorizontalLine(int startX, int endX, int y, int color) {
		super.drawHorizontalLine(startX, endX, y, color);
	}

	@Override
	public void drawVerticalLine(int x, int startY, int endY, int color) {
		super.drawVerticalLine(x, startY, endY, color);
	}

	@Override
	public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
		super.drawGradientRect(left, top, right, bottom, startColor, endColor);
	}


	@Override
	public void drawCenteredString(FontRenderer fontRendererIn, @Nonnull String text, int x, int y, int color) {
		super.drawCenteredString(fontRendererIn, text, x, y, color);
	}

	@Override
	public void drawString(FontRenderer fontRendererIn, @Nonnull String text, int x, int y, int color) {
		super.drawString(fontRendererIn, text, x, y, color);
	}

	@Override
	public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
		super.drawTexturedModalRect(x, y, textureX, textureY, width, height);
	}

	@Override
	public void drawTexturedModalRect(float xCoord, float yCoord, int minU, int minV, int maxU, int maxV) {
		super.drawTexturedModalRect(xCoord, yCoord, minU, minV, maxU, maxV);
	}

	@Override
	public void drawTexturedModalRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn) {
		super.drawTexturedModalRect(xCoord, yCoord, textureSprite, widthIn, heightIn);
	}


	public void drawTexturedModalRect(double xCoord, double yCoord, float w, float h, double minU, double minV, double maxU, double maxV) {
		float f = 1;
		float f1 = 1;
		Tessellator tessellator = Tessellator.getInstance();
		IVertexBuffer vertexbuffer = CompatClientHelper.wrap(tessellator.getBuffer());
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		vertexbuffer.pos(xCoord + 0, yCoord + h, this.zLevel).tex(minU * f, maxV * f1).endVertex();
		vertexbuffer.pos(xCoord + w, yCoord + h, this.zLevel).tex(maxU * f, maxV * f1).endVertex();
		vertexbuffer.pos(xCoord + w, yCoord + 0, this.zLevel).tex(maxU * f, minV * f1).endVertex();
		vertexbuffer.pos(xCoord + 0, yCoord + 0, this.zLevel).tex(minU * f, minV * f1).endVertex();
		tessellator.draw();
	}

	public void renderStack(ItemStack stack, int x, int y, String altText) {
		if (StackHelper.isNull(stack)) return;
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 32.0F);
		this.zLevel = 200.0F;
		this.itemRender.zLevel = 200.0F;
		net.minecraft.client.gui.FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null) font = fontRenderer;
		this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y, altText);
		this.zLevel = 0.0F;
		this.itemRender.zLevel = 0.0F;
		GlStateManager.popMatrix();


	}

	public void drawRectangle(int x, int y, int w, int h, int color) {
		drawRect(x, y, x + w, y + h, color);
	}

	public void drawSlotBackground(int x, int y) {
		drawTexturedModalRect(x, y, 0, 0, 18, 18);
	}

	public void renderSmallStackText(ItemStack stack, String s, int x, int y) {
		if (StackHelper.isNull(stack)) return;
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 32.0F);
		this.zLevel = 200.0F;
		this.itemRender.zLevel = 200.0F;
		net.minecraft.client.gui.FontRenderer font;
		font = stack.getItem().getFontRenderer(stack);
		if (font == null) font = fontRenderer;
		this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y, "");

		if (!StringUtils.isNullOrEmpty(s)) {
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			GlStateManager.disableBlend();
			float scale = 2;
			float scaledFontWidth;
			while ((scaledFontWidth = fontRenderer.getStringWidth(s) / scale) >= 15) scale++;
			GlStateManager.translate(x + 16 - scaledFontWidth, y + 17 - 9 / scale, 0);
			GlStateManager.scale(1 / scale, 1 / scale, 1 / scale);
			fontRenderer.drawStringWithShadow(s, 0, 0, 16777215);
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
		}

		this.zLevel = 0.0F;
		this.itemRender.zLevel = 0.0F;
		GlStateManager.popMatrix();


	}

	public void renderFluidTiled(FluidStack fluidStack, TextureManager manager, int x, int y, int w, int h) {
		if (fluidStack == null) {
			return;
		}
		Fluid fluid = fluidStack.getFluid();

		if (w == 0 || h == 0) {
			return;
		}
		ResourceLocation fluidStill = fluid.getStill(fluidStack);
		if (fluidStill == null) return;

		TextureAtlasSprite sprite = null;
		sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(fluidStill.toString());
		if (sprite == null)
			sprite = Textures.MISSING_SPRITE;

		manager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		int color = fluid.getColor(fluidStack);
		float red = (color >> 16 & 255) / 255.0F;
		float green = (color >> 8 & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		GlStateManager.color(red, green, blue);
		for (int dx1 = x; dx1 < x + w; dx1 += 16) {
			for (int dy1 = y; dy1 < y + h; dy1 += 16) {
				int dx2 = Math.min(dx1 + 16, x + w);
				int dy2 = Math.min(dy1 + 16, y + h);

				int w1 = dx2 - dx1;
				int h1 = dy2 - dy1;
				drawTexturedModalRect(dx1, dy1, w1, h1,
						sprite.getMinU(),
						sprite.getMinV(),
						sprite.getInterpolatedU(w1),
						sprite.getInterpolatedV(h1));
			}
		}

	}

	public void setWidthHeight(int width, int height) {
		this.xSize = width;
		this.ySize = height;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
	}
}
