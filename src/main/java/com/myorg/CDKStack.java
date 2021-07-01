package com.myorg;

import com.myorg.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        String region = Utils.getProperties().getProperty("aws.region");
        String ami = Utils.getProperties().getProperty("ec2.instance.ami");
        GenericLinuxImage genericLinuxImage = new GenericLinuxImage(Collections.singletonMap(region, ami));

        Instance instance = Instance.Builder.create(
                this,
                Utils.getProperties().getProperty("ec2.instance.name"))
                .instanceType(new InstanceType(Utils.getProperties().getProperty("ec2.instance.size")))
                .machineImage(genericLinuxImage)
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .securityGroup(securityGroup)
                .vpc(vpc)
                .keyName(Utils.getProperties().getProperty("ec2.keypair"))
                .userData(readFile())
                .build();
    }

    private UserData loadUserData() {
        ClassLoader classLoader = CDKStack.class.getClassLoader();
        File file = new File(classLoader.getResource("install-dependencies.txt").getFile());
        String data = null;
        try {
            data = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return UserData.custom(data);
    }

    private UserData readFile() {
        UserData userData = UserData.forLinux();

        try {
            ClassLoader classLoader = CDKStack.class.getClassLoader();
            File file = new File(classLoader.getResource("install-dependencies.txt").getFile());
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String commands;

            while ((commands = br.readLine()) != null) {
                sb.append(commands);
                userData.addCommands(commands);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userData;
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
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(22));
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
