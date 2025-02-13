package com.ddib.payment.payment.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KakaoProperties {

    public String kakaoApprovalUrl;

    public String kakaoCancelUrl;

    public String kakaoFailUrl;


    public String orderCompleteUrl;

    public String orderFailUrl;

    public String payCancelUrl;

    public String payFailUrl;

}
