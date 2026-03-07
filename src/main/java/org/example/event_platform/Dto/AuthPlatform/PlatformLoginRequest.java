package org.example.event_platform.Dto.AuthPlatform;

import lombok.Data;

@Data
public class PlatformLoginRequest {
    private String username;
    private String password;
}
