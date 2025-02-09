package com.hubspotassesment.model;

import lombok.Data;
import java.util.List;

@Data
public class CallRecordsWrapper {
    private List<CallRecord> callRecords;
}
