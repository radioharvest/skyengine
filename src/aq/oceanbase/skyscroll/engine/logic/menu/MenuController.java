package aq.oceanbase.skyscroll.engine.logic.menu;

import android.util.Log;
import aq.oceanbase.skyscroll.engine.graphics.RenderContainer;
import aq.oceanbase.skyscroll.engine.graphics.RenderableObject;
import aq.oceanbase.skyscroll.game.Game;
import aq.oceanbase.skyscroll.game.enums.MENUPAGE;
import aq.oceanbase.skyscroll.engine.logic.events.MenuEvent;
import aq.oceanbase.skyscroll.engine.logic.events.MenuEventListener;
import aq.oceanbase.skyscroll.game.menu.MainMenuPage;

import java.util.ArrayList;
import java.util.List;

public class MenuController implements MenuEventListener {
    private MENUPAGE mCurrentMenuPage;

    private Game mGameInstance;

    private MenuPageBase mCurrentPage;

    private List<RenderableObject> mMenuPages = new ArrayList<RenderableObject>();
    private RenderContainer mCurrentMenuRenderContext;

    public MenuController(Game gameInstance) {
        this.mGameInstance = gameInstance;

        mCurrentMenuRenderContext = new RenderContainer();

        generatePages();
    }

    public void generatePages() {
        int[] metrics = mGameInstance.getScreenMetrics();
        Log.e("Debug", "MenuCreationInput: " + metrics[2] + " " + metrics[3]);

        int x = (int)(metrics[2] * 0.3f);
        int y = (int)(metrics[3] * 0.4f);

        int width = (int)(metrics[2] * 0.4f);
        int height = (int)(metrics[3] * 0.2f);

        Log.e("Debug", "MenuCreation: x: " + x + " y: " + y + " width: " + width + " height: " + height);

        //MainMenuPage mainMenuPage = new MainMenuPage(this, x, y, width, height, mGameInstance.getCamera(), mGameInstance.getScreenMetrics());
        //mMenuPages.add(mainMenuPage);
        mMenuPages.add(new MainMenuPage(this, x, y, width, height, mGameInstance.getCamera(), mGameInstance.getScreenMetrics()));

        mCurrentMenuRenderContext
                .addRenderable(mMenuPages.get(0));
        mCurrentPage = (MenuPageBase)mMenuPages.get(0);

        mCurrentPage.addMenuEventListener(this);
    }

    public RenderContainer getCurrentMenuRenderContainer() {
        return mCurrentMenuRenderContext;
    }

    public MenuPageBase getCurrentPage() {
        return this.mCurrentPage;
    }


    @Override
    public void onReturnToPreviousPage(MenuEvent e) {

    }

    @Override
    public void onOpenNextPage(MenuEvent e) {

    }

    @Override
    public void onStartGame(MenuEvent e) {
        mGameInstance.closeMenu();
    }

    @Override
    public void onExit(MenuEvent e) {

    }
}
