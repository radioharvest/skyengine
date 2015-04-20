package aq.oceanbase.skyscroll.game.events;

import aq.oceanbase.skyscroll.game.Game;

import java.util.EventObject;

public class QuestionEvent extends EventObject {
    private Game.ANSWER mAnswer = Game.ANSWER.NONE;

    public QuestionEvent(Object source, Game.ANSWER answerState) {
        super(source);
        mAnswer = answerState;
    }

    public QuestionEvent(Object source) {
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
