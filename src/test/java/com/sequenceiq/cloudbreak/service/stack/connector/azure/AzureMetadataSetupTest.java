package com.sequenceiq.cloudbreak.service.stack.connector.azure;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sequenceiq.cloud.azure.client.AzureClient;
import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.domain.Credential;
import com.sequenceiq.cloudbreak.domain.Resource;
import com.sequenceiq.cloudbreak.domain.ResourceType;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.Template;
import com.sequenceiq.cloudbreak.service.ServiceTestUtils;

import reactor.core.Reactor;

public class AzureMetadataSetupTest {

    private static final String DUMMY_VM_NAME = "dummyVmName";

    @InjectMocks
    @Spy
    private AzureMetadataSetup underTest;

    @Mock
    private Reactor reactor;

    @Mock
    private AzureStackUtil azureStackUtil;

    @Mock
    private AzureClient azureClient;

    private Stack stack;

    private Credential credential;

    private Template template;

    @Before
    public void setUp() {
        underTest = new AzureMetadataSetup();
        MockitoAnnotations.initMocks(this);
        credential = ServiceTestUtils.createCredential(CloudPlatform.AZURE);
        template = ServiceTestUtils.createTemplate(CloudPlatform.AZURE);
        stack = ServiceTestUtils.createStack(template, credential, getDefaultResourceSet());
    }

    public Set<Resource> getDefaultResourceSet() {
        Set<Resource> resources = new HashSet<>();
        resources.add(new Resource(ResourceType.AZURE_CLOUD_SERVICE, DUMMY_VM_NAME, stack));
        resources.add(new Resource(ResourceType.AZURE_VIRTUAL_MACHINE, DUMMY_VM_NAME, stack));
        return resources;
    }

    @Test
    public void testSetupMetadataWhenIOExceptionOccurs() throws IOException {
        // GIVEN
        given(azureStackUtil.createAzureClient(any(Credential.class), anyString())).willReturn(azureClient);
        doThrow(new IOException()).when(underTest).getPrivateIP(anyString());
        // WHEN
        underTest.setupMetadata(stack);
        // THEN
        verify(underTest, times(0)).getVirtualIP(anyString());
    }

}