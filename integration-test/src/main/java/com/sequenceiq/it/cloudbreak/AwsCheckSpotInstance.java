package com.sequenceiq.it.cloudbreak;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.sequenceiq.cloudbreak.api.endpoint.StackEndpoint;
import com.sequenceiq.cloudbreak.api.model.InstanceGroupResponse;
import com.sequenceiq.cloudbreak.api.model.InstanceMetaDataJson;
import com.sequenceiq.cloudbreak.api.model.StackResponse;
import com.sequenceiq.it.IntegrationTestContext;


public class AwsCheckSpotInstance extends AbstractCloudbreakIntegrationTest {

    @BeforeMethod
    public void setContextParams() {
        IntegrationTestContext itContext = getItContext();
        Assert.assertNotNull(itContext.getContextParam(CloudbreakITContextConstants.STACK_ID), "Stack id is mandatory.");
    }

    @Parameters({"region", "hostGroupToCheck", "checkBeforeDownscaling", "downscalingAdjustment"})
    @Test
    public void checkSpotInstance(@Optional Regions region, String hostGroupToCheck, @Optional ("false") Boolean checkBeforeDownscaling,
            @Optional Integer downscalingAdjustment) {
        //GIVEN
        IntegrationTestContext itContext = getItContext();
        String stackId = itContext.getContextParam(CloudbreakITContextConstants.STACK_ID);

        StackEndpoint stackEndpoint = getCloudbreakClient().stackEndpoint();
        StackResponse stackResponse = stackEndpoint.get(Long.valueOf(stackId));

        List<InstanceGroupResponse> instanceGroups = stackResponse.getInstanceGroups();

        List<String> instanceIdList = new ArrayList<>();
        List<String> hostGroupList = Arrays.asList(hostGroupToCheck.split(","));

        Map<String, List> instanceIdMap = new HashMap<>();
        List<String> list = new ArrayList<>();

        //WHEN
        for (InstanceGroupResponse instanceGroup : instanceGroups) {
            if (hostGroupList.contains(instanceGroup.getGroup().toString())) {
                Set<InstanceMetaDataJson> instanceMetaData = instanceGroup.getMetadata();
                for (InstanceMetaDataJson metaData : instanceMetaData) {
                    instanceIdList.add(metaData.getInstanceId());
                    list.add(metaData.getInstanceId());
                    instanceIdMap.put(instanceGroup.getGroup().toString(), list);
                }
            }
        }

        if (Boolean.TRUE.equals(checkBeforeDownscaling)) {
            itContext.putContextParam(CloudbreakITContextConstants.INSTANCE_COUNT, String.valueOf(instanceIdMap.get(hostGroupToCheck).size()));
        } else {
            Integer spotInstanceCount = 0;
            Assert.assertNotNull(region);
            AmazonEC2Client ec2 = new AmazonEC2Client();
            ec2.setRegion(Region.getRegion(region));
            DescribeSpotInstanceRequestsResult describeSpotInstanceRequestsResult = ec2.describeSpotInstanceRequests();
            List<SpotInstanceRequest> spotInstanceRequests = describeSpotInstanceRequestsResult.getSpotInstanceRequests();
            //THEN
            Assert.assertFalse(spotInstanceRequests.isEmpty());

            List<String> spotInstanceIdList = new ArrayList<>();

            for (SpotInstanceRequest request : spotInstanceRequests) {
                spotInstanceIdList.add(request.getInstanceId());
            }

            for (String id : instanceIdList) {
                Assert.assertTrue(spotInstanceIdList.contains(id));
                if (spotInstanceIdList.contains(id)) {
                    spotInstanceCount += 1;
                }
            }

            if (downscalingAdjustment != null) {
                Assert.assertNotNull(itContext.getContextParam(CloudbreakITContextConstants.INSTANCE_COUNT), "Instance counter is mandatory.");
                Integer instanceCountPrev = Integer.valueOf(itContext.getContextParam(CloudbreakITContextConstants.INSTANCE_COUNT));
                Assert.assertEquals(Integer.valueOf(instanceCountPrev - downscalingAdjustment), spotInstanceCount);
            }
        }
    }
}