package com.example.insurance.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gorules.zen_engine.JsonBuffer;
import io.gorules.zen_engine.ZenDecision;
import io.gorules.zen_engine.ZenEngineResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class GoRulesEngineImpl implements GoRulesEngine {

    private final RuleManager ruleManager;
    private final ObjectMapper objectMapper;

    public GoRulesEngineImpl(RuleManager ruleManager, ObjectMapper objectMapper) {
        this.ruleManager = ruleManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> evaluate(String decisionName, Map<String, Object> context) {
        log.info("Evaluating decision: {} with context: {}", decisionName, context);

        ZenDecision decision = ruleManager.getDecision(decisionName);
        if (decision == null) {
            log.warn("Decision not found: {}, attempting to load", decisionName);
            decision = ruleManager.loadDecision(decisionName);
        }

        try {
            String jsonContext = objectMapper.writeValueAsString(context);
            ZenEngineResponse response = decision.evaluate(new JsonBuffer(jsonContext), null).join();

            String resultJson = response.result().toString();

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(resultJson, Map.class);

            log.info("Decision {} evaluated successfully, result: {}", decisionName, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to evaluate decision: {}", decisionName, e);
            throw new RuntimeException("Decision evaluation failed: " + decisionName, e);
        }
    }
}
