package com.chuadatten.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationEvent {
    private String email; 
    private String urlActive;
    private String userId;
}
