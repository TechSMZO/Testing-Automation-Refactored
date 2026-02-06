package tests;

import org.testng.SkipException;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;






/**
 * Executes a 3rd-party API concurrently using different input data.
 *
 * How to run:
 * - Provide keys with system properties (recommended so secrets don't live in code):
 *   -DappiifyPublicKey="..."
 *   -DappiifyPrivateKey="..."
 *
 * Optional properties:
 * -DapiThreads=5
 * -DapiTimeoutSeconds=30
 */

public class ThirdPartyApiMultiThreadTest {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final String APPIIFY_PUSH_ORDER_URL = "https://appiify.com/app/api/v1/push-order";

    private record ApiResult(String input, int statusCode, long elapsedMs, String responseBody, String error) {
        boolean isSuccess() {
            return error == null && statusCode >= 200 && statusCode < 300;
        }
    }

    @Test
    public void executeThirdPartyApiInMultipleThreads() throws InterruptedException {
        long testStartNs = System.nanoTime();
        String publicKey = System.getProperty("appiifyPublicKey");
        String privateKey = System.getProperty("appiifyPrivateKey");
        if (isBlank(publicKey) || isBlank(privateKey)) {
            throw new SkipException("Set -DappiifyPublicKey and -DappiifyPrivateKey to run this test.");
        }

        int threads = intProp("apiThreads", 500);
        Duration timeout = Duration.ofSeconds(intProp("apiTimeoutSeconds", (int) DEFAULT_TIMEOUT.getSeconds()));

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, threads));
        try {
            List<String> payloads = buildHundredSameDataPayloads();

            List<Callable<ApiResult>> tasks = new ArrayList<>();
            for (String payload : payloads) {
                tasks.add(() -> callPushOrder(client, publicKey, privateKey, payload, timeout));
            }

            List<Future<ApiResult>> futures = pool.invokeAll(tasks);

            List<ApiResult> results = new ArrayList<>(futures.size());
            for (Future<ApiResult> f : futures) {
                try {
                    results.add(f.get());
                } catch (ExecutionException e) {
                    results.add(new ApiResult("<unknown>", 0, 0, null, "ExecutionException: " + e.getCause()));
                }
            }

            // Print a short summary (kept simple for CI logs).
            results.sort(Comparator.comparing(ApiResult::input));
            for (ApiResult r : results) {
                String status = r.isSuccess() ? "SUCCESS" : "FAIL";
                String err = r.error == null ? "" : (" | error=" + r.error);
                System.out.println("order_id=" + r.input + " | http=" + r.statusCode + " | timeMs=" + r.elapsedMs + " | " + status + err);
                // Print response body (trimmed to avoid huge logs)
                if (r.responseBody != null) {
                    String body = r.responseBody;
                    int maxLen = 500;
                    if (body.length() > maxLen) {
                        body = body.substring(0, maxLen) + "...(truncated)";
                    }
                    System.out.println("  responseBody=" + body);
                }
            }
            long ok = results.stream().filter(ApiResult::isSuccess).count();
            long fail = results.size() - ok;
            long totalMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - testStartNs);
            System.out.println("API parallel run finished. total=" + results.size() + ", success=" + ok + ", failed=" + fail + ", totalTimeMs=" + totalMs);

            // If you want the build to fail when any request fails, keep this assertion.
            // If you prefer "best effort", comment it out.
            if (fail > 0) {
                ApiResult firstFailure = results.stream().filter(r -> !r.isSuccess()).findFirst().orElse(null);
                String details = (firstFailure == null)
                        ? "Unknown failure"
                        : ("input=" + firstFailure.input + ", status=" + firstFailure.statusCode + ", error=" + firstFailure.error);
                throw new AssertionError("Some API calls failed. " + details);
            }
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        }
    }

    private static ApiResult callPushOrder(HttpClient client, String publicKey, String privateKey, String jsonBody, Duration timeout) {
        long start = System.nanoTime();
        try {
            String orderId = extractOrderId(jsonBody);
            HttpRequest request = buildPushOrderRequest(publicKey, privateKey, jsonBody, timeout);
            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            return new ApiResult(orderId, resp.statusCode(), elapsedMs, resp.body(), null);
        } catch (Exception e) {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            return new ApiResult("<unknown-order-id>", 0, elapsedMs, null, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static HttpRequest buildPushOrderRequest(String publicKey, String privateKey, String jsonBody, Duration timeout) {
        return HttpRequest.newBuilder()
                .uri(URI.create(APPIIFY_PUSH_ORDER_URL))
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Accept", "application/json")
                .header("public-key", publicKey)
                .header("private-key", privateKey)
                .header("Content-Type", "application/json")
                .header("X-CSRF-TOKEN", "")
                .build();
    }

    private static List<String> buildHundredSameDataPayloads() {
        // Build 500 orders with the same business data as the sample curl.
        // Only order_id (reference) changes so each is unique.
        LocalDate date = LocalDate.of(2026, 1, 6); // fixed to match your curl example
        long runId = System.currentTimeMillis();

        List<String> payloads = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            String orderId = "ordID_" + runId + "_" + i;
            long phone = 8000042323L;          // same as curl
            long altPhone = 8000142323L;
            int pincode = 122001;
            String consigneeName = "John";     // strictly alphabetic, no digits/special chars

            String json = """
                    {
                      "order_id": "%s",
                      "order_date": "%s",
                      "order_type": "ESSENTIALS",
                      "consignee_name": "%s",
                      "consignee_phone": %d,
                      "consignee_alternate_phone": %d,
                      "consignee_email": "johnhelp+%d@gmail.com",
                      "consignee_address_line_one": "Sector 49",
                      "consignee_address_line_two": "Sohna Road",
                      "consignee_pin_code": %d,
                      "consignee_city": "Gurgaon",
                      "consignee_state": "Haryana",
                      "product_detail": [
                        {
                          "name": "Laptop",
                          "sku_number": "22",
                          "quantity": 1,
                          "discount": "",
                          "hsn": "#123",
                          "unit_price": 1000,
                          "product_category": "Other"
                        }
                      ],
                      "payment_type": "PREPAID",
                      "cod_amount": "",
                      "shipping_charges": "",
                      "weight": 200,
                      "length": 10,
                      "width": 20,
                      "height": 15,
                      "warehouse_id": "",
                      "gst_ewaybill_number": "",
                      "gstin_number": ""
                    }
                    """.formatted(orderId, date, consigneeName, phone, altPhone, i, pincode);

            payloads.add(json);
        }
        return payloads;
    }

    private static int intProp(String key, int defaultVal) {
        String raw = System.getProperty(key);
        if (raw == null || raw.trim().isEmpty()) return defaultVal;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String extractOrderId(String jsonBody) {
        // Minimal extraction for log labels without adding a JSON dependency.
        // Expects: "order_id": "value"
        String needle = "\"order_id\":";
        int idx = jsonBody.indexOf(needle);
        if (idx < 0) return "<unknown-order-id>";
        int firstQuote = jsonBody.indexOf('"', idx + needle.length());
        if (firstQuote < 0) return "<unknown-order-id>";
        int secondQuote = jsonBody.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) return "<unknown-order-id>";
        return jsonBody.substring(firstQuote + 1, secondQuote);
    }
}

