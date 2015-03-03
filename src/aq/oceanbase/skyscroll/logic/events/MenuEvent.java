package aq.oceanbase.skyscroll.logic.events;

import aq.oceanbase.skyscroll.logic.enums.MENUPAGE;

import java.util.EventObject;

public class MenuEvent extends EventObject {

    private MENUPAGE mSourcePageType = MENUPAGE.INVALID;
    private MENUPAGE mNextPageType = MENUPAGE.INVALID;

    private boolean mStartGame = false;
    private boolean mReturnToPreviousPage = false;
    private boolean mExit = false;

    public MenuEvent(Object source, MENUPAGE sourceType) {
        super(source);

        this.mSourcePageType = sourceType;
    }

    public void setStartGame(boolean value) {
        mStartGame = value;
    }

    public void setReturnToPreviousPage(boolean value) {
        this.mReturnToPreviousPage = value;
    }

    public void setExit(boolean value) {
        this.mExit = value;
    }

    public void setNextPageType(MENUPAGE type) {
        this.mNextPageType = type;
    }

    public MENUPAGE getSourcePageType() {
        return mSourcePageType;
    }

    public MENUPAGE getNextPage() {
        return mNextPageType;
    }

    public boolean isGameStarting() {
        return mStartGame;
    }

    public boolean isReturningToPreviousPage() {
        return mReturnToPreviousPage;
    }

    public boolean isExiting() {
        return mExit;
    }
}
