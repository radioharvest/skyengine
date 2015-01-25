package aq.oceanbase.skyscroll.logic.menu;

import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.elements.window.Window;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.Button;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.ButtonBlock;

import java.util.List;

public class MenuPageBase extends Window {

    private MenuController mMenuController;

    protected List<Button> mButtons;
    protected ButtonBlock mButtonBlock;


    public MenuPageBase(MenuController controller, int screenX, int screenY, int width, int height, Camera cam, int[] screenMetrics) {
        super(screenX, screenY, 2.0f, width, height, cam, screenMetrics);

        layoutSetup();
    }


    public MenuController getController() {
        return this.mMenuController;
    }


    protected void layoutSetup() {

    }

    private void addButton(Button button) {
        mButtons.add(button);
    }
}
