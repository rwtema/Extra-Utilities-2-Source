package com.rwtema.extrautils2.gui.backend;

import net.minecraft.util.text.translation.I18n;

public class WidgetTextTranslate extends WidgetText {
    public WidgetTextTranslate(int x, int y, int w, int h, int align, int color, String msg) {
        super(x, y, w, h, align, color, msg);
    }

    public WidgetTextTranslate(int i, int j, String invName, int playerInvWidth) {
        super(i, j, invName, playerInvWidth);
    }

    @Override
    public String getMsgClient() {
        return I18n.translateToLocal(msg);
    }
}
