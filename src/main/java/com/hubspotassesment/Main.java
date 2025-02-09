package com.hubspotassesment;

import com.hubspotassesment.model.CallRecord;
import com.hubspotassesment.response.BillingResult;
import com.hubspotassesment.service.CallAnalyzer;
import com.hubspotassesment.utils.ApiClient;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ApiClient apiClient = new ApiClient();

        try {
            List<CallRecord> callRecords = apiClient.fetchCallRecords();
            List<BillingResult> results = CallAnalyzer.analyzeCalls(callRecords);
            apiClient.postBillingResults(results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
