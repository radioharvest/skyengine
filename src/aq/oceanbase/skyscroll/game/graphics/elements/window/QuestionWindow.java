package aq.oceanbase.skyscroll.game.graphics.elements.window;

import android.content.Context;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.logic.events.ButtonEvent;
import aq.oceanbase.skyscroll.engine.logic.window.Window;
import aq.oceanbase.skyscroll.engine.logic.window.WindowLayout;
import aq.oceanbase.skyscroll.engine.logic.window.blocks.*;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.game.Game;
import aq.oceanbase.skyscroll.game.events.*;
import aq.oceanbase.skyscroll.game.questions.Question;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;
import aq.oceanbase.skyscroll.engine.utils.Timer;

import java.util.ArrayList;
import java.util.List;

public class QuestionWindow extends Window {

    private Question mQuestion;

    private List<QuestionEventListener> mQuestionEventListeners = new ArrayList<>();

    private int mTimeToAnswer = 10000;

    public QuestionWindow (float x, float y, float z, float width, float height, Camera cam, int[] screenMetrics) {
        super(x, y, z, width, height, cam, screenMetrics);
    }

    public QuestionWindow (int screenX, int screenY, float depth, int width, int height, Camera cam, int[] screenMetrics) {
        super(screenX, screenY, depth, width, height, cam, screenMetrics);
    }

    public QuestionWindow (Vector3f position, float width, float height, Camera cam, int[] screenMetrics) {
        super(position.x, position.y, position.z, width, height, cam, screenMetrics);
    }

    public QuestionWindow (int screenX, int screenY, float depth, Camera cam, int[] screenMetrics) {
        super(screenX, screenY, depth, screenMetrics[2] - 2 * screenX, screenMetrics[3] - 2 * screenY, cam, screenMetrics);
    }

    public QuestionWindow (int offset, float depth, Camera cam, int[] screenMetrics) {
        super(offset, offset, depth, screenMetrics[2] - 2 * offset, screenMetrics[3] - 2 * offset, cam, screenMetrics);
    }

    public QuestionWindow(Camera cam, int[] screenMetrics) {
        super(cam, screenMetrics);
    }


    public void addQuestionEventListener(QuestionEventListener obj) {
        mQuestionEventListeners.add(obj);
    }

    public void removeQuestionEventListener(QuestionEventListener obj) {
        mQuestionEventListeners.remove(obj);
    }

    public void addQuestion(Question question) {
        this.mQuestion = question;

        ButtonBlock buttonBlock = new ButtonBlock(this, 9, this.getBorderOffset(), 0.0f, ButtonBlock.BUTTONLAYOUT.GRID);
        for (String answer : question.getVariants()) {
            Button button = new Button(answer);
            //button.addButtonEventListener(this);
            buttonBlock.addButton(button);
            buttonBlock.getButton(buttonBlock.getButtonsAmount()-1).addButtonEventListener(this);
        }
        addWindowEventListener(buttonBlock);
        addQuestionEventListener(buttonBlock);

        this.mLayout.setLayoutType(WindowLayout.LAYOUT.HORIZONTAL);

        WindowLayout rightSide = new WindowLayout(WindowLayout.LAYOUT.VERTICAL, this, 0.95f);
        rightSide.addChild(new NodeDisplayBlock(this, 11, R.drawable.node_display_score_100));
        rightSide.addChild(new ContentBlock(this, 8, mQuestion.getBody(), 27));
        rightSide.addChild(new TimerBarBlock(this, 1).setHorizontal());
        //rightSide.addChild(new WindowBlock(this, 0.1f));
        rightSide.addChild(buttonBlock);

        //this.mLayout.addChild(new TimerBarBlock(this, 0.05f));
        this.mLayout.addChild(rightSide);
    }


    private void fireAnswerEvent(Game.ANSWER answer) {
        QuestionEvent event = new QuestionEvent(this, answer);

        if (!mQuestionEventListeners.isEmpty()) {
            for (int i = 0; i < mQuestionEventListeners.size(); i++) {
                QuestionEventListener listener = mQuestionEventListeners.get(i);
                listener.onAnswer(event);
            }
        }
    }


    @Override
    public void onButtonPressed(ButtonEvent event) {
        mTimer = new Timer(mCloseTime).start();
        mClosing = true;

        if (event.getButtonId() == mQuestion.getAnswer()) {
            fireAnswerEvent(Game.ANSWER.CORRECT);
        }
        else {
            fireAnswerEvent(Game.ANSWER.WRONG);
        }
    }

    @Override
    protected void update() {
        if (mTimer == null) {
            mTimer = new Timer(mTimeToAnswer).start();
        }

        if (!mTimer.isRunning()) {
            if (!mClosing) fireAnswerEvent(Game.ANSWER.WRONG);
            fireCloseEvent();
        }

        super.update();
    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        super.initialize(context, programManager);


    }
}
