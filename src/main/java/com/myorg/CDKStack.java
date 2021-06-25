package com.myorg;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.*;

import java.util.ArrayList;
import java.util.List;

public class CDKStack extends Stack {
    public CDKStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CDKStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create public subnet 
        SubnetConfiguration publicSubnet = SubnetConfiguration.builder()
                .name("public-subnet")
                .subnetType(SubnetType.PUBLIC)
                .cidrMask(24)
                .build();
        List<SubnetConfiguration> subnetList = new ArrayList<>();
        subnetList.add(publicSubnet);

        // Create VPC (and attach the above subnet)
        Vpc vpc = new Vpc(this, "vpc-from-ckd", VpcProps.builder()
                .cidr("10.0.0.0/16")
                .maxAzs(1)
                .subnetConfiguration(subnetList)
                .build());

        // Create the Security Group inside the VPC
        SecurityGroup securityGroup = new SecurityGroup(this, "sg-cdk-java", SecurityGroupProps.builder()
                .vpc(vpc)
                .allowAllOutbound(true)
                .build());

        // Create EC2 instance
        AmazonLinuxImage genericLinuxImage = new AmazonLinuxImage();
        Instance.Builder.create(this, "EC2 from CDK")
                .instanceType(new InstanceType("t2.micro"))
                .machineImage(genericLinuxImage)
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .securityGroup(securityGroup)
                .vpc(vpc)
                .build();
    }
}
