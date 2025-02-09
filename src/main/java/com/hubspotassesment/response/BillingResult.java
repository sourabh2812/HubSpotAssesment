package com.hubspotassesment.response;

import lombok.Data;
import java.util.List;

@Data
public class BillingResult {
    private int customerId;
    private String date;
    private int maxConcurrentCalls;
    private long timestamp;
    private List<String> callIds;
}