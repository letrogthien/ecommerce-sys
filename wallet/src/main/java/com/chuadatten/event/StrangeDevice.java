package com.chuadatten.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StrangeDevice {
    private String email;
    private String devideName;
    private String deviceType;
}
