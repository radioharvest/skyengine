package aq.oceanbase.skyscroll.graphics.elements.window;

import android.content.Context;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.Button;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.ButtonBlock;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.ContentBlock;
import aq.oceanbase.skyscroll.graphics.elements.window.blocks.TimerBarBlock;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.logic.Game;
import aq.oceanbase.skyscroll.logic.Question;
import aq.oceanbase.skyscroll.logic.events.WindowEvent;
import aq.oceanbase.skyscroll.logic.events.WindowEventListener;
import aq.oceanbase.skyscroll.utils.math.Vector3f;
import aq.oceanbase.skyscroll.utils.misc.Timer;

public class QuestionWindow extends Window {

    private Question mQuestion;

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


    public void addQuestion(Question question) {
        this.mQuestion = question;

        this.mLayout.setLayoutType(WindowLayout.LAYOUT.HORIZONTAL);

        WindowLayout rightSide = new WindowLayout(WindowLayout.LAYOUT.VERTICAL, this, 0.95f);
        rightSide.addChild(new ContentBlock(this, 1, mQuestion.getBody(), 35));
        rightSide.addChild(new ButtonBlock(this, 1, mQuestion.getVariants(), this.getBorderOffset(), 0.0f));

        this.mLayout.addChild(new TimerBarBlock(this, 0.05f));
        this.mLayout.addChild(rightSide);
    }


    private void fireAnswerEvent(Game.ANSWER answer) {
        WindowEvent event = new WindowEvent(this, answer);

        if (!mEventListeners.isEmpty()) {
            for (int i = 0; i < mEventListeners.size(); i++) {
                WindowEventListener listener = (WindowEventListener)mEventListeners.get(i);
                listener.onAnswer(event);
            }
        }
    }


    @Override
    public void onButtonPressed(ButtonBlock buttonBlock, int buttonId) {
        mTimer = new Timer(mCloseTime).start();
        mClosing = true;

        if (buttonId == mQuestion.getAnswer()) {
            buttonBlock.highlightButton(buttonId, Button.STATE.CORRECT);
            fireAnswerEvent(Game.ANSWER.CORRECT);
        }
        else {
            buttonBlock.highlightButton(buttonId, Button.STATE.WRONG);
            fireAnswerEvent(Game.ANSWER.WRONG);
        }
    }

    @Override
    protected void update() {
        super.update();

        if (!mTimer.isRunning()) {
            if (!mClosing) fireAnswerEvent(Game.ANSWER.WRONG);
            fireCloseEvent();
        }

    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        super.initialize(context, programManager);

        mTimer = new Timer(mTimeToAnswer).start();
    }
}
