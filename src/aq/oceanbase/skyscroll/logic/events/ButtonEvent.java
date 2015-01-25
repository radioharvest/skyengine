package aq.oceanbase.skyscroll.logic.events;

import java.util.EventObject;

public class ButtonEvent extends EventObject {

    private int mId = -1;
    private boolean mIsPressed = false;

    public ButtonEvent (Object source, int id, boolean pressed) {
        super(source);

        this.mId = id;
        this.mIsPressed = pressed;
    }

    public int getButtonId() {
        return this.mId;
    }

    public boolean isButtonPressed() {
        return mIsPressed;
    }
}