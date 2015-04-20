package aq.oceanbase.skyscroll.game.events;

import java.util.EventListener;

public interface QuestionEventListener extends EventListener {
    public void onAnswer(QuestionEvent e);
}
