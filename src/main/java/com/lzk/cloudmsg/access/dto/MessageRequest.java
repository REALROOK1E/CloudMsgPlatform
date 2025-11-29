package com.lzk.cloudmsg.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    
    /**
     * Template ID
     */
    private Long templateId;

    /**
     * Business ID (for tracing)
     */
    private String businessId;

    /**
     * Receivers (User IDs, Phone numbers, etc.)
     */
    private Set<String> receivers;

    /**
     * Variables for placeholders in the template
     */
    private Map<String, String> variables;

    /**
     * Crowd Key (for large scale sending)
     */
    private String crowdKey;
}
