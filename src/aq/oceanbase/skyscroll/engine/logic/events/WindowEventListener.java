package aq.oceanbase.skyscroll.engine.logic.events;

import java.util.EventListener;

public interface WindowEventListener extends EventListener {
    public void onClose(WindowEvent e);
}
