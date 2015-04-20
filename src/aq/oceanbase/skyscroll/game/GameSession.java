package aq.oceanbase.skyscroll.game;

import aq.oceanbase.skyscroll.game.tree.Tree;

public class GameSession {
    public final static int DIFFICULTY_EASY = 1;
    public final static int DIFFICULTY_MEDIUM = 2;
    public final static int DIFFICULTY_HARD = 3;

    private int difficulty;

    public Tree tree;
    public int score;

    public GameSession() {
        this.difficulty = 1;

        this.tree = new Tree();
        this.score = 0;
    }

    public GameSession(int diff) {
        this.difficulty = diff;

        this.tree = new Tree();
        this.score = 0;
    }

    public GameSession(Tree tree, int score, int diff) {
        this.difficulty = diff;
        this.tree = tree;
        this.score = score;
    }
}
