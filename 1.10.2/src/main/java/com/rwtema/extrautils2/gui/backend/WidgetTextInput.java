package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class WidgetTextInput extends WidgetBase implements IWidgetKeyInput, IWidgetMouseInput, IWidgetClientTick {
	private final static int PADDING = 1;
	@Nonnull
	private String text = "";
	private int maxStringLength = 32;
	private int cursorCounter;
	private boolean canLoseFocus = true;
	private boolean isFocused;
	private int lineScrollOffset;
	private int cursorPosition;
	private int selectionEnd;


	public WidgetTextInput(int x, int y, int w, @Nonnull String text) {
		super(x, y, w, 9 + PADDING * 2);
		this.text = text;
	}

	@Override
	public void updateClient() {
		cursorCounter++;
	}

	@Nonnull
	public String getText() {
		return text;
	}

	public void setText(@Nonnull String textIn) {
		if (isValidText(textIn)) {
			if (textIn.length() > maxStringLength) {
				text = textIn.substring(0, maxStringLength);
			} else {
				text = textIn;
			}

			setCursorPositionEnd();
			onValueChanged();
		}
	}

	public String getSelectedText() {
		int i = cursorPosition < selectionEnd ? cursorPosition : selectionEnd;
		int j = cursorPosition < selectionEnd ? selectionEnd : cursorPosition;
		return text.substring(i, j);
	}

	public void writeText(String textToWrite) {
		String s = "";
		String s1 = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
		int i = cursorPosition < selectionEnd ? cursorPosition : selectionEnd;
		int j = cursorPosition < selectionEnd ? selectionEnd : cursorPosition;
		int k = maxStringLength - text.length() - (i - j);

		if (!text.isEmpty()) {
			s = s + text.substring(0, i);
		}

		int l;

		if (k < s1.length()) {
			s = s + s1.substring(0, k);
			l = k;
		} else {
			s = s + s1;
			l = s1.length();
		}

		if (!text.isEmpty() && j < text.length()) {
			s = s + text.substring(j);
		}

		if (isValidText(s)) {
			text = s;
			moveCursorBy(i - selectionEnd + l);
			onValueChanged();
		}
	}

	protected void onValueChanged() {

	}

	public void deleteWords(int num) {
		if (!text.isEmpty()) {
			if (selectionEnd != cursorPosition) {
				writeText("");
			} else {
				deleteFromCursor(getNthWordFromCursor(num) - cursorPosition);
			}
		}
	}

	public void deleteFromCursor(int num) {
		if (!text.isEmpty()) {
			if (selectionEnd != cursorPosition) {
				writeText("");
			} else {
				boolean flag = num < 0;
				int i = flag ? cursorPosition + num : cursorPosition;
				int j = flag ? cursorPosition : cursorPosition + num;
				String s = "";

				if (i >= 0) {
					s = text.substring(0, i);
				}

				if (j < text.length()) {
					s = s + text.substring(j);
				}

				if (isValidText(s)) {
					text = s;

					if (flag) {
						moveCursorBy(num);
					}

					onValueChanged();
				}
			}
		}
	}

	protected boolean isValidText(@Nonnull String s) {
		return true;
	}

	public int getNthWordFromCursor(int numWords) {
		return getNthWordFromPos(numWords, getCursorPosition());
	}


	public int getNthWordFromPos(int n, int pos) {
		return getNthWordFromPosWS(n, pos, true);
	}

	public int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
		int i = pos;
		boolean flag = n < 0;
		int j = Math.abs(n);

		for (int k = 0; k < j; ++k) {
			if (!flag) {
				int l = text.length();
				i = text.indexOf(32, i);

				if (i == -1) {
					i = l;
				} else {
					while (skipWs && i < l && text.charAt(i) == 32) {
						++i;
					}
				}
			} else {
				while (skipWs && i > 0 && text.charAt(i - 1) == 32) {
					--i;
				}

				while (i > 0 && text.charAt(i - 1) != 32) {
					--i;
				}
			}
		}

		return i;
	}

	public void moveCursorBy(int num) {
		setCursorPosition(selectionEnd + num);
	}

	public void setCursorPositionZero() {
		setCursorPosition(0);
	}

	public void setCursorPositionEnd() {
		setCursorPosition(text.length());
	}

	@SideOnly(Side.CLIENT)
	private void drawCursorVertical(int startX, int startY, int endX, int endY) {
		if (startX < endX) {
			int i = startX;
			startX = endX;
			endX = i;
		}

		if (startY < endY) {
			int j = startY;
			startY = endY;
			endY = j;
		}

		if (endX > x + w) {
			endX = x + w;
		}

		if (startX > x + w) {
			startX = x + w;
		}

		Tessellator tessellator = Tessellator.getInstance();
		IVertexBuffer vertexbuffer = CompatClientHelper.wrap(tessellator.getBuffer());
		GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.enableColorLogic();
		GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
		vertexbuffer.pos((double) startX, (double) endY, 0.0D).endVertex();
		vertexbuffer.pos((double) endX, (double) endY, 0.0D).endVertex();
		vertexbuffer.pos((double) endX, (double) startY, 0.0D).endVertex();
		vertexbuffer.pos((double) startX, (double) startY, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.disableColorLogic();
		GlStateManager.enableTexture2D();
	}

	public int getMaxStringLength() {
		return maxStringLength;
	}

	public void setMaxStringLength(int length) {
		maxStringLength = length;

		if (text.length() > length) {
			text = text.substring(0, length);
			onValueChanged();
		}
	}

	public int getCursorPosition() {
		return cursorPosition;
	}

	public void setCursorPosition(int pos) {
		cursorPosition = pos;
		int i = text.length();
		cursorPosition = MathHelper.clamp(cursorPosition, 0, i);
		setSelectionPos(cursorPosition);
	}

	/**
	 * Getter for the focused field
	 */
	public boolean isFocused() {
		return isFocused;
	}

	/**
	 * Sets focus to this gui element
	 */
	public void setFocused(boolean isFocusedIn) {
		if (isFocusedIn && !isFocused) {
			cursorCounter = 0;
		}

		isFocused = isFocusedIn;
	}


	/**
	 * the side of the selection that is not the cursor, may be the same as the cursor
	 */
	public int getSelectionEnd() {
		return selectionEnd;
	}

	/**
	 * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the
	 * selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
	 */
	public void setSelectionPos(int position) {
		int i = text.length();

		if (position > i) {
			position = i;
		}

		if (position < 0) {
			position = 0;
		}

		selectionEnd = position;

		if (getFontRendererInstance() != null) {
			if (lineScrollOffset > i) {
				lineScrollOffset = i;
			}

			int j = getW();
			String s = getFontRendererInstance().trimStringToWidth(text.substring(lineScrollOffset), j);
			int k = s.length() + lineScrollOffset;

			if (position == lineScrollOffset) {
				lineScrollOffset -= getFontRendererInstance().trimStringToWidth(text, j, true).length();
			}

			if (position > k) {
				lineScrollOffset += position - k;
			} else if (position <= lineScrollOffset) {
				lineScrollOffset -= lineScrollOffset - position;
			}

			lineScrollOffset = MathHelper.clamp(lineScrollOffset, 0, i);
		}
	}

	/**
	 * Sets whether this text box loses focus when something other than it is clicked.
	 */
	public void setCanLoseFocus(boolean canLoseFocusIn) {
		canLoseFocus = canLoseFocusIn;
	}

	@Override
	public boolean keyTyped(char key, int keyCode) {
		if (!isFocused) {
			return false;
		} else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
			setCursorPositionEnd();
			setSelectionPos(0);
			return true;
		} else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText());
			return true;
		} else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			writeText(GuiScreen.getClipboardString());
			return true;
		} else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText());
			writeText("");
			return true;
		} else {
			boolean shiftKeyDown = GuiScreen.isShiftKeyDown();
			boolean ctrlKeyDown = GuiScreen.isCtrlKeyDown();
			switch (keyCode) {
				case Keyboard.KEY_BACK:
					if (ctrlKeyDown) {
						deleteWords(-1);
					} else {
						deleteFromCursor(-1);
					}

					return true;
				case Keyboard.KEY_HOME:
					if (shiftKeyDown) {
						setSelectionPos(0);
					} else {
						setCursorPositionZero();
					}

					return true;
				case Keyboard.KEY_LEFT:

					if (shiftKeyDown) {
						if (ctrlKeyDown) {
							setSelectionPos(getNthWordFromPos(-1, getSelectionEnd()));
						} else {
							setSelectionPos(getSelectionEnd() - 1);
						}
					} else if (ctrlKeyDown) {
						setCursorPosition(getNthWordFromCursor(-1));
					} else {
						moveCursorBy(-1);
					}

					return true;
				case Keyboard.KEY_RIGHT:

					if (shiftKeyDown) {
						if (ctrlKeyDown) {
							setSelectionPos(getNthWordFromPos(1, getSelectionEnd()));
						} else {
							setSelectionPos(getSelectionEnd() + 1);
						}
					} else if (ctrlKeyDown) {
						setCursorPosition(getNthWordFromCursor(1));
					} else {
						moveCursorBy(1);
					}

					return true;
				case Keyboard.KEY_END:

					if (shiftKeyDown) {
						setSelectionPos(text.length());
					} else {
						setCursorPositionEnd();
					}

					return true;
				case Keyboard.KEY_DELETE:

					if (ctrlKeyDown) {
						deleteWords(1);
					} else {
						deleteFromCursor(1);
					}

					return true;
				default:

					if (ChatAllowedCharacters.isAllowedCharacter(key)) {
						writeText(Character.toString(key));
						return true;
					} else {
						return false;
					}
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton, boolean mouseOver) {
		if (canLoseFocus) {
			setFocused(mouseOver);
		}

		if (isFocused && mouseOver) {
			if (mouseButton == 0) {
				int i = mouseX - getX();


				i -= PADDING;


				String s = getFontRendererInstance().trimStringToWidth(text.substring(lineScrollOffset), getW());
				setCursorPosition(getFontRendererInstance().trimStringToWidth(s, i).length() + lineScrollOffset);
			}else if (mouseButton == 1){
				setText("");
			}

		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton, boolean mouseOver) {

	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastMove, boolean mouseOver) {

	}

	@Override
	public void mouseWheelScroll(int delta, boolean mouseOver) {

	}

	@Override
	public void mouseTick(int mouseX, int mouseY, boolean mouseOver) {

	}

	@Override
	public boolean usesMouseWheel() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRendererInstance() {
		return Minecraft.getMinecraft().fontRenderer;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		int w = getW();
		int x1 = guiLeft + getX();
		gui.drawTexturedModalRect(x1, guiTop + getY(), 0, 102, w / 2, 11);
		gui.drawTexturedModalRect(x1 + w / 2, guiTop + getY(), 90 - w/2, 102, w / 2, 11);

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderForeground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
		int i = 14737632;
		int j = cursorPosition - lineScrollOffset;
		int k = selectionEnd - lineScrollOffset;
		String s = getFontRendererInstance().trimStringToWidth(text.substring(lineScrollOffset), getW());
		boolean flag = j >= 0 && j <= s.length();
		boolean flag1 = isFocused && cursorCounter / 6 % 2 == 0 && flag;
		int l = x + PADDING;
		int i1 = y + PADDING;
		int j1 = l;

		if (k > s.length()) {
			k = s.length();
		}

		if (!s.isEmpty()) {
			String s1 = flag ? s.substring(0, j) : s;
			j1 = getFontRendererInstance().drawStringWithShadow(s1, (float) l, (float) i1, i);
		}

		boolean flag2 = cursorPosition < text.length() || text.length() >= getMaxStringLength();
		int k1 = j1;

		if (!flag) {
			k1 = j > 0 ? l + w : l;
		} else if (flag2) {
			k1 = j1 - 1;
			--j1;
		}

		if (!s.isEmpty() && flag && j < s.length()) {
			j1 = getFontRendererInstance().drawStringWithShadow(s.substring(j), (float) j1, (float) i1, i);
		}

		if (flag1) {
			if (flag2) {
				Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + getFontRendererInstance().FONT_HEIGHT, -3092272);
			} else {
				getFontRendererInstance().drawStringWithShadow("_", (float) k1, (float) i1, i);
			}
		}

		if (k != j) {
			int l1 = l + getFontRendererInstance().getStringWidth(s.substring(0, k));
			drawCursorVertical(k1, i1 - 1, l1 - 1, i1 + 1 + getFontRendererInstance().FONT_HEIGHT);
		}

	}
}
