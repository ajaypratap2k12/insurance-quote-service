package com.example.insurance.rules;

import java.util.Map;

public interface GoRulesEngine {

    Map<String, Object> evaluate(String decisionName, Map<String, Object> context);
}
