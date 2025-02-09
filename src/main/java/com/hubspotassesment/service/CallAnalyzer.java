package com.hubspotassesment.service;

import com.hubspotassesment.model.CallRecord;
import com.hubspotassesment.response.BillingResult;

import java.text.SimpleDateFormat;
import java.util.*;

public class CallAnalyzer {

    public static List<BillingResult> analyzeCalls(List<CallRecord> callRecords) {
        Map<Integer, Map<String, List<CallRecord>>> customerCallsByDate = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (CallRecord call : callRecords) {
            long start = call.getStartTimestamp();
            long end = call.getEndTimestamp();

            // Process each day a call spans
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(start);

            while (calendar.getTimeInMillis() < end) {
                String date = sdf.format(calendar.getTime());

                customerCallsByDate
                        .computeIfAbsent(call.getCustomerId(), k -> new HashMap<>())
                        .computeIfAbsent(date, k -> new ArrayList<>())
                        .add(call);

                // Move to next day
                calendar.add(Calendar.DATE, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }
        }

        // Process each customer and date
        List<BillingResult> results = new ArrayList<>();
        for (var customerEntry : customerCallsByDate.entrySet()) {
            int customerId = customerEntry.getKey();
            for (var dateEntry : customerEntry.getValue().entrySet()) {
                String date = dateEntry.getKey();
                List<CallRecord> calls = dateEntry.getValue();

                // Find peak concurrent calls
                BillingResult result = findPeakConcurrentCalls(customerId, date, calls);
                results.add(result);
            }
        }

        return results;
    }

    private static BillingResult findPeakConcurrentCalls(int customerId, String date, List<CallRecord> calls) {
        List<Long> timestamps = new ArrayList<>();

        for (CallRecord call : calls) {
            timestamps.add(call.getStartTimestamp());
            timestamps.add(call.getEndTimestamp());
        }

        timestamps.sort(Long::compare);

        int maxConcurrent = 0;
        long peakTimestamp = 0;
        List<String> peakCallIds = new ArrayList<>();
        Set<String> activeCalls = new HashSet<>();

        for (long ts : timestamps) {
            for (CallRecord call : calls) {
                if (call.getStartTimestamp() == ts) {
                    activeCalls.add(call.getCallId());
                }
                if (call.getEndTimestamp() == ts) {
                    activeCalls.remove(call.getCallId());
                }
            }

            if (activeCalls.size() > maxConcurrent) {
                maxConcurrent = activeCalls.size();
                peakTimestamp = ts;
                peakCallIds = new ArrayList<>(activeCalls);
            }
        }

        Collections.sort(peakCallIds);

        BillingResult result = new BillingResult();
        result.setCustomerId(customerId);
        result.setDate(date);
        result.setMaxConcurrentCalls(maxConcurrent);
        result.setTimestamp(peakTimestamp);
        result.setCallIds(peakCallIds);

        return result;
    }
}
