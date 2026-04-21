package com.quality.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quality.domain.PdiRecord;
import com.quality.domain.PdoRecord;
import com.quality.domain.ProductionOrder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JsonDataLoaderService {

    @Value("${quality.data.production-orders}")
    private Resource productionOrdersResource;

    @Value("${quality.data.pdi-records}")
    private Resource pdiRecordsResource;

    @Value("${quality.data.pdo-records}")
    private Resource pdoRecordsResource;

    private final ObjectMapper objectMapper;

    private List<ProductionOrder> productionOrders = new ArrayList<>();
    private List<PdiRecord> pdiRecords = new ArrayList<>();
    private List<PdoRecord> pdoRecords = new ArrayList<>();

    public JsonDataLoaderService() {
        this.objectMapper = createObjectMapper();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(LocalDateTime.class, new ArrayToLocalDateTimeDeserializer());
        mapper.registerModule(customModule);
        return mapper;
    }

    @PostConstruct
    public void loadData() {
        try {
            loadProductionOrders();
            loadPdiRecords();
            loadPdoRecords();
            log.info("Successfully loaded {} production orders, {} PDI records, {} PDO records",
                    productionOrders.size(), pdiRecords.size(), pdoRecords.size());
        } catch (IOException e) {
            log.error("Failed to load JSON data: {}", e.getMessage(), e);
        }
    }

    private void loadProductionOrders() throws IOException {
        if (!productionOrdersResource.exists()) {
            log.error("Production orders file not found at: {}", productionOrdersResource);
            return;
        }
        try {
            Map<String, Object> root = objectMapper.readValue(
                    productionOrdersResource.getInputStream(),
                    new TypeReference<Map<String, Object>>() {
                    }
            );
            productionOrders = objectMapper.convertValue(
                    root.get("productionOrders"),
                    new TypeReference<List<ProductionOrder>>() {
                    }
            );
            log.info("Loaded {} production orders", productionOrders.size());
        } catch (Exception e) {
            log.error("Error parsing production orders: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void loadPdiRecords() throws IOException {
        if (!pdiRecordsResource.exists()) {
            log.error("PDI records file not found at: {}", pdiRecordsResource);
            return;
        }
        try {
            pdiRecords = objectMapper.readValue(
                    pdiRecordsResource.getInputStream(),
                    new TypeReference<List<PdiRecord>>() {
                    }
            );
            log.info("Loaded {} PDI records", pdiRecords.size());
        } catch (Exception e) {
            log.error("Error parsing PDI records: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void loadPdoRecords() throws IOException {
        if (!pdoRecordsResource.exists()) {
            log.error("PDO records file not found at: {}", pdoRecordsResource);
            return;
        }
        try {
            pdoRecords = objectMapper.readValue(
                    pdoRecordsResource.getInputStream(),
                    new TypeReference<List<PdoRecord>>() {
                    }
            );
            log.info("Loaded {} PDO records", pdoRecords.size());
        } catch (Exception e) {
            log.error("Error parsing PDO records: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ProductionOrder> getProductionOrders() {
        return productionOrders;
    }

    public List<PdiRecord> getPdiRecords() {
        return pdiRecords;
    }

    public List<PdoRecord> getPdoRecords() {
        return pdoRecords;
    }

    /**
     * Robust deserializer for LocalDateTime from array [year, month, day, hour, minute, second]
     */
    private static class ArrayToLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
        public ArrayToLocalDateTimeDeserializer() {
            super(LocalDateTime.class);
        }

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                try {
                    return LocalDateTime.parse(p.getText());
                } catch (Exception e) {
                    log.warn("Failed to parse date string: {}", p.getText());
                    return null;
                }
            }
            if (p.currentToken() == JsonToken.START_ARRAY) {
                List<Integer> values = new ArrayList<>();
                while (p.nextToken() != JsonToken.END_ARRAY) {
                    values.add(p.getIntValue());
                }
                if (values.size() < 3) return null;
                int year = values.get(0);
                int month = values.get(1);
                int day = values.get(2);
                int hour = values.size() > 3 ? values.get(3) : 0;
                int minute = values.size() > 4 ? values.get(4) : 0;
                int second = values.size() > 5 ? values.get(5) : 0;
                return LocalDateTime.of(year, month, day, hour, minute, second);
            }
            return null;
        }
    }
}