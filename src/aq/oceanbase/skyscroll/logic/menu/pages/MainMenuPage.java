package aq.oceanbase.skyscroll.logic.menu.pages;

import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.elements.window.WindowLayout;
import aq.oceanbase.skyscroll.logic.menu.MenuPageBase;

public class MainMenuPage extends MenuPageBase {

    public MainMenuPage(int screenX, int screenY, int width, int height, Camera cam, int[] screenMetrics) {
        super(screenX, screenY, width, height, cam, screenMetrics);
    }

    @Override
    protected void layoutSetup() {
        this.mLayout.setLayoutType(WindowLayout.LAYOUT.HORIZONTAL);
    }
}
