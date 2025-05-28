package services;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliusOffenderApiTest {
    @Test
    public void buildsAValidQueryParamString() {
        DeliusOffenderApi deliusOffenderApi = new DeliusOffenderApi(ConfigFactory.load(), null);
        Map<String, String> params = new HashMap<>();
        params.put("surname", "smith");
        assertThat(deliusOffenderApi.queryParamsFrom(params)).isEqualTo("?surname=smith");

        params.put("forename", "john");
        assertThat(deliusOffenderApi.queryParamsFrom(params)).isEqualTo("?forename=john&surname=smith");
    }
}
