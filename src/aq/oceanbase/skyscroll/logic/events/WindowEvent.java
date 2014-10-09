package aq.oceanbase.skyscroll.logic.events;

import java.util.EventObject;

public class WindowEvent extends EventObject {
    public static enum ANSWER {
        CORRECT, WRONG
    }

    private boolean mClose;


    public WindowEvent(Object source) {
        super(source);
        mClose = true;
    }

    public boolean isClosing() {
        return mClose;
    }
}
