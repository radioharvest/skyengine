package aq.oceanbase.skyscroll.logic.events;

import android.util.EventLog;

import java.util.EventListener;

public interface MenuEventListener extends EventListener {
    public void onReturnToPreviousPage(MenuEvent e);

    public void onOpenNextPage(MenuEvent e);

    public void onStartGame(MenuEvent e);

    public void onExit(MenuEvent e);
}
