package com.bezkoder.springjwt.repository;

import com.bezkoder.springjwt.models.DeviceToPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DevicePinRepository extends JpaRepository<DeviceToPin, String> {
    DeviceToPin findByDeviceId(String deviceId);
}
