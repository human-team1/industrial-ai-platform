package com.example.factoryguard.domain.inspection.model;

public class DecisionCalculator {

    private DecisionCalculator() {}

    public static DecisionCode decide(double score, double confidence,
                                      double anomalyThreshold, double lowConfidenceThreshold) {
        if (score >= anomalyThreshold) {
            return DecisionCode.DEFECT;
        }
        if (confidence < lowConfidenceThreshold) {
            return DecisionCode.RECHECK;
        }
        return DecisionCode.NORMAL;
    }
}