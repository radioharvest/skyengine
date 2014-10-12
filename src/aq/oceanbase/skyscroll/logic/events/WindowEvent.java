package aq.oceanbase.skyscroll.logic.events;

import aq.oceanbase.skyscroll.logic.Game;

import java.util.EventObject;

public class WindowEvent extends EventObject {

    private Game.ANSWER mAnswer = Game.ANSWER.NONE;

    public WindowEvent(Object source, Game.ANSWER answerState) {
        super(source);
        mAnswer = answerState;
    }

    public WindowEvent(Object source) {
        super(source);
    }

    public boolean isAnsweredCorrectly() {
        if (mAnswer == Game.ANSWER.CORRECT) return true;
        else return false;
    }

    public Game.ANSWER getAnswerState() {
        return this.mAnswer;
    }
}
