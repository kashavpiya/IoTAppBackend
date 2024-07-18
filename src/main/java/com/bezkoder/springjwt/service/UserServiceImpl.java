package com.bezkoder.springjwt.service;

import com.bezkoder.springjwt.models.Device;
import com.bezkoder.springjwt.models.DeviceToPin;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.models.UserDevice;
import com.bezkoder.springjwt.payload.request.AddDeviceRequest;
import com.bezkoder.springjwt.payload.request.SettingDeviceDataRequest;
import com.bezkoder.springjwt.payload.request.SignupRequest;
import com.bezkoder.springjwt.payload.response.DeviceListResponse;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.repository.DevicePinRepository;
import com.bezkoder.springjwt.repository.DeviceRepository;
import com.bezkoder.springjwt.repository.UserDeviceRepository;
import com.bezkoder.springjwt.repository.UserRepository;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.jwt.JwtHelper;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private DevicePinRepository devicePinRepository;

    @Autowired
    PasswordEncoder encoder;

    /**
     * Add device
     *
     * @param addDeviceRequest
     * @return
     */
    public ResponseEntity<MessageResponse> addDevice(AddDeviceRequest addDeviceRequest) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId = currentUser.getId();
        Device device = new Device();
        device.setDeviceId(addDeviceRequest.getDeviceId());
        device.setUuid(UUID.randomUUID().toString());
        device.setUserId((int) userId);
        device.setValid(addDeviceRequest.isValid());
        device.setShared(addDeviceRequest.isShared());
        device.setOnboardingDate(new Date());
        device.setPin(addDeviceRequest.getPin());
        device.setDeviceName(addDeviceRequest.getDevice_name());

        DeviceToPin deviceToPin =devicePinRepository.findByDeviceId(addDeviceRequest.getDeviceId());
        if(deviceToPin ==null){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: deviceId is invalid"));
        }
        if(!deviceToPin.getPin().equals(addDeviceRequest.getPin())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Pin and deviceId not matched"));
        }
        try {
            deviceRepository.save(device);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: DB Error"));
        }
        return ResponseEntity.ok().body(new MessageResponse("Add Device successfully"));
    }

    /**
     * update Device
     *
     * @param addDeviceRequest
     * @return
     */
    public ResponseEntity<MessageResponse> updateDevice(AddDeviceRequest addDeviceRequest) {

        Device oldDevice = deviceRepository.findByDeviceId(addDeviceRequest.getDeviceId());
        if (Objects.isNull(oldDevice)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Device Id is missed or device is missing"));
        }
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId = currentUser.getId();
        if (userId != oldDevice.getUserId()) {
            return ResponseEntity.badRequest().body(new MessageResponse("You don't have the access to update the device info"));
        }
        oldDevice.setShared(addDeviceRequest.isShared());
        oldDevice.setValid(addDeviceRequest.isValid());
        oldDevice.setUserId((int) userId);
        oldDevice.setDeviceName(addDeviceRequest.getDevice_name());
        try {
            deviceRepository.save(oldDevice);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: DB Error when update"));
        }
        return ResponseEntity.ok().body(new MessageResponse("Update Device successfully"));
    }


    /**
     * Query device by user, get user info from contet
     *
     * @return
     */
    public ResponseEntity<DeviceListResponse> queryDeviceByUser() {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId = currentUser.getId();
        List<Device> deviceList = deviceRepository.findByUserId((int) userId);

        List<UserDevice> sharedDevice = userDeviceRepository.findByUserName(currentUser.getUsername());
        for (UserDevice userDevice : sharedDevice) {
            Device device = deviceRepository.findByDeviceId(userDevice.getDeviceId());
            deviceList.add(device);
        }
        DeviceListResponse deviceListResponse = new DeviceListResponse();
        deviceListResponse.setDeviceList(deviceList);
        return ResponseEntity.ok(deviceListResponse);
    }




    public ResponseEntity<MessageResponse> shareDevice(String userName, String deviceId) {
        Optional<User> target = userRepository.findByUsername(userName);
        if (target.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: UserName you wanna share doesn't existed "));
        }

        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId = currentUser.getId();
        Device deviceToShare = deviceRepository.findByDeviceId(deviceId);
        if (deviceToShare.getUserId().longValue() != userId) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: You don't have the access to share the device"));
        }

        UserDevice userDevice = new UserDevice();
        userDevice.setDeviceId(deviceId);
        userDevice.setUserName(userName);
        userDevice.setCreateDate(new Date());
        userDevice.setUpdateDate(new Date());
        try {
            userDeviceRepository.save(userDevice);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Saving DB failed"));
        }
        return ResponseEntity
                .ok(new MessageResponse("Shared device successfully"));
    }

    /**
     * Updating user Info
     *
     * @param signupRequest
     * @return
     */
    public ResponseEntity<MessageResponse> changeUserInfo(SignupRequest signupRequest) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId = currentUser.getId();

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User doesn't exist in the database "));
        }
        String email = signupRequest.getEmail();
        String Password = signupRequest.getPassword();
        User curUser = user.get();

        curUser.setEmail(email);
        curUser.setPassword(encoder.encode(Password));
        try {
            userRepository.save(curUser);
        } catch (Exception e) {
            log.error("Error updating database {}", curUser.toString());
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User doesn't exist in the database "));
        }
        return ResponseEntity.ok().body(new MessageResponse("Update User Info successfully"));
    }


    /**
     * delete user profile
     *
     * @return
     */
    public ResponseEntity<MessageResponse> deleteUserInfo() {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId = currentUser.getId();
        try {
            userRepository.deleteById(userId);
            log.warn("Deleted user account {}", userId);
        } catch (Exception e) {
            log.error("Error when deleting user account {}", userId);
            ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: can not delete the profile"));
        }
        return ResponseEntity.ok().body(new MessageResponse("Delete User Info successfully"));
    }

    public ResponseEntity<MessageResponse> settingDeviceInfo(SettingDeviceDataRequest settingDeviceDataRequest) {
        String deviceId = settingDeviceDataRequest.getDevice_id();
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId = currentUser.getId();
        List<Device> deviceList = deviceRepository.findByUserId((int) userId);

        List<UserDevice> sharedDevice = userDeviceRepository.findByUserName(currentUser.getUsername());
        for (UserDevice userDevice : sharedDevice) {
            Device device = deviceRepository.findByDeviceId(userDevice.getDeviceId());
            deviceList.add(device);
        }
        Set<String> deivceIdSet = deviceList.stream().map(Device::getDeviceId).collect(Collectors.toSet());
        if (!deivceIdSet.contains(deviceId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User doesn't have the permission to change the settings for this device"));
        }

        Device device =deviceRepository.findByDeviceId(deviceId);
        device.setIdlet(settingDeviceDataRequest.getIdlet());
        device.setTemp(settingDeviceDataRequest.getTemp());
        device.setMaxwater(settingDeviceDataRequest.getMaxwater());
        device.setMode(settingDeviceDataRequest.getMode());
        device.setRg(settingDeviceDataRequest.getRg());
        try{
            deviceRepository.save(device);
        }catch (Exception e){
            log.error(e.toString());
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: System error"));
        }
        log.info("changing device settings {},{},{},{}" ,settingDeviceDataRequest.getDevice_id(),settingDeviceDataRequest.getTemp(),settingDeviceDataRequest.getIdlet(), settingDeviceDataRequest.getMaxwater());
        return ResponseEntity.ok().body(new MessageResponse("Change device settings successfully"));
    }

    /**
     * Updating user Info for admin
     *
     * @param signupRequest
     * @return
     */
    public ResponseEntity<MessageResponse> changeUserInfoAdmin(SignupRequest signupRequest) {
        Optional<User> user = userRepository.findByUsername(signupRequest.getUsername());
        if (user.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User doesn't exist in the database "));
        }
        String email = signupRequest.getEmail();
        String Password = signupRequest.getPassword();
        User curUser = user.get();

        curUser.setEmail(email);
        curUser.setPassword(encoder.encode(Password));
        try {
            userRepository.save(curUser);
        } catch (Exception e) {
            log.error("Error updating database {}", curUser.toString());
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User doesn't exist in the database "));
        }
        return ResponseEntity.ok().body(new MessageResponse("Update User Info successfully"));
    }
}
