package com.example.social_chat.utils;


import lombok.Getter;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Getter
@Component
public class InstanceInfo {

    private final String instanceId;

    public InstanceInfo() throws UnknownHostException {
        instanceId = InetAddress.getLocalHost().getHostName();
    }

}