package com.bezkoder.springjwt.awsiot.aws.iot.device.config;

import com.amazonaws.services.iot.model.*;
import com.amazonaws.services.iot.model.ResourceNotFoundException;
import com.amazonaws.services.iotdata.AWSIotData;
import com.amazonaws.services.iotdata.AWSIotDataClientBuilder;
import com.amazonaws.services.iotdata.model.*;
import com.bezkoder.springjwt.awsiot.aws.iot.device.Payload.WaterPayload;
import com.bezkoder.springjwt.models.Device;
import com.bezkoder.springjwt.payload.awspayload.Command;
import com.bezkoder.springjwt.payload.request.ChangeTempuratureRequest;
import com.bezkoder.springjwt.payload.request.ShowerControlRequest;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.repository.DeviceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClientBuilder;
import software.amazon.awssdk.services.iot.model.DescribeThingResponse;

/**
 * publish message to MQTT service
 */
@Service
@Slf4j
public class MQTTServiceImpl {

    /**
     * get the aws iot MQTT client
     */
    @Autowired
    private AwsClientFactory iotClient;

    /**
     * AWS IOT client config
     */
    @Autowired
    private AwsIotAccountConfig awsIotAccountConfig;

    @Autowired
    private DeviceRepository deviceRepository;

    private static ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    /**
     * MQTT end point for Setting
     */
    private static final String AWS_MQTT_END_POINT_FOR_SETTING = "setting";

    /**
     * MQTT end point for Control
     */
    private static final String AWS_MQTT_END_POINT_FOR_CONTROL = "devices";


    private static final Set<String> commandSet = new HashSet<>(Arrays.asList("SHOWERON", "SHOWEROFF", "SHOWERPREHEAT", "ST"));

    /**
     * Method to change the temperature for the device
     *
     * @param changeTempuratureRequest
     * @throws IOException
     */
    public void publishToShadow(ChangeTempuratureRequest changeTempuratureRequest) throws Exception {
        int temp = 0;
        if (changeTempuratureRequest.getTemp_type().equals("HIGH")) {
            temp = 100;
        } else if (changeTempuratureRequest.getTemp_type().equals("MEDIUM")) {
            temp = 95;
        } else if (changeTempuratureRequest.getTemp_type().equals("LOW")) {
            temp = 85;
        } else {
            throw new Exception("No temp found , plz select LOW, MEDIUM OR HIGH");
        }

        String device_id = changeTempuratureRequest.getDevice_id();
        String topic = AWS_MQTT_END_POINT_FOR_SETTING + "/" + device_id;
        WaterPayload waterPayload = new WaterPayload();
        waterPayload.setTemp(String.valueOf(temp));
        waterPayload.setDevice_id(changeTempuratureRequest.getDevice_id());
        String payload = ow.writeValueAsString(waterPayload);
        ByteBuffer bb = StandardCharsets.UTF_8.encode(payload);
        PublishRequest publishRequest = new PublishRequest();
        publishRequest.withPayload(bb);
        publishRequest.withTopic(topic);
        publishRequest.setQos(0);
        iotClient.getIotDataClient(awsIotAccountConfig).publish(publishRequest);
    }

    public ResponseEntity<MessageResponse> ShowerControl(ShowerControlRequest showerControlRequest) throws JsonProcessingException {
        String device_id = showerControlRequest.getDevice_id();
        String topic = AWS_MQTT_END_POINT_FOR_CONTROL + "/" + device_id + "/command";
        System.out.println(topic);
        String command = showerControlRequest.getCommand();
        if (!commandSet.contains(command)) {
            return ResponseEntity.ok().body(new MessageResponse("InValid Command Please check again"));
        }

        if (command.equals("SHOWERON")) {
            command = "ON";
        } else if (command.equals("SHOWEROFF")) {
            command = "OF";
        } else if (command.equals("SHOWERPREHEAT")) {
            command = "PH";
        }

        Device device = deviceRepository.findByDeviceId(device_id);
        if (device == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: device not found "));
        }
        int idlet = device.getIdlet();
        int maxwater = device.getMaxwater();
        Command command1 = new Command();
        command1.setCmd(command);
        command1.setTemp(device.getTemp());
        command1.setIdlet(idlet);
        command1.setMaxwater(maxwater);
        command1.setMd(device.getMode());
        command1.setRg(device.getRg());
        ByteBuffer bb = StandardCharsets.UTF_8.encode(ow.writeValueAsString(command1));
        PublishRequest publishRequest = new PublishRequest();
        publishRequest.withPayload(bb);
        publishRequest.withTopic(topic);
        publishRequest.setQos(0);
        log.info("Send command to device {}, with payload {}", device_id, command1.toString());
        iotClient.getIotDataClient(awsIotAccountConfig).publish(publishRequest);
        return ResponseEntity.ok().body(new MessageResponse("Send ShowerControl Successfully"));
    }

    public String getRetainedMessage(String deviceId) {
        String topic = "devices/" + deviceId + "/connection";
        System.out.println(topic);

        AWSIotData iotDataClient = AWSIotDataClientBuilder.standard().build();
        GetRetainedMessageRequest request = new GetRetainedMessageRequest().withTopic(topic);

        try {
            GetRetainedMessageResult result = iotDataClient.getRetainedMessage(request);
            String retainedMessage = new String(result.getPayload().array(), StandardCharsets.UTF_8);
            return retainedMessage;
        } catch (ResourceNotFoundException e) {
            log.error("Retained message not found for topic: {}", topic, e);
            return "Error: Retained message not found for topic " + topic;
        } catch (Exception e) {
            log.error("Error retrieving retained message: {}", e.getMessage(), e);
            return "Error retrieving retained message: " + e.getMessage();
        }
    }

}
