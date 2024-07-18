package com.bezkoder.springjwt.security.notification;

import com.bezkoder.springjwt.awsiot.aws.iot.device.config.AwsClientFactory;
import com.bezkoder.springjwt.awsiot.aws.iot.device.config.AwsIotAccountConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class NotificationService {

    private Map<String, SseEmitter> userEmitterMap = new ConcurrentHashMap<>();

    @Autowired
    private AwsClientFactory iotClient;

    @Autowired
    private AwsIotAccountConfig awsIotAccountConfig;

    public SseEmitter getOrCreateSseEmitter(String device_id){
        if(userEmitterMap.containsKey(device_id)){
            log.info("Get emmiter in memeory for device_id {}", device_id);
            return userEmitterMap.get(device_id);
        }


        SseEmitter emitter = new SseEmitter(-1l);
        log.info("Get emmiter for device_id {}", device_id);
        userEmitterMap.put(device_id, emitter);
        return emitter;
    }
}
