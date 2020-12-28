package com.mt.common;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class EurekaRegistryHelper {
    @Autowired
    EurekaClient discoveryClient;

    public String getProxyHomePageUrl() {
        List<InstanceInfo> proxy = discoveryClient.getApplication("PROXY").getInstances();
        String var0 = "http://" + proxy.get(0).getIPAddr() + ":" + proxy.get(0).getPort();
        log.debug("proxy url retrieved from eureka client {}", var0);
        return var0;
    }

    public String getValidatorUrl() {
        List<InstanceInfo> validator = discoveryClient.getApplication("VALIDATOR").getInstances();
        String var0 = "http://" + validator.get(0).getIPAddr() + ":" + validator.get(0).getPort();
        log.debug("validator url retrieved from eureka client {}", var0);
        return var0;
    }
}
