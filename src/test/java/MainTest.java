import com.google.gson.Gson;
import com.hupspotassesment.model.CallRecord;
import com.hupspotassesment.model.CallRecordsWrapper;
import com.hupspotassesment.response.BillingResult;
import com.hupspotassesment.response.BillingResultsWrapper;
import com.hupspotassesment.service.CallAnalyzer;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MainTest {
    private static final String BASE_URL = "https://candidate.hubteam.com/candidateTest/v3/problem";
    private static final String API_KEY = "67af70966ea8a98785075679457e";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    /**
     * Test the call analysis using test dataset and validate against expected results.
     */
    @Test
    public void testCallAnalyzerWithTestDataset() {
        try {
            // Fetch test dataset and expected answer
            List<CallRecord> testCallRecords = fetchTestDataset();
            List<BillingResult> expectedResults = fetchTestDatasetAnswer();

            // Process the test dataset
            List<BillingResult> actualResults = CallAnalyzer.analyzeCalls(testCallRecords);

            // Compare actual vs expected
            assertEquals(expectedResults, actualResults, "Test case failed: Results do not match.");

            // Post to test API and verify 200 OK response
            int responseCode = postTestResults(actualResults);
            assertEquals(200, responseCode, "Failed to post test results!");

        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

    /**
     * Fetch the test dataset from API.
     */
    private List<CallRecord> fetchTestDataset() throws IOException {
        String url = BASE_URL + "/test-dataset?userKey=" + API_KEY;
        String responseBody = get(url);
        CallRecordsWrapper wrapper = gson.fromJson(responseBody, CallRecordsWrapper.class);

        if (wrapper == null || wrapper.getCallRecords() == null) {
            throw new IOException("Failed to parse API response: test-dataset is null");
        }
        return wrapper.getCallRecords();
    }

    /**
     * Fetch the expected results for the test dataset.
     */
    private List<BillingResult> fetchTestDatasetAnswer() throws IOException {
        String url = BASE_URL + "/test-dataset-answer?userKey=" + API_KEY;
        String responseBody = get(url);
        BillingResultsWrapper wrapper = gson.fromJson(responseBody, BillingResultsWrapper.class);

        if (wrapper == null || wrapper.getResults() == null) {
            throw new IOException("Failed to parse API response: expected answer is null");
        }
        return wrapper.getResults();
    }

    /**
     * Posts the test results to API for validation.
     */
    private int postTestResults(List<BillingResult> results) throws IOException {
        String url = BASE_URL + "/test-result?userKey=" + API_KEY;
        String json = gson.toJson(new BillingResultsWrapper(results));
        return post(url, json);
    }

    /**
     * Sends a GET request to the given URL.
     */
    private String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("GET request failed: " + response.code());
        }
        return response.body().string();
    }

    /**
     * Sends a POST request to the given URL.
     */
    private int post(String url, String json) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        Response response = client.newCall(request).execute();
        int responseCode = response.code();
        response.close();
        return responseCode;
    }
}
