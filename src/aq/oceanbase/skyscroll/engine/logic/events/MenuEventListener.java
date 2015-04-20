package aq.oceanbase.skyscroll.engine.logic.events;

import java.util.EventListener;

public interface MenuEventListener extends EventListener {
    public void onReturnToPreviousPage(MenuEvent e);

    public void onOpenNextPage(MenuEvent e);

    public void onStartGame(MenuEvent e);

    public void onExit(MenuEvent e);
}
