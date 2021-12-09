package org.example;

import java.util.ArrayList;
import java.util.List;

public class Calculator {

    private final ArrayList<Score> scores;
    private final ArrayList<String> relevant;
    private final int hit;

    public Calculator(ArrayList<Score> scores, String[] relevant) {
        this(scores, relevant, 20);
    }

    public Calculator(ArrayList<Score> scores, String[] relevant, int hit) {
        scores.sort(Score::compareTo);

        this.scores = scores;
        this.relevant = new ArrayList<>(List.of(relevant));
        this.hit = hit;
    }

    @Override
    public String toString() {
        String t = "";
        for (var i = 0; i < hit; i++) t += scores.get(i).getExternalId() + "(" + scores.get(i).getScore() + "), ";
        t += "\n" + String.format("recall=%1.3f    precision=%1.3f    f-score=%1.3f", getRecall(), getPrecision(), getFScore());
        return t;
    }

    public double getFScore() {
        return getFScore(0.5f);
    }

    public double getFScore(float alpha) {
        return (double) 1 / (alpha * (1 / getRecall())) + ((1 - alpha) * (1 / getPrecision()));
    }

    public double getRecall() {
//        return (double) getTP() / (getTP() + getFN());
        return (double) getTP() / relevant.size();
    }

    public double getPrecision() {
//        return (double) getTP() / (getTP() + getFP());
        return (double) getTP() / hit;
    }

    public int getTN() {
        return scores.size() - getFP();
    }

    public int getFN() {
        return relevant.size() - getTP();
    }

    public int getFP() {
        return hit - getTP();
    }

    public int getTP() {
        int c = 0;
        for (var i = 0; i < hit; i++) {
            if (relevant.contains(scores.get(i).getExternalId())) {
                c++;
            }
        }

        return c;
    }
}
