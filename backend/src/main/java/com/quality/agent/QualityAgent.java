package com.quality.agent;

import com.quality.domain.DefectReport;
import com.quality.domain.PdoRecord;
import com.quality.domain.ProductionOrder;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class QualityAgent {

    private final ChatLanguageModel llm;
    private final boolean aiEnabled;

    public QualityAgent(@Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
                        @Value("${ollama.model:llama3.2}") String modelName,
                        @Value("${ollama.temperature:0.2}") double temperature) {
        ChatLanguageModel tmpLlm = null;
        boolean tmpEnabled = false;
        try {
            tmpLlm = OllamaChatModel.builder()
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .temperature(temperature)
                    .build();
            tmpEnabled = true;
            log.info("AI agent enabled with Ollama model: {} at {}", modelName, baseUrl);
        } catch (Exception e) {
            log.warn("Failed to initialize Ollama: {}. AI insights disabled.", e.getMessage());
        }
        this.llm = tmpLlm;
        this.aiEnabled = tmpEnabled;
    }

    /**
     * Analyse quality data and provide insights.
     */
    public String analyseQualityData(List<ProductionOrder> orders, List<PdoRecord> pdoRecords,
                                     List<DefectReport> defects) {
        if (!aiEnabled || llm == null) {
            return buildRuleBasedInsights(orders, pdoRecords, defects);
        }
        try {
            String prompt = buildPrompt(orders, pdoRecords, defects);
            Response<AiMessage> response = llm.generate(List.of(
                    SystemMessage.from("You are a quality assurance expert for solar panel manufacturing. Analyse the data and provide concise insights, root cause analysis, and actionable recommendations."),
                    UserMessage.from(prompt)
            ));
            return response.content().text();
        } catch (Exception e) {
            log.error("AI analysis failed: {}", e.getMessage());
            return buildRuleBasedInsights(orders, pdoRecords, defects);
        }
    }

    private String buildPrompt(List<ProductionOrder> orders, List<PdoRecord> pdoRecords,
                               List<DefectReport> defects) {
        StringBuilder sb = new StringBuilder();
        sb.append("Production orders with quality issues:\n");
        for (ProductionOrder o : orders) {
            if (o.getInspections() == null) continue;
            for (var insp : o.getInspections()) {
                if ("FAIL".equals(insp.getJudgment()) || "FAIL".equals(insp.getStatus())) {
                    sb.append(String.format("- Order %s, Inspection %s: %s\n",
                            o.getProductionOrderId(), insp.getInspectionId(), insp.getDescription()));
                }
            }
        }
        sb.append("\nDefects recorded:\n");
        for (DefectReport d : defects) {
            sb.append(String.format("- %s: %s (qty %d) severity %s\n",
                    d.getDefectCode(), d.getDescription(), d.getQuantityAffected(), d.getSeverity()));
        }
        sb.append("\nScrap reasons from PDO records:\n");
        Map<String, Long> scrapCount = pdoRecords.stream()
                .filter(p -> p.getScrapReasonCode() != null && !p.getScrapReasonCode().isBlank())
                .collect(Collectors.groupingBy(PdoRecord::getScrapReasonCode, Collectors.counting()));
        scrapCount.forEach((reason, count) -> sb.append(String.format("- %s: %d occurrences\n", reason, count)));
        sb.append("\nProvide: 1) Top 3 root causes, 2) Corrective actions, 3) Process improvements.");
        return sb.toString();
    }

    private String buildRuleBasedInsights(List<ProductionOrder> orders, List<PdoRecord> pdoRecords,
                                          List<DefectReport> defects) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RULE-BASED QUALITY INSIGHTS ===\n");
        long totalScrap = pdoRecords.stream().mapToInt(PdoRecord::getScrapQuantity).sum();
        long totalGood = pdoRecords.stream().mapToInt(PdoRecord::getGoodQuantity).sum();
        double overallYield = totalGood + totalScrap > 0 ? (double) totalGood / (totalGood + totalScrap) * 100 : 100;
        sb.append(String.format("Overall yield: %.1f%%\n", overallYield));
        Map<String, Long> scrapByReason = pdoRecords.stream()
                .filter(p -> p.getScrapReasonCode() != null)
                .collect(Collectors.groupingBy(PdoRecord::getScrapReasonCode, Collectors.counting()));
        scrapByReason.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> sb.append(String.format("Top scrap reason: %s (%d occurrences)\n", e.getKey(), e.getValue())));
        sb.append("\nRECOMMENDATIONS:\n");
        sb.append("1. Focus on the most frequent scrap reason.\n");
        sb.append("2. Review inspection specifications for failing inspections.\n");
        sb.append("3. Consider additional training on the affected operations.\n");
        return sb.toString();
    }
}