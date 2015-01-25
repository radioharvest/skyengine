package aq.oceanbase.skyscroll.logic.menu.pages;

import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.elements.window.WindowBlock;
import aq.oceanbase.skyscroll.graphics.elements.window.WindowLayout;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.Button;
import aq.oceanbase.skyscroll.logic.events.ButtonEvent;
import aq.oceanbase.skyscroll.logic.menu.MenuController;
import aq.oceanbase.skyscroll.logic.menu.MenuPageBase;

public class MainMenuPage extends MenuPageBase {

    public MainMenuPage(MenuController controller, int screenX, int screenY, int width, int height, Camera cam, int[] screenMetrics) {
        super(controller, screenX, screenY, width, height, cam, screenMetrics);
    }

    @Override
    protected void layoutSetup() {
        this.mLayout.setLayoutType(WindowLayout.LAYOUT.HORIZONTAL);
        WindowLayout middle = new WindowLayout(WindowLayout.LAYOUT.VERTICAL, this, 0.4f);

        Button newGameButton = new Button("New game");
        newGameButton.addButtonEventListener(this);

        middle.addChild(new WindowBlock(this, 0.3f));


        mLayout.addChild(new WindowBlock(this, 0.3f));
    }

    @Override
    public void onButtonPressed(ButtonEvent e) {}
}
