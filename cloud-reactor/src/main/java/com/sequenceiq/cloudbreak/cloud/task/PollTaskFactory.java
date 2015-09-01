package com.sequenceiq.cloudbreak.cloud.task;


import java.util.List;

import com.sequenceiq.cloudbreak.cloud.BooleanStateConnector;
import com.sequenceiq.cloudbreak.cloud.event.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.event.instance.BooleanResult;
import com.sequenceiq.cloudbreak.cloud.event.instance.InstanceConsoleOutputResult;
import com.sequenceiq.cloudbreak.cloud.event.instance.InstancesStatusResult;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;

public interface PollTaskFactory {

    PollTask<ResourcesStatePollerResult> newPollResourcesStateTask(AuthenticatedContext authenticatedContext, List<CloudResource> cloudResource);

    PollTask<ResourcesStatePollerResult> newPollResourceTerminationTask(AuthenticatedContext authenticatedContext, List<CloudResource> cloudResource);

    PollTask<InstancesStatusResult> newPollInstanceStateTask(AuthenticatedContext authenticatedContext, List<CloudInstance> instances);

    PollTask<InstanceConsoleOutputResult> newPollInstanceConsoleOutputTask(AuthenticatedContext authenticatedContext, CloudInstance instance);

    PollTask<BooleanResult> newPollBooleanStateTask(AuthenticatedContext authenticatedContext, BooleanStateConnector connector);

    PollTask<BooleanResult> newPollBooleanTerminationTask(AuthenticatedContext authenticatedContext, BooleanStateConnector connector);
}