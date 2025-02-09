package com.hubspotassesment.utils;

import com.google.gson.Gson;
import com.hubspotassesment.model.CallRecord;
import com.hubspotassesment.model.CallRecordsWrapper;
import com.hubspotassesment.response.BillingResult;
import com.hubspotassesment.response.BillingResultsWrapper;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class ApiClient {
    private static final String BASE_URL = "https://candidate.hubteam.com/candidateTest/v3/problem";
    private static final String API_KEY = "67af70966ea8a98785075679457e";
    private final OkHttpClient client;
    private final Gson gson;

    public ApiClient() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public List<CallRecord> fetchCallRecords() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/dataset?userKey=" + API_KEY)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Failed to fetch data: " + response.code());
        }

        String responseBody = response.body().string();

        CallRecordsWrapper wrapper = gson.fromJson(responseBody, CallRecordsWrapper.class);

        if (wrapper == null || wrapper.getCallRecords() == null) {
            throw new IOException("Failed to parse API response: callRecords is null");
        }

        return wrapper.getCallRecords();
    }

    public void postBillingResults(List<BillingResult> results) throws IOException {
        BillingResultsWrapper wrapper = new BillingResultsWrapper(results);
        String json = gson.toJson(wrapper);

        Request request = new Request.Builder()
                .url(BASE_URL + "/result?userKey=" + API_KEY)
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Failed to post results: " + response.code() + " - " + response.body().string());
        }

        System.out.println("Results successfully posted!");
    }

}
