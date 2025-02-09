package com.hubspotassesment.model;

import lombok.Data;

@Data
public class CallRecord {
    private int customerId;
    private String callId;
    private long startTimestamp;
    private long endTimestamp;
}