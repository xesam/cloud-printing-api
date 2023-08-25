package com.github.xesam.printing.api.core;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class Sha1SignatureTest {
    private static final ApiAuth TEST_API_AUTH = new ApiAuth("test_id", "test_secret");

    @Test
    public void when_second_is_string_then_return_the_signature() {
        ApiSignature signature = new ApiSha1Signature();
        String sign = signature.getSignature(TEST_API_AUTH.getAppId(), TEST_API_AUTH.getSecret(), "1000000000");

        assertThat(sign, equalTo("c92c63ca5be6d9d31c71a8cc7e6140d59f79a9af"));
    }
}
