package aq.oceanbase.skyscroll.logic.menu;

import aq.oceanbase.skyscroll.graphics.render.RenderContainer;
import aq.oceanbase.skyscroll.logic.enums.MENUPAGE;

public class MenuController {
    private MENUPAGE mCurrentMenu;

    private RenderContainer mCurrentMenuRenderable;

    public RenderContainer getCurrentMenuPage() {
        return mCurrentMenuRenderable;
    }
}
