package aq.oceanbase.skyscroll.logic.events;

import java.util.EventListener;

public interface ButtonEventListener extends EventListener {
    public void onButtonPressed(ButtonEvent e);
}
