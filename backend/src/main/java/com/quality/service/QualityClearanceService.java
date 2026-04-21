package com.quality.service;

import com.quality.agent.QualityAgent;
import com.quality.domain.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class QualityClearanceService {

    private final JsonDataLoaderService dataLoader;
    private final QualityAgent qualityAgent;

    private final Map<String, QualityClearanceStatus> clearanceStatusMap = new ConcurrentHashMap<>();
    private final Map<String, DefectReport> defectReportMap = new ConcurrentHashMap<>();
    private final Map<String, ReworkOrder> reworkOrderMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Starting initial evaluation of all inspections...");
        evaluateAllInspections();
        log.info("Initial evaluation completed. Total clearance statuses: {}", clearanceStatusMap.size());
    }

    public void evaluateAllInspections() {
        List<ProductionOrder> orders = dataLoader.getProductionOrders();
        List<PdoRecord> pdoRecords = dataLoader.getPdoRecords();

        if (orders == null || orders.isEmpty()) {
            log.warn("No production orders loaded – cannot evaluate.");
            return;
        }

        for (ProductionOrder order : orders) {
            if (order.getInspections() == null) continue;
            for (Inspection insp : order.getInspections()) {
                PdoRecord matchingPdo = findMatchingPdo(order.getProductionOrderId(), insp.getOperationId(), pdoRecords);
                QualityClearanceStatus status = new QualityClearanceStatus();
                status.setProductionOrderId(order.getProductionOrderId());
                status.setBatchNumber(matchingPdo != null ? matchingPdo.getBatchNumber() : "N/A");
                status.setOperationId(insp.getOperationId());
                status.setInspectionId(insp.getInspectionId());

                if (matchingPdo == null) {
                    status.setStatus("MISSING");
                    status.setJudgment(null);
                } else {
                    String autoJudgment = evaluateInspection(insp, matchingPdo);
                    status.setJudgment(autoJudgment);
                    status.setStatus(autoJudgment.equals("PASS") ? "CLEARED" : "DEFECT");
                }
                String key = generateKey(status);
                clearanceStatusMap.put(key, status);
            }
        }
        log.info("Evaluated {} clearance statuses", clearanceStatusMap.size());
    }

    private PdoRecord findMatchingPdo(String prodOrderId, Integer opId, List<PdoRecord> pdoRecords) {
        return pdoRecords.stream()
                .filter(p -> p.getProductionOrderId().equals(prodOrderId) && p.getOperationId().equals(opId))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private String evaluateInspection(Inspection insp, PdoRecord pdo) {
        Map<String, Object> spec = insp.getSpecification();
        // Numeric range check (flash test)
        if (spec != null && spec.containsKey("minPower") && spec.containsKey("maxPower")) {
            Number measured = (Number) pdo.getMeasuredValue();
            if (measured == null) return "FAIL";
            double min = ((Number) spec.get("minPower")).doubleValue();
            double max = ((Number) spec.get("maxPower")).doubleValue();
            return (measured.doubleValue() >= min && measured.doubleValue() <= max) ? "PASS" : "FAIL";
        }
        // Qualitative – use PDO judgment if present, otherwise fallback to qualityPassRate
        if (pdo.getJudgment() != null) return pdo.getJudgment();
        if (pdo.getQualityPassRate() != null && pdo.getQualityPassRate() < 100.0) {
            return "FAIL";
        }
        return "PASS";   // default to PASS when no failure indicators
    }

    private String generateKey(QualityClearanceStatus status) {
        return status.getProductionOrderId() + "|" + status.getBatchNumber() + "|" + status.getOperationId();
    }

    public List<QualityClearanceStatus> getPendingClearances() {
        return clearanceStatusMap.values().stream()
                .filter(s -> !"CLEARED".equals(s.getStatus()) || s.getAcknowledgedBy() == null)
                .toList();
    }

    public QualityClearanceStatus acknowledgeClearance(String prodOrderId, String batchNumber,
                                                       Integer opId, String inspectionId,
                                                       String judgment, String remarks, String userId) {
        String key = prodOrderId + "|" + batchNumber + "|" + opId;
        QualityClearanceStatus status = clearanceStatusMap.get(key);
        if (status == null) {
            status = new QualityClearanceStatus();
            status.setProductionOrderId(prodOrderId);
            status.setBatchNumber(batchNumber);
            status.setOperationId(opId);
            status.setInspectionId(inspectionId);
        }
        status.setJudgment(judgment);
        status.setStatus("PASS".equals(judgment) ? "CLEARED" : "DEFECT");
        status.setAcknowledgedBy(userId);
        status.setAcknowledgedAt(LocalDateTime.now());
        status.setRemarks(remarks);
        clearanceStatusMap.put(key, status);
        return status;
    }

    public DefectReport createDefectReport(DefectReport report) {
        String defectId = "DEF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        report.setDefectId(defectId);
        report.setCreatedAt(LocalDateTime.now());
        defectReportMap.put(defectId, report);
        // Link to clearance status
        String key = report.getProductionOrderId() + "|" + report.getBatchNumber() + "|" + report.getOperationId();
        QualityClearanceStatus status = clearanceStatusMap.get(key);
        if (status != null) {
            status.setDefectId(defectId);
            status.setStatus("DEFECT");
        }
        return report;
    }

    public ReworkOrder createReworkOrder(ReworkOrder rework) {
        String reworkId = "RW-" + rework.getOriginalProductionOrderId() + "-" +
                UUID.randomUUID().toString().substring(0, 4);
        rework.setReworkOrderId(reworkId);
        rework.setCreatedAt(LocalDateTime.now());
        rework.setStatus("CREATED");
        reworkOrderMap.put(reworkId, rework);
        String key = rework.getOriginalProductionOrderId() + "|" + rework.getAffectedBatch() + "|0";
        QualityClearanceStatus status = clearanceStatusMap.get(key);
        if (status != null) {
            status.setReworkOrderId(reworkId);
            status.setStatus("REWORK_REQUIRED");
        }
        return rework;
    }

    public List<QualityClearanceStatus> getAllStatuses() {
        return new ArrayList<>(clearanceStatusMap.values());
    }

    public List<DefectReport> getAllDefects() {
        return new ArrayList<>(defectReportMap.values());
    }

    public List<ReworkOrder> getAllReworkOrders() {
        return new ArrayList<>(reworkOrderMap.values());
    }
}