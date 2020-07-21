package guru.springframework.sbmbeerorderservice.services.testcomponents;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ITConfig {

    public static final String FAILED_VALIDATION = "fail-validation";

    public static final String FAILED_ALLOCATION = "fail-allocation";
    public static final String PARTIAL_ALLOCATION = "partial-allocation";

    public static final Integer MINUS_BEERS_FOR_PARTIAL_ALLOCATION = 1;
}
