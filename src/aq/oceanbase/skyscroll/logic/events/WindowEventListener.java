package aq.oceanbase.skyscroll.logic.events;

import java.util.EventListener;

public interface WindowEventListener extends EventListener {
    public void onClose(WindowEvent e);

    public void onAnswer(WindowEvent e);
}
