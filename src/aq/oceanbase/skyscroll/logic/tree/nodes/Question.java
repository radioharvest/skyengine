package aq.oceanbase.skyscroll.logic.tree.nodes;

public class Question {
    private double mId;

    private String mType;

    private int mDifficulty;

    private String mBody;

    private String mVariants[];

    private int mAnswer;

    public Question(String text, String[] variants, int answer) {
        this.mBody = text;
        this.mVariants = variants;
        this.mAnswer = answer;
    }

    public double getId() {
        return mId;
    }

    public void setId(double id) {
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
        return mAnswer;
    }

    public void setAnswer(int answer) {
        this.mAnswer = answer;
    }
}
