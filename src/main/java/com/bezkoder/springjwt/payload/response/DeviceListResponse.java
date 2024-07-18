package com.bezkoder.springjwt.payload.response;

import com.bezkoder.springjwt.models.Device;
import lombok.Data;
import java.util.*;

@Data
public class DeviceListResponse {
    private List<Device> deviceList;

}
