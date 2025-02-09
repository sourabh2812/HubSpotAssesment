package com.hubspotassesment.response;

import lombok.Data;
import java.util.List;

@Data
public class BillingResultsWrapper {
    private final List<BillingResult> results;

    public BillingResultsWrapper(List<BillingResult> results) {
        this.results = results;
    }
}
