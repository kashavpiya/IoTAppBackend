package com.bezkoder.springjwt.repository;

import com.bezkoder.springjwt.models.Device;
import com.bezkoder.springjwt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repo for device add. update or delete, query
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Device findByDeviceId(String deviceId);

    List<Device> findByUsername(String username);

    List<Device> findByUserId(int userId);
}
