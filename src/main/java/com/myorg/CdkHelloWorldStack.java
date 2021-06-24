package com.myorg;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.model.Instance;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;

import java.util.ArrayList;
import java.util.List;

public class CdkHelloWorldStack extends Stack {
    public CdkHelloWorldStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkHelloWorldStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create the VPC
        SubnetConfiguration publicSubnet = SubnetConfiguration.builder()
                .name("public-subnet")
                .subnetType(SubnetType.PUBLIC).build();
        List<SubnetConfiguration> subnetList = new ArrayList<>();
        subnetList.add(publicSubnet);

        Vpc vpc = Vpc.Builder.create(this, "VPC")
                .cidr("10.0.0.0/16")
                .subnetConfiguration(subnetList)
                .build();

        // Create the Security Group inside the VPC
        SecurityGroup sg = new SecurityGroup(this, "sg-cdk-java", SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(true).build());

        System.out.printf("\n");
        System.out.printf(
                "Successfully created VPC named %s with subnet %s \n",
                vpc.getVpcId(), vpc.getPrivateSubnets());

        System.out.printf(
                "Successfully created security group with Id %s",
                sg.getSecurityGroupId());

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().build();
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest().clone()
                .withImageId("ami-0c1a7f89451184c8b").withInstanceType("t3a.large")
                .withMinCount(1)
                .withMaxCount(1).withNetworkInterfaces(new InstanceNetworkInterfaceSpecification()
                        .withAssociatePublicIpAddress(true)
                        .withDeviceIndex(0));

        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        Instance instance = runInstancesResult.getReservation().getInstances().get(0);
        String instanceId = instance.getInstanceId();
        System.out.println("EC2 Instance Id: " + instanceId);

        // Setting up the tags for the instance
        CreateTagsRequest createTagsRequest = new CreateTagsRequest()
                .withResources(instance.getInstanceId())
                .withTags(new Tag("Name", "Node created from CDK"));
        ec2.createTags(createTagsRequest);

        // Starting the Instance
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest().withInstanceIds(instanceId);

        ec2.startInstances(startInstancesRequest);
    }
}
