package com.vr.radalla;

import com.vr.radalla.model.MessageTemplate;
import com.vr.radalla.transform.CmsTemplateTransformer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class CmsTemplateTransformerTest {

    @Test
    void testTransformValidJson() {
        String json = """
        {
          "items": [
            {
              "fields": {
                "key": "TRAIN_DELAYED",
                "name": "Juna my\u00f6h\u00e4ss\u00e4",
                "trafficType": {
                  "sys": {
                    "id": "222-222-222"
                  }
                },
                "subject": {
                  "content": [
                    {
                      "content": [
                        { "value": "Subject" }
                      ]
                    }
                  ]
                },
                "body": {
                  "content": [
                    {
                      "content": [
                        { "value": "Body" }
                      ]
                    }
                  ]
                }
              }
            }
          ]
        }
        """;

        MessageTemplate template = CmsTemplateTransformer.transform(json);
        assertThat(template.getId()).isEqualTo("TRAIN_DELAYED");
        assertThat(template.getName()).isEqualTo("Juna myöhässä");
        assertThat(template.getTrafficType()).isEqualTo("222-222-222");
        assertThat(template.getSubject()).contains("Subject");
        assertThat(template.getBody()).contains("Body");
    }

    @Test
    void testTransformMissingFields() {
        String jsonMissingKey = "{ \"items\": [ { \"fields\": { } } ] }";

        assertThatThrownBy(() -> CmsTemplateTransformer.transform(jsonMissingKey))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to transform CMS template")
                .hasCauseInstanceOf(IllegalStateException.class)
                .cause()
                .hasMessageContaining("Missing or empty 'key' field");
    }

    @Test
    void testTransformMalformedJson() {
        String json = "{";
        assertThatThrownBy(() -> CmsTemplateTransformer.transform(json))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to transform CMS template");
    }
}
