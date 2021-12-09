package org.example;

public class Score implements Comparable<Score>{

    private final String externalId;
    private final double score;

    public Score(String externalId, double score) {
        this.externalId = externalId;
        this.score = score;
    }

    public String getExternalId() {
        return externalId;
    }

    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(Score o) {
        return Double.compare(score, o.getScore()) * -1;
    }
}
