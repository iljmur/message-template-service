package com.vr.radalla.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplate {
    private String id;               // from CMS 'key'
    private String name;            // template name
    private String trafficType;     // e.g., SMS, Email, etc.
    private String subject;         // transformed plain text with {PARAM}
    private String body;            // same as above
}
