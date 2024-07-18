package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.awsiot.aws.iot.device.config.MQTTServiceImpl;
import com.bezkoder.springjwt.payload.request.ChangeTempuratureRequest;
import com.bezkoder.springjwt.payload.request.ShowerControlRequest;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/api/iot/device")
public class IotDeviceController {
    @Autowired
    private MQTTServiceImpl mqttService;


    /**
     * Setting temp for the deivce without start it
     *
     * @param changeTempuratureRequest
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/settingTemperature")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Object> settingTemperature (@RequestBody ChangeTempuratureRequest changeTempuratureRequest) throws Exception {

        mqttService.publishToShadow(changeTempuratureRequest);
        return ResponseEntity.ok(new MessageResponse("Change Temperature Succeed"));
    }

    /**
     * Get the water usage ,device usage and water saved data
     *
     * @param deviceId device id
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/deviceUsageDetail")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Object> getDeviceDetail(@RequestParam("device_id") String deviceId) throws Exception {
        //TODO
        // read data from the database group by date

        return ResponseEntity.ok(new MessageResponse("User data avaliable"));
    }

    /**
     * Turn on shower, turn off and preheat control
     *
     * @param showerControlRequest
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/showerControl")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> ShowerControl (@RequestBody ShowerControlRequest showerControlRequest) throws Exception {
        return mqttService.ShowerControl(showerControlRequest);
    }

    /**
     * Check if a device is connected in AWS IoT
     *
     * @param deviceId The ID of the device to check
     * @return ResponseEntity indicating if the device is connected or not
     */
    @GetMapping("/connection-status")
    public ResponseEntity<String> getConnectionStatus(@RequestParam String deviceId) {
        try {
            String retainedMessage = mqttService.getRetainedMessage(deviceId);
            if (retainedMessage.startsWith("Error")) {
                return ResponseEntity.status(404).body(retainedMessage);
            }
            return ResponseEntity.ok(retainedMessage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving retained message: " + e.getMessage());
        }
    }

}
