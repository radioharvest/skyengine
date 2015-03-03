package aq.oceanbase.skyscroll.logic.menu;

import android.util.Log;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.elements.window.Window;
import aq.oceanbase.skyscroll.graphics.elements.window.WindowLayout;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.Button;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.ButtonBlock;
import aq.oceanbase.skyscroll.logic.enums.MENUPAGE;
import aq.oceanbase.skyscroll.logic.events.ButtonEventListener;
import aq.oceanbase.skyscroll.logic.events.MenuEvent;
import aq.oceanbase.skyscroll.logic.events.MenuEventListener;
import aq.oceanbase.skyscroll.logic.events.WindowEvent;

import java.util.ArrayList;
import java.util.List;

public class MenuPageBase extends Window {

    private MenuController mMenuController;

    private MENUPAGE mPageType = MENUPAGE.INVALID;
    private MENUPAGE mPreviousPage = MENUPAGE.INVALID;

    protected List<Button> mButtons;
    protected ButtonBlock mButtonBlock;

    private List<Object> mEventListeners = new ArrayList<Object>();

    public MenuPageBase(MenuController controller, int screenX, int screenY, int width, int height, Camera cam, int[] screenMetrics) {
        super(screenX, screenY, 2.0f, width, height, cam, screenMetrics);

        layoutSetup();
    }


    public void setPreviousPage(MENUPAGE page) {
        this.mPreviousPage = page;
    }

    public MENUPAGE getPageType() {
        return mPageType;
    }

    public MENUPAGE getPreviousPage() {
        return mPreviousPage;
    }

    public MenuController getController() {
        return this.mMenuController;
    }


    protected void layoutSetup() {

    }

    public void addButton(Button button) {
        mButtons.add(button);
    }


    public void addMenuEventListener(Object obj) {
        mEventListeners.add(obj);
    }

    public void removeMenuEventListener(Object obj) {
        mEventListeners.remove(obj);
    }


    protected void fireReturnEvent() {
        MenuEvent event = new MenuEvent(this, mPageType);
        event.setReturnToPreviousPage(true);
        event.setNextPageType(mPreviousPage);

        if (!mEventListeners.isEmpty()) {
            for (int i = 0; i < mEventListeners.size(); i++) {
                MenuEventListener listener = (MenuEventListener)mEventListeners.get(i);
                listener.onReturnToPreviousPage(event);
            }
        }
    }

    protected void fireOpenNextPageEvent(MENUPAGE type) {
        MenuEvent event = new MenuEvent(this, mPageType);
        event.setNextPageType(type);

        if (!mEventListeners.isEmpty()) {
            for (int i = 0; i < mEventListeners.size(); i++) {
                MenuEventListener listener = (MenuEventListener)mEventListeners.get(i);
                listener.onOpenNextPage(event);
            }
        }
    }

    protected void fireStartGameEvent() {
        MenuEvent event = new MenuEvent(this, mPageType);
        event.setStartGame(true);

        if (!mEventListeners.isEmpty()) {
            for (int i = 0; i < mEventListeners.size(); i++) {
                MenuEventListener listener = (MenuEventListener)mEventListeners.get(i);
                listener.onStartGame(event);
            }
        }
    }

    protected void fireExitEvent() {
        MenuEvent event = new MenuEvent(this, mPageType);
        event.setExit(true);

        if (!mEventListeners.isEmpty()) {
            for (int i = 0; i < mEventListeners.size(); i++) {
                MenuEventListener listener = (MenuEventListener)mEventListeners.get(i);
                listener.onExit(event);
            }
        }
    }
}
