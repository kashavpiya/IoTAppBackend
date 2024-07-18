package com.bezkoder.springjwt;

import com.bezkoder.springjwt.models.Device;
import com.bezkoder.springjwt.repository.DeviceRepository;
import com.bezkoder.springjwt.repository.ShowerEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeviceStatusScheduler {

    @Autowired
    private ShowerEventRepository showerEventRepository;

    @Autowired
    private DeviceRepository deviceRepository;  // Assuming you have a repository to get all devices

    @Scheduled(fixedRate = 86400000)  // 24 hours in milliseconds
    public void runDeviceStatusCheck() {
        List<Device> devices = deviceRepository.findAll();  // Retrieve all devices
        for (Device device : devices) {
            showerEventRepository.getDeviceStatus1(device.getDeviceId());
        }
    }
}