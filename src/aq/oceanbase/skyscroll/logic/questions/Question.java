package aq.oceanbase.skyscroll.logic.questions;

import android.util.Log;
import aq.oceanbase.skyscroll.logic.Game;

import java.util.Date;
import java.util.Random;

public class Question {
    private long mId;

    private String mType;

    private int mDifficulty;

    private String mBody;

    private String mVariants[];

    private int mAnswerId;

    public Question(String text, String[] variants, int answer) {
        this.mBody = text;
        this.mVariants = variants;
        this.mAnswerId = answer;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        this.mBody = body;
    }

    public String[] getVariants() {
        return mVariants;
    }

    public void setVariants(String[] variants) {
        this.mVariants = variants;
    }

    public int getAnswer() {
        return mAnswerId;
    }

    public void setAnswer(int answer) {
        this.mAnswerId = answer;
    }

    public Question shuffleAnswers() {
        Random rand = new Random(new Date().getTime());
        for (int i = 0, k = 0; i < Math.ceil(Game.QUESTIONS_AMOUNT); i++, k++) {
            if (k >= mVariants.length) break;
            int target = rand.nextInt(mVariants.length);
            if (target != k) {
                Log.e("Error", " " + mAnswerId);
                String swap = mVariants[target];
                mVariants[target] = mVariants[k];
                mVariants[k] = swap;
                if (k == mAnswerId) mAnswerId = target;
                else if (target == mAnswerId) mAnswerId = k;
                Log.e("Error", " " + mAnswerId);
            } else {
                i--;
            }
        }

        return this;
    }
}
