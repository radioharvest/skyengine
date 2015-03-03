package aq.oceanbase.skyscroll.logic.menu;

import android.content.Context;
import android.util.Log;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.RenderContainer;
import aq.oceanbase.skyscroll.graphics.render.Renderable;
import aq.oceanbase.skyscroll.logic.Game;
import aq.oceanbase.skyscroll.logic.enums.MENUPAGE;
import aq.oceanbase.skyscroll.logic.events.MenuEvent;
import aq.oceanbase.skyscroll.logic.events.MenuEventListener;
import aq.oceanbase.skyscroll.logic.menu.pages.MainMenuPage;
import aq.oceanbase.skyscroll.touch.TouchHandler;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.utils.math.Vector2f;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MenuController extends TouchHandler implements MenuEventListener {
    private MENUPAGE mCurrentMenuPage;

    private Game mGameInstance;

    private MenuPageBase mCurrentPage;

    private List<Renderable> mMenuPages = new ArrayList<Renderable>();
    private RenderContainer mCurrentMenuRenderables;

    public MenuController(Game gameInstance) {
        this.mGameInstance = gameInstance;

        mCurrentMenuRenderables = new RenderContainer();

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

        mCurrentMenuRenderables
                .addRenderable(mGameInstance.mTreeBackground)
                .addRenderable(mMenuPages.get(0));
        mCurrentPage = (MenuPageBase)mMenuPages.get(0);

        mCurrentPage.addMenuEventListener(this);
    }

    public RenderContainer getCurrentMenuRenderables() {
        return mCurrentMenuRenderables;
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

    @Override
    public void onTap(float x, float y) {
        Vector3f touch = new TouchRay(x, y, 1.0f, mGameInstance.getCamera(), mGameInstance.getScreenMetrics())
                .getPointPositionOnRay(mGameInstance.getCamera().getPosZ() - mCurrentPage.getPosition().z);

        mCurrentPage.onTap(touch.x, touch.y);
    }
}
