package com.quality.controller;

import com.quality.agent.QualityAgent;
import com.quality.domain.*;
import com.quality.service.JsonDataLoaderService;
import com.quality.service.QualityClearanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quality")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class QualityController {

    private final QualityClearanceService clearanceService;
    private final JsonDataLoaderService dataLoader;
    private final QualityAgent qualityAgent;

    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, String>> evaluateAll() {
        clearanceService.evaluateAllInspections();
        return ResponseEntity.ok(Map.of("status", "Evaluation completed"));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<QualityClearanceStatus>> getPending() {
        return ResponseEntity.ok(clearanceService.getPendingClearances());
    }

    @GetMapping("/status")
    public ResponseEntity<List<QualityClearanceStatus>> getAllStatus() {
        return ResponseEntity.ok(clearanceService.getAllStatuses());
    }

    @PostMapping("/acknowledge")
    public ResponseEntity<QualityClearanceStatus> acknowledge(@RequestBody AcknowledgmentRequest request) {
        QualityClearanceStatus status = clearanceService.acknowledgeClearance(
                request.getProductionOrderId(),
                request.getBatchNumber(),
                request.getOperationId(),
                request.getInspectionId(),
                request.getJudgment(),
                request.getRemarks(),
                "quality.engineer"  // in real app, get from security context
        );
        return ResponseEntity.ok(status);
    }

    @PostMapping("/defect")
    public ResponseEntity<DefectReport> createDefect(@RequestBody DefectReport report) {
        return ResponseEntity.ok(clearanceService.createDefectReport(report));
    }

    @PostMapping("/rework")
    public ResponseEntity<ReworkOrder> createRework(@RequestBody ReworkOrder rework) {
        return ResponseEntity.ok(clearanceService.createReworkOrder(rework));
    }

    @GetMapping("/defects")
    public ResponseEntity<List<DefectReport>> getDefects() {
        return ResponseEntity.ok(clearanceService.getAllDefects());
    }

    @GetMapping("/rework")
    public ResponseEntity<List<ReworkOrder>> getReworkOrders() {
        return ResponseEntity.ok(clearanceService.getAllReworkOrders());
    }

    @GetMapping("/insights")
    public ResponseEntity<Map<String, String>> getInsights() {
        String insights = qualityAgent.analyseQualityData(
                dataLoader.getProductionOrders(),
                dataLoader.getPdoRecords(),
                clearanceService.getAllDefects()
        );
        return ResponseEntity.ok(Map.of("insights", insights));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("clearanceStatus", clearanceService.getAllStatuses());
        dashboard.put("defects", clearanceService.getAllDefects());
        dashboard.put("reworkOrders", clearanceService.getAllReworkOrders());
        dashboard.put("summary", Map.of(
                "totalInspections", clearanceService.getAllStatuses().size(),
                "cleared", clearanceService.getAllStatuses().stream().filter(s -> "CLEARED".equals(s.getStatus())).count(),
                "defects", clearanceService.getAllStatuses().stream().filter(s -> "DEFECT".equals(s.getStatus())).count(),
                "reworkRequired", clearanceService.getAllStatuses().stream().filter(s -> "REWORK_REQUIRED".equals(s.getStatus())).count()
        ));
        return ResponseEntity.ok(dashboard);
    }
}