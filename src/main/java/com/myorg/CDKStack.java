package com.myorg;

import com.myorg.utils.Utils;
import org.jetbrains.annotations.NotNull;
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

        List<SubnetConfiguration> subnetList = new ArrayList<>();
        subnetList.add(createSubnetConfiguration());

        Vpc vpc = createVpc(subnetList);

        SecurityGroup securityGroup = createSecurityGroup(vpc);

        createEC2instance(vpc, securityGroup);
    }

    private void createEC2instance(Vpc vpc, SecurityGroup securityGroup) {
        AmazonLinuxImage genericLinuxImage = new AmazonLinuxImage();
        Instance.Builder.create(
                this,
                Utils.getProperties().getProperty("ec2.instance.name"))
                .instanceType(new InstanceType(Utils.getProperties().getProperty("ec2.instance.size")))
                .machineImage(genericLinuxImage)
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .securityGroup(securityGroup)
                .vpc(vpc)
                .build();
    }

    @NotNull
    private SecurityGroup createSecurityGroup(Vpc vpc) {
        SecurityGroup securityGroup = new SecurityGroup(
                this,
                Utils.getProperties().getProperty("ec2.sg.name"),
                SecurityGroupProps.builder()
                        .vpc(vpc)
                        .allowAllOutbound(true)
                        .build());
        return securityGroup;
    }

    @NotNull
    private Vpc createVpc(List<SubnetConfiguration> subnetList) {
        Vpc vpc = new Vpc(
                this,
                Utils.getProperties().getProperty("ec2.vpc.name"),
                VpcProps.builder()
                        .cidr(Utils.getProperties().getProperty("ec2.vpc.cidr"))
                        .maxAzs(Integer.parseInt(Utils.getProperties().getProperty("ec2.vpc.maxazs")))
                        .subnetConfiguration(subnetList)
                        .build());
        return vpc;
    }

    @NotNull
    private SubnetConfiguration createSubnetConfiguration() {
        SubnetConfiguration publicSubnet = SubnetConfiguration.builder()
                .name(Utils.getProperties().getProperty("ec2.subnet.name"))
                .subnetType(SubnetType.PUBLIC)
                .cidrMask(Integer.parseInt(Utils.getProperties().getProperty("ec2.subnet.cidrmask")))
                .build();
        return publicSubnet;
    }
}
