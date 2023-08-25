package com.github.xesam.printing.api.core;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class Md5SignatureTest {
    @Test
    public void when_second_is_string_then_return_the_signature() {
        ApiSignature signature = new ApiMd5Signature();
        String sign = signature.getSignature("appid=sp5c1314095ed15&name=test&pkey=22222222&sn=111111111&timestamp=1544765873&appsecret=735aa25a15b75e6c1e0760823a22346a");

        assertThat(sign, equalTo("0D6E220C0E3FCE6A68895C0FAE0EB755"));
    }
}
