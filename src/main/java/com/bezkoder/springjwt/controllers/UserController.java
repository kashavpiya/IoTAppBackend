package com.bezkoder.springjwt.controllers;


import ch.qos.logback.classic.Logger;
import com.amazonaws.util.StringUtils;
import com.bezkoder.springjwt.models.*;
import com.bezkoder.springjwt.payload.request.AddDeviceRequest;
import com.bezkoder.springjwt.payload.request.SettingDeviceDataRequest;
import com.bezkoder.springjwt.payload.request.SignupRequest;
import com.bezkoder.springjwt.payload.response.DeviceListResponse;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.repository.*;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;
import com.bezkoder.springjwt.service.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.bezkoder.springjwt.payload.request.UserIdRequest;
import com.bezkoder.springjwt.payload.request.UserRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;


import java.util.*;
import java.util.stream.Collectors;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/api/iot/user")

/**
 * The Controller for the user to
 */
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ShowerEventRepository showerEventRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private DevicePinRepository devicePinRepository;

    /**
     * Add the device
     *
     * @param addDeviceRequest request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addDevice")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> addDevice(@RequestBody AddDeviceRequest addDeviceRequest) throws Exception {
        Integer userId = addDeviceRequest.getUserId();
        String username = extractAccessToken();

        if (StringUtils.isNullOrEmpty(addDeviceRequest.getDeviceId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Device id needed"));
        }
        if (StringUtils.isNullOrEmpty(addDeviceRequest.getDevice_name())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Device name needed"));
        }
        Device device = new Device();
        device.setDeviceId(addDeviceRequest.getDeviceId());
        device.setUuid(UUID.randomUUID().toString());
        device.setUserId(userId);
        device.setUsername(username);
        device.setValid(addDeviceRequest.isValid());
        device.setShared(addDeviceRequest.isShared());
        device.setOnboardingDate(new Date());
        device.setPin(addDeviceRequest.getPin());
        device.setDeviceName(addDeviceRequest.getDevice_name());

        DeviceToPin deviceToPin = devicePinRepository.findByDeviceId(addDeviceRequest.getDeviceId());
        if (deviceToPin == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: deviceId is invalid"));
        }
        if (!deviceToPin.getPin().equals(addDeviceRequest.getPin())) {
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
     * delete user account
     *
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/delete-user")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUser(@RequestBody UserIdRequest request)  throws Exception {
        Integer userId = request.getUserId();
        String username = extractAccessToken();

        System.out.println(username + userId);

        try {
            List<Device> userDevices = deviceRepository.findByUserId(userId);
            for (Device device : userDevices) {
                deviceRepository.delete(device);
            }

            userRepository.deleteById((long) userId);

            // Delete user from AWS Cognito using the v2 SDK
            CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
                    .region(Region.US_EAST_1)
                    .build();

            AdminDeleteUserRequest deleteUserRequest = AdminDeleteUserRequest.builder()
                    .userPoolId("us-east-1_cgPwKnX2P")
                    .username(username)
                    .build();

            AdminDeleteUserResponse deleteUserResponse = cognitoClient.adminDeleteUser(deleteUserRequest);

            return ResponseEntity.ok().body(new MessageResponse("User Removed!"));
        } catch (Exception e) {
           return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: can not delete the profile"+ e));
        }


        //will have to add deleting from aws cognito in the future
        //also not implemented on front end yet
    }

    /**
     * @param addDeviceRequest
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/updateDevice")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateDevice(@RequestBody AddDeviceRequest addDeviceRequest) throws Exception {
        Integer userId = addDeviceRequest.getUserId();

        if (StringUtils.isNullOrEmpty(addDeviceRequest.getDeviceId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Device id needed"));
        }
        if (StringUtils.isNullOrEmpty(addDeviceRequest.getDevice_name())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Device name needed"));
        }
        Device oldDevice = deviceRepository.findByDeviceId(addDeviceRequest.getDeviceId());
        if (Objects.isNull(oldDevice)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Device Id is missed or device is missing"));
        }

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
     * The api for sharing
     *
     * @param username
     * @param deviceId
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/shareDevice")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> shareDevice(@RequestParam String username, String deviceId, Integer userId) throws Exception {
        if (StringUtils.isNullOrEmpty(deviceId)) {
            return new ResponseEntity("Device id is need", (HttpStatusCode.valueOf(500)));
        }
        if (StringUtils.isNullOrEmpty(username)) {
            return new ResponseEntity("username is need", (HttpStatusCode.valueOf(500)));
        }
        Optional<User> target = userRepository.findByUsername(username);
        if (target.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: UserName you wanna share doesn't existed "));
        }


        Device deviceToShare = deviceRepository.findByDeviceId(deviceId);
        if (deviceToShare.getUserId().longValue() != userId) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: You don't have the access to share the device"));
        }

        UserDevice userDevice = new UserDevice();
        userDevice.setDeviceId(deviceId);
        userDevice.setUserName(username);
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
     * query by user
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/query")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<DeviceListResponse> queryDeviceByUserId(@RequestBody UserRequest request) {
        Integer userId = request.getUserId();

//        String username = extractAccessToken();
//        System.out.println(username);

        // Use userId from request object to query devices
        List<Device> deviceList = deviceRepository.findByUserId(userId);

//      List<Device> deviceList = deviceRepository.findByUsername(username);

        List<UserDevice> sharedDevice = userDeviceRepository.findByUserName(request.getUsername());
        for (UserDevice userDevice : sharedDevice) {
            Device device = deviceRepository.findByDeviceId(userDevice.getDeviceId());
            deviceList.add(device);
        }
        // Create and return response
        DeviceListResponse deviceListResponse = new DeviceListResponse();
        deviceListResponse.setDeviceList(deviceList);
        return ResponseEntity.ok(deviceListResponse);
    }

    @PostMapping("/queryAll")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<DeviceListResponse> queryDeviceByUserId() {

        String username = extractAccessToken();
        System.out.println(username);

        List<Device> deviceList = deviceRepository.findByUsername(username);

        List<UserDevice> sharedDevice = userDeviceRepository.findByUserName(username);
        for (UserDevice userDevice : sharedDevice) {
            Device device = deviceRepository.findByDeviceId(userDevice.getDeviceId());
            deviceList.add(device);
        }
        // Create and return response
        DeviceListResponse deviceListResponse = new DeviceListResponse();
        deviceListResponse.setDeviceList(deviceList);
        return ResponseEntity.ok(deviceListResponse);
    }

    private String extractAccessToken() {
        // Get the current request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // Extract the access token from the Authorization header
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.replace("Bearer ", "");

            // Validate the access token and extract username using AWS SDK v2
            try {
                CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();

                GetUserRequest getUserRequest = GetUserRequest.builder()
                        .accessToken(accessToken)
                        .build();

                GetUserResponse getUserResponse = cognitoClient.getUser(getUserRequest);
                return getUserResponse.username();
            } catch (NotAuthorizedException | UserNotFoundException | InvalidParameterException e) {
                throw new RuntimeException("Access token validation failed: " + e.getMessage());
            } catch (CognitoIdentityProviderException e) {
                throw new RuntimeException("AWS Cognito error: " + e.awsErrorDetails().errorMessage());
            }
        }
        throw new RuntimeException("Access token not found in request headers");
    }


    //this will have to change
    @PostMapping(value = "/change-info")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> changeInfo(@RequestBody SignupRequest signupRequest) {
        return userService.changeUserInfo(signupRequest);
    }

    @GetMapping("/querySaving")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Saving> getDB(Integer userId, String username) {
        Saving res = showerEventRepository.getSavingAllDetail(username, userId);
        if (res == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/queryNewSaving")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Saving> getDBa(Integer userId, String username) {
        Saving res = showerEventRepository.getNewSavingAllDetail(username, userId);
        if (res == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/queryDeviceSaving")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Saving> getDBb(String deviceId){
        Saving res = showerEventRepository.getDeviceSavingDetail(deviceId);
        if (res == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(res);

    }


    @GetMapping("/queryAllHistory")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<ShowerEvent>> getAllHistory(@RequestParam Integer userId, @RequestParam String username) {
        List<ShowerEvent> res = showerEventRepository.getHistoryAllDetail(username, userId);
        if (res == null || res.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(res);
    }


    @GetMapping("/queryIndividualHistory")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<CycleTimes>> getDeviceHistory(@RequestParam Integer userId, @RequestParam String username) {
        List<CycleTimes> res = showerEventRepository.getIndividualHistory(username, userId);
        if (res == null || res.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/queryHistory")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<ShowerEvent>> getHistory(@RequestParam String deviceId, @RequestParam Long time) {
        return ResponseEntity.ok(showerEventRepository.getShowerHistory(deviceId, time));
    }


    @GetMapping("/queryDeviceId")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Object> getHistory(@RequestParam String deviceId) {
        List<DeviceData> deviceData = showerEventRepository.getDeviceStatus(deviceId);
        if (deviceData == null || deviceData.size() == 0) {
            return ResponseEntity.ok(new MessageResponse("No device data found for this device"));
        }
        return ResponseEntity.ok(deviceData.get(0));
    }

    @PostMapping("/settingDeviceInfo")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> settingDeviceInfo(@RequestBody SettingDeviceDataRequest settingDeviceDataRequest) {
        String deviceId = settingDeviceDataRequest.getDevice_id();
        Integer userId = settingDeviceDataRequest.getUserId();
        String username = settingDeviceDataRequest.getUsername();

        List<Device> deviceList = deviceRepository.findByUserId((int) userId);

        List<UserDevice> sharedDevice = userDeviceRepository.findByUserName(username);
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

            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: System error"));
        }

        return ResponseEntity.ok().body(new MessageResponse("Change device settings successfully"));
    }

    @PostMapping(value = "/change-info-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> changeInfoAdmin( @RequestParam String pwd , @RequestBody SignupRequest signupRequest) {
        if(!pwd.equals("ASDFqwer123456.")){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: pwd not correct"));
        }
        return userService.changeUserInfoAdmin(signupRequest);
    }

    @PostMapping("/deleteDevice")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteDevice(@RequestParam String deviceId, Integer userId) throws Exception {
        System.out.println(deviceId);
        Device deviceToDelete = deviceRepository.findByDeviceId(deviceId);

        if (deviceToDelete == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Device not found"));
        }

        // Check if the user has permission to delete the device
        if (userId != deviceToDelete.getUserId()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: You don't have permission to delete this device"));
        }

        // Delete the device from the repository
        try {
            deviceRepository.delete(deviceToDelete);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Failed to delete device"));
        }

        // Remove shared device entries
        List<UserDevice> sharedDevices = userDeviceRepository.findByDeviceId(deviceId);
        for (UserDevice sharedDevice : sharedDevices) {
            userDeviceRepository.delete(sharedDevice);
        }

        return ResponseEntity.ok().body(new MessageResponse("Device deleted successfully"));
    }

    /**
     * API to get the device name by device ID.
     *
     * @param deviceId the device ID
     * @return ResponseEntity with the device name or an error message
     */
    @GetMapping(value = "/getDeviceName")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getDeviceName(@RequestParam String deviceId) {
        if (StringUtils.isNullOrEmpty(deviceId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Device ID is required"));
        }

        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Device not found"));
        }

        return ResponseEntity.ok(new MessageResponse(device.getDeviceName()));
    }
}
