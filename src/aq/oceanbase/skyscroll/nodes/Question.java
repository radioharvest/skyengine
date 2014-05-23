package aq.oceanbase.skyscroll.nodes;

public class Question {
    private double id;

    private String type;

    private int difficulty;

    private String body;

    private String variants[];

    private int answer;

    public double getId() {
        return id;
    }

    public void setId(double id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String[] getVariants() {
        return variants;
    }

    public void setVariants(String[] variants) {
        this.variants = variants;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }
}
