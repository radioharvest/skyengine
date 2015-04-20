package aq.oceanbase.skyscroll.game.menu;

import android.util.Log;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.logic.window.blocks.Button;
import aq.oceanbase.skyscroll.engine.logic.window.blocks.ButtonBlock;
import aq.oceanbase.skyscroll.engine.logic.events.ButtonEvent;
import aq.oceanbase.skyscroll.engine.logic.menu.MenuController;
import aq.oceanbase.skyscroll.engine.logic.menu.MenuPageBase;

public class MainMenuPage extends MenuPageBase {

    public MainMenuPage(MenuController controller, int screenX, int screenY, int width, int height, Camera cam, int[] screenMetrics) {
        super(controller, screenX, screenY, width, height, cam, screenMetrics);
        Log.e("Debug", "ScreenMetricsMenu: " + screenMetrics[2] + " " + screenMetrics[3]);
    }

    @Override
    protected void layoutSetup() {
        mButtonBlock = new ButtonBlock(this, 1, this.getBorderOffset(), 0.0f, ButtonBlock.BUTTONLAYOUT.VERTICAL);

        Button startButton = new Button("Start game");
        startButton.addButtonEventListener(this);
        mButtonBlock.addButton(startButton);

        //this.mLayout.setLayoutType(WindowLayout.LAYOUT.HORIZONTAL);

        this.mLayout.addChild(mButtonBlock);
    }

    @Override
    public void onButtonPressed(ButtonEvent e) {
        switch(e.getButtonId()) {
            case 0:
                fireStartGameEvent();
                break;
            default:
                break;
        }
    }
}
