package com.sequenceiq.cloudbreak.common.type;

public enum HostMetadataState {
    CONTAINER_RUNNING,
    HEALTHY,
    UNHEALTHY,
    WAITING_FOR_REPAIR;
}
