package com.bezkoder.springjwt.repository;


import com.bezkoder.springjwt.models.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    List<UserDevice> findByUserName(String username);

    List<UserDevice> findByDeviceId(String device_id);
}
