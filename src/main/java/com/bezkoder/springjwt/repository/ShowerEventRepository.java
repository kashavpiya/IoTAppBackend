package com.bezkoder.springjwt.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.bezkoder.springjwt.models.*;
import lombok.extern.slf4j.Slf4j;
import org.h2.schema.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.*;

@Repository
@Slf4j
public class ShowerEventRepository {
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    private static double DegreeRaisedForHeatMax = 51;

    private static double costRaisedOneDegree = 0.0027;

    /**
     * Query shower event data for last week
     */
    private static List<Integer> queryLastWeek = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);

    /**
     * Query shower event data for last month
     */
    private static List<Integer> queryLastMonth = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30);

    /**
     * Query shower event data for last year
     */
    private static List<Integer> queryLastYear = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

    /**
     * date convert util
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

    private Map<String, Long> oldTotalTimes = new HashMap<>();
    private Map<String, Long> oldCycles = new HashMap<>();


    /**
     * Statistic for all the params like  KW, CO2 and water
     *
     * @param events
     * @return saving data model
     */
    private Saving statistic(List<ShowerEvent> events) {
        Saving saving = new Saving();
        double waterTotalSaved = 0l;
        double electricTotalSaved = 0l;
        double electricMoneySaved = 0l;
        double CO2Saved = 0l;

//        for (ShowerEvent event : events) {
//            if (event.getPayload().getShower_time() * 2.1 >= 28) {
//            } else {
//                waterTotalSaved += event.getPayload().getShower_time().longValue() * 2.1 - 28;
//            }
//
//            if (event.getPayload().getShower_time() <= 14) {
//                electricTotalSaved += (DegreeRaisedForHeatMax * costRaisedOneDegree) * event.getPayload().getShower_time() - 3.86;
//                electricMoneySaved += (DegreeRaisedForHeatMax * costRaisedOneDegree) * event.getPayload().getShower_time() * 0.3 - 1.16;
//                CO2Saved += ((double) (event.getPayload().getShower_time().doubleValue() - 14) / (double) 4) * 2.07;
//            }
//
//        }
        saving.setWaterSaved(waterTotalSaved);
        saving.setElectricitySaved(electricTotalSaved);
        saving.setElectricityCostSaved(electricMoneySaved);
        saving.setCO2Saved(CO2Saved);
        return saving;
    }

    private Saving statistic(DeviceData deviceData) {
        Saving saving = new Saving();
        double waterTotalSaved = 0l;
        double electricTotalSaved = 0l;
        double electricMoneySaved = 0l;
        double CO2Saved = 0l;

        Instant instant = Instant.ofEpochMilli(deviceData.getSample_time());
        LocalDateTime sampleDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        Date sampleDate = Date.from(instant);

        int totalShowerTotalMinutes = (int) (deviceData.getPayload().getTotalTime() / 60000000);
        if (totalShowerTotalMinutes * 2.1 >= 28) {
        } else {
            waterTotalSaved += (long) totalShowerTotalMinutes * 2.1 - 28;
        }
        if (totalShowerTotalMinutes <= 14) {
            electricTotalSaved += (DegreeRaisedForHeatMax * costRaisedOneDegree) * totalShowerTotalMinutes - 3.86;
            electricMoneySaved += (DegreeRaisedForHeatMax * costRaisedOneDegree) * totalShowerTotalMinutes * 0.3 - 1.16;
            CO2Saved += ((double) ((double) totalShowerTotalMinutes - 14) / (double) 4) * 2.07;
        }
        saving.setWaterSaved(waterTotalSaved);
        saving.setElectricitySaved(electricTotalSaved);
        saving.setElectricityCostSaved(electricMoneySaved);
        saving.setCO2Saved(CO2Saved);
        saving.setSampleDate(sampleDate);



        return saving;
    }

    private Saving statistic2(DeviceData deviceData) {
        Saving saving = new Saving();
        double waterTotalSaved = 0.0;
        double CO2Saved = 0.0;
        double coalSaved = 0.0;
        double electricSaved = 0.0;

        Instant instant = Instant.ofEpochMilli(deviceData.getSample_time());
        LocalDateTime sampleDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        Date sampleDate = Date.from(instant);

        int totalShowerTotalMinutes = (int) (deviceData.getPayload().getTotalTime() / 60);
        int totalCycles = (int) (deviceData.getPayload().getCycles());
        int totalMinsSaved = (totalCycles * 8) - totalShowerTotalMinutes;
        waterTotalSaved += totalMinsSaved * 2.1;
        System.out.print("water: ");
        System.out.println(waterTotalSaved);
        System.out.println(totalCycles);
        System.out.println(totalShowerTotalMinutes);
        coalSaved += totalMinsSaved * 0.25;
        CO2Saved += totalMinsSaved * 0.38;
        electricSaved += coalSaved * 0.88;

        saving.setWaterSaved(waterTotalSaved);
        saving.setCO2Saved(CO2Saved);
        saving.setCoalSaved(coalSaved);
        saving.setSampleDate(sampleDate);
        saving.setElectricitySaved(electricSaved);
        return saving;
    }

    public void updateShowerTime(String deviceId, long sampleTime, int showerTimeDifference, int numShowers) {
        ShowerEvent showerEvent = new ShowerEvent();
        showerEvent.setDevice_id(deviceId);
        showerEvent.setSample_time(sampleTime);
        showerEvent.setPayload(new HistoryPayload(showerTimeDifference, numShowers));

        dynamoDBMapper.save(showerEvent);
    }


    /**
     * Query device by user, get user info from DB
     *
     * @return
     */
    public Saving getSavingAllDetail(String username, Integer userId) {
        List<UserDevice> sharedDevice = userDeviceRepository.findByUserName(username);
        List<Device> deviceList = deviceRepository.findByUserId((int) userId);
        for (UserDevice userDevice : sharedDevice) {
            Device device = deviceRepository.findByDeviceId(userDevice.getDeviceId());
            deviceList.add(device);
        }

        if (deviceList.isEmpty()) {
            return null;
        }

        Saving totalResult = new Saving();

        for (Device device : deviceList) {
            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
            eav.put(":v1", new AttributeValue().withS(device.getDeviceId()));
            DynamoDBQueryExpression<DeviceData> queryExpression = new DynamoDBQueryExpression<DeviceData>().withKeyConditionExpression("device_id = :v1").withExpressionAttributeValues(eav);
            queryExpression.withScanIndexForward(false).withLimit(1);
            List<DeviceData> deviceDataHistory = dynamoDBMapper.queryPage(DeviceData.class, queryExpression).getResults();
            log.info("Event found " + String.valueOf(deviceDataHistory.size()));
            if (deviceDataHistory == null || deviceDataHistory.size() == 0) continue;
            Saving saving = statistic(deviceDataHistory.get(0));
            totalResult.setElectricityCostSaved(totalResult.getElectricityCostSaved() + saving.getElectricitySaved());
            totalResult.setElectricitySaved(totalResult.getElectricitySaved() + saving.getElectricitySaved());
            totalResult.setCO2Saved(totalResult.getCO2Saved() + saving.getCO2Saved());
            totalResult.setWaterSaved(totalResult.getWaterSaved() + saving.getWaterSaved());
            totalResult.setTotalCycles(totalResult.getTotalCycles()+ deviceDataHistory.get(0).getPayload().getCycles());
            totalResult.setTotalTime(totalResult.getTotalTime()+ deviceDataHistory.get(0).getPayload().getTotalTime());
            totalResult.setSampleDate(saving.getSampleDate());

        }

        return totalResult;
    }

    public Saving getNewSavingAllDetail(String username, Integer userId) {
        List<UserDevice> sharedDevice = userDeviceRepository.findByUserName(username);
        List<Device> deviceList = deviceRepository.findByUserId((int) userId);
        for (UserDevice userDevice : sharedDevice) {
            Device device = deviceRepository.findByDeviceId(userDevice.getDeviceId());
            deviceList.add(device);
        }

        if (deviceList.isEmpty()) {
            return null;
        }

        Saving totalResult = new Saving();

        for (Device device : deviceList) {
            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
            eav.put(":v1", new AttributeValue().withS(device.getDeviceId()));
            DynamoDBQueryExpression<DeviceData> queryExpression = new DynamoDBQueryExpression<DeviceData>().withKeyConditionExpression("device_id = :v1").withExpressionAttributeValues(eav);
            queryExpression.withScanIndexForward(false).withLimit(1);
            List<DeviceData> deviceDataHistory = dynamoDBMapper.queryPage(DeviceData.class, queryExpression).getResults();
            log.info("Event found " + String.valueOf(deviceDataHistory.size()));
            if (deviceDataHistory == null || deviceDataHistory.size() == 0) continue;
            Saving saving = statistic2(deviceDataHistory.get(0));
            totalResult.setElectricityCostSaved(totalResult.getElectricityCostSaved() + saving.getElectricitySaved());
            totalResult.setElectricitySaved(totalResult.getElectricitySaved() + saving.getElectricitySaved());
            totalResult.setCO2Saved(totalResult.getCO2Saved() + saving.getCO2Saved());
            totalResult.setCoalSaved(totalResult.getCoalSaved() + saving.getCoalSaved());
            totalResult.setWaterSaved(totalResult.getWaterSaved() + saving.getWaterSaved());
            totalResult.setTotalCycles(totalResult.getTotalCycles()+ deviceDataHistory.get(0).getPayload().getCycles());
            totalResult.setTotalTime(totalResult.getTotalTime()+ deviceDataHistory.get(0).getPayload().getTotalTime());
            totalResult.setSampleDate(saving.getSampleDate());

        }

        return totalResult;
    }

    public Saving getDeviceSavingDetail(String deviceId) {
        Saving totalResult = new Saving();

        Device device = deviceRepository.findByDeviceId(deviceId);

        if (device == null) {
            // Handle case where device is not found
            return totalResult; // or throw an exception, log an error, etc.
        }

        // Proceed with querying device data and calculating totals
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(device.getDeviceId()));
        DynamoDBQueryExpression<DeviceData> queryExpression = new DynamoDBQueryExpression<DeviceData>()
                .withKeyConditionExpression("device_id = :v1")
                .withExpressionAttributeValues(eav)
                .withScanIndexForward(false)
                .withLimit(1);
        List<DeviceData> deviceDataHistory = dynamoDBMapper.queryPage(DeviceData.class, queryExpression).getResults();
        log.info("Event found " + deviceDataHistory.size());

        if (!deviceDataHistory.isEmpty()) {
            Saving saving = statistic2(deviceDataHistory.get(0));
            totalResult.setElectricityCostSaved(totalResult.getElectricityCostSaved() + saving.getElectricitySaved());
            totalResult.setElectricitySaved(totalResult.getElectricitySaved() + saving.getElectricitySaved());
            totalResult.setCO2Saved(totalResult.getCO2Saved() + saving.getCO2Saved());
            totalResult.setCoalSaved(totalResult.getCoalSaved() + saving.getCoalSaved());
            totalResult.setWaterSaved(totalResult.getWaterSaved() + saving.getWaterSaved());
            totalResult.setTotalCycles(totalResult.getTotalCycles() + deviceDataHistory.get(0).getPayload().getCycles());
            totalResult.setTotalTime(totalResult.getTotalTime() + deviceDataHistory.get(0).getPayload().getTotalTime());
            totalResult.setSampleDate(saving.getSampleDate());
        }

        return totalResult;
    }

    private List<Saving> getLastWeekEvents(String device_id) {
        List<Integer> lastWeek = queryLastWeek;
        List<Saving> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < lastWeek.size() - 1; i++) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.DATE, cal.get(Calendar.DATE) - lastWeek.get(i));
            long curDay = cal.getTimeInMillis();
            log.info(String.valueOf(curDay));
            cal.setTime(new Date());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.DATE, cal.get(Calendar.DATE) - lastWeek.get(i + 1));
            long preDay = cal.getTimeInMillis();
            log.info(String.valueOf(preDay));
            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();

            eav.put(":v1", new AttributeValue().withS(device_id));
            eav.put(":v2", new AttributeValue().withN(String.valueOf(preDay)));
            eav.put(":v3", new AttributeValue().withN(String.valueOf(curDay)));
            DynamoDBQueryExpression<ShowerEvent> queryExpression = new DynamoDBQueryExpression<ShowerEvent>().withKeyConditionExpression("device_id = :v1 and sample_time BETWEEN :v2 AND :v3").withExpressionAttributeValues(eav);
            List<ShowerEvent> showerEvents = dynamoDBMapper.query(ShowerEvent.class, queryExpression);
            log.info("Event found " + String.valueOf(showerEvents.size()));
            Saving saving = statistic(showerEvents);

            result.add(saving);
        }
        return result;
    }

    private List<Saving> getLastMonthEvents(String device_id) {
        List<Integer> lastMonth = queryLastMonth;
        List<Saving> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < lastMonth.size() - 1; i++) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.DATE, cal.get(Calendar.DATE) - lastMonth.get(i));
            long curDay = cal.getTimeInMillis();
            log.info(String.valueOf(curDay));
            cal.set(Calendar.DATE, cal.get(Calendar.DATE) - lastMonth.get(i + 1));
            long preDay = cal.getTimeInMillis();
            log.info(String.valueOf(preDay));
            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();

            eav.put(":v1", new AttributeValue().withS(device_id));
            eav.put(":v2", new AttributeValue().withN(String.valueOf(preDay)));
            eav.put(":v3", new AttributeValue().withN(String.valueOf(curDay)));
            DynamoDBQueryExpression<ShowerEvent> queryExpression = new DynamoDBQueryExpression<ShowerEvent>().withKeyConditionExpression("device_id = :v1 and sample_time BETWEEN :v2 AND :v3").withExpressionAttributeValues(eav);
            List<ShowerEvent> showerEvents = dynamoDBMapper.query(ShowerEvent.class, queryExpression);
            log.info("Event found " + String.valueOf(showerEvents.size()));
            Saving saving = statistic(showerEvents);

            result.add(saving);
        }
        return result;

    }

    private List<Saving> getLastYearEvents(String device_id) {
        List<Integer> lastYear = queryLastYear;
        List<Saving> result = new ArrayList<>();

        for (int i = 0; i < lastYear.size() - 1; i++) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.DATE, cal.get(Calendar.DATE) - (lastYear.get(i) * 30));
            long curDay = cal.getTimeInMillis();
            log.info(String.valueOf(curDay));
            cal.setTime(new Date());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.DATE, cal.get(Calendar.DATE) - (lastYear.get(i + 1) * 30));
            long preDay = cal.getTimeInMillis();
            log.info(String.valueOf(preDay));
            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();

            eav.put(":v1", new AttributeValue().withS(device_id));
            eav.put(":v2", new AttributeValue().withN(String.valueOf(preDay)));
            eav.put(":v3", new AttributeValue().withN(String.valueOf(curDay)));
            DynamoDBQueryExpression<ShowerEvent> queryExpression = new DynamoDBQueryExpression<ShowerEvent>().withKeyConditionExpression("device_id = :v1 and sample_time BETWEEN :v2 AND :v3").withExpressionAttributeValues(eav);
            List<ShowerEvent> showerEvents = dynamoDBMapper.query(ShowerEvent.class, queryExpression);
            log.info("Event found " + String.valueOf(showerEvents.size()));
            Saving saving = statistic(showerEvents);

            result.add(saving);
        }
        return result;
    }

    public List<ShowerEvent> getShowerHistory(String device_id, Long timeStamp) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(device_id));

        // Remove limit to get all results
        DynamoDBQueryExpression<ShowerEvent> queryExpression = new DynamoDBQueryExpression<ShowerEvent>()
                .withKeyConditionExpression("device_id = :v1")
                .withExpressionAttributeValues(eav);

        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setS(device_id);
        AttributeValue attributeValue2 = new AttributeValue();
        attributeValue2.setN(String.valueOf(timeStamp));
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("device_id", attributeValue);
        map.put("sample_time", attributeValue2);
        queryExpression.setExclusiveStartKey(map);

        // Query all pages to get all results
        List<ShowerEvent> allResults = new ArrayList<>();
        QueryResultPage<ShowerEvent> showerEventsPage;
        do {
            showerEventsPage = dynamoDBMapper.queryPage(ShowerEvent.class, queryExpression);
            allResults.addAll(showerEventsPage.getResults());
            queryExpression.setExclusiveStartKey(showerEventsPage.getLastEvaluatedKey());
        } while (showerEventsPage.getLastEvaluatedKey() != null);

        return allResults;
    }

    public List<ShowerEvent> getHistoryAllDetail(String username, Integer userId) {
        List<UserDevice> sharedDevice = userDeviceRepository.findByUserName(username);
        List<Device> deviceList = deviceRepository.findByUserId((int) userId);

        for (UserDevice userDevice : sharedDevice) {
            Device device = deviceRepository.findByDeviceId(userDevice.getDeviceId());
            deviceList.add(device);
        }

        if (deviceList.isEmpty()) {
            return null;
        }

        List<ShowerEvent> totalResult = new ArrayList<>();

        for (Device device : deviceList) {
            String deviceId = device.getDeviceId();

            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":v1", new AttributeValue().withS(deviceId));

            DynamoDBQueryExpression<ShowerEvent> queryExpression = new DynamoDBQueryExpression<ShowerEvent>()
                    .withKeyConditionExpression("device_id = :v1")
                    .withExpressionAttributeValues(eav);

            List<ShowerEvent> allResults = new ArrayList<>();
            QueryResultPage<ShowerEvent> showerEventsPage;

            do {
                showerEventsPage = dynamoDBMapper.queryPage(ShowerEvent.class, queryExpression);
                allResults.addAll(showerEventsPage.getResults());
                queryExpression.setExclusiveStartKey(showerEventsPage.getLastEvaluatedKey());
            } while (showerEventsPage.getLastEvaluatedKey() != null);

            totalResult.addAll(allResults);
        }

        return totalResult;
    }


    public List<CycleTimes> getIndividualHistory(String username, Integer userId) {
       // Retrieve shared devices and user-specific devices
        List<UserDevice> sharedDevices = userDeviceRepository.findByUserName(username);
        List<Device> deviceList = deviceRepository.findByUserId(userId);

        // Add shared devices to the deviceList
        for (UserDevice userDevice : sharedDevices) {
            Device device = deviceRepository.findByDeviceId(userDevice.getDeviceId());
            if (device != null && !deviceList.contains(device)) {
                deviceList.add(device);
            }
        }

        // Initialize result list
        List<CycleTimes> totalResult = new ArrayList<>();

        // Query DynamoDB for each device
        for (Device device : deviceList) {
            String deviceId = device.getDeviceId();

            // Setup query with the correct attribute name
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":v1", new AttributeValue().withS(deviceId));

            DynamoDBQueryExpression<CycleTimes> queryExpression = new DynamoDBQueryExpression<CycleTimes>()
                    .withKeyConditionExpression("DeviceID = :v1")  // Adjusted attribute name
                    .withExpressionAttributeValues(eav);

            // Perform query and handle pagination
            List<CycleTimes> allResults = new ArrayList<>();
            QueryResultPage<CycleTimes> cycleTimesPage;

            do {
                cycleTimesPage = dynamoDBMapper.queryPage(CycleTimes.class, queryExpression);
                allResults.addAll(cycleTimesPage.getResults());
                queryExpression.setExclusiveStartKey(cycleTimesPage.getLastEvaluatedKey());
            } while (cycleTimesPage.getLastEvaluatedKey() != null);

            // Add results to totalResult
            totalResult.addAll(allResults);
        }

        // Return results, avoiding null
        return totalResult.isEmpty() ? Collections.emptyList() : totalResult;
    }


    /**
     * Query status
     *
     * @param device_id
     * @return
     */
    public List<DeviceData> getDeviceStatus(String device_id) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":v1", new AttributeValue().withS(device_id));
        DynamoDBQueryExpression<DeviceData> queryExpression = new DynamoDBQueryExpression<DeviceData>().withKeyConditionExpression("device_id = :v1").withExpressionAttributeValues(eav);
        queryExpression.withScanIndexForward(false).withLimit(1);
        List<DeviceData> deviceDataHistory = dynamoDBMapper.queryPage(DeviceData.class, queryExpression).getResults();
        log.info("Event found " + String.valueOf(deviceDataHistory.size()));


        System.out.println(deviceDataHistory);
        return deviceDataHistory;
    }

    public List<DeviceData> getDeviceStatus1(String device_id) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":v1", new AttributeValue().withS(device_id));
        DynamoDBQueryExpression<DeviceData> queryExpression = new DynamoDBQueryExpression<DeviceData>().withKeyConditionExpression("device_id = :v1").withExpressionAttributeValues(eav);
        queryExpression.withScanIndexForward(false).withLimit(1);
        List<DeviceData> deviceDataHistory = dynamoDBMapper.queryPage(DeviceData.class, queryExpression).getResults();
        log.info("Event found " + String.valueOf(deviceDataHistory.size()));

        processDeviceDataHistory(deviceDataHistory);
        System.out.println(deviceDataHistory);
        return deviceDataHistory;
    }

    public void processDeviceDataHistory(List<DeviceData> deviceDataHistory) {
        for (DeviceData deviceData : deviceDataHistory) {
            String device_id = deviceData.getDevice_id();
            System.out.println(device_id);
            long newCycles = deviceData.getPayload().getCycles();
            System.out.println(deviceData.getPayload());
            long newTotalTime = deviceData.getPayload().getTotalTime();

            // Check if the oldTotalTimes map contains the device_id
            if (oldTotalTimes.containsKey(device_id)) {
                long oldTotalTime = oldTotalTimes.get(device_id);
                System.out.println("it exists");

                if ((newCycles != oldCycles.getOrDefault(device_id, 0L))) {

                    System.out.println("NEW" + newTotalTime);
                    System.out.println("OLD" + oldTotalTime);
                    System.out.print("oldCy" + oldCycles.getOrDefault(device_id, 0L));
                    System.out.println("NEWcy" + newCycles);
                    int showerTimeDifference = (int) (newTotalTime - oldTotalTime);
                    int numCycles = (int) (newCycles - oldCycles.getOrDefault(device_id, 0L));
                    System.out.println("DIFF" + showerTimeDifference);

                    // Check if the shower time difference is 0 or negative
                    if (showerTimeDifference < 0) {
                        System.out.println("Shower time difference is 0 or negative. Skipping update.");
                        continue;
                    }

                    // Update the shower time in DynamoDB
                    updateShowerTime(device_id, deviceData.getSample_time(), showerTimeDifference, numCycles);

                    oldTotalTimes.put(device_id, newTotalTime);
                    oldCycles.put(device_id, newCycles);
                }
            } else {
                // If the device_id is not in oldTotalTimes map, add it with the current values
                oldTotalTimes.put(device_id, deviceData.getPayload().getTotalTime());
                oldCycles.put(device_id, newCycles);
                updateShowerTime(device_id, (Long) deviceData.getSample_time(), (int) newTotalTime, (int) newCycles);
            }
        }
    }

}
