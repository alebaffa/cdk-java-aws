package com.myorg;

import com.myorg.utils.Utils;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;

import java.util.Properties;

public class CDKApp {
    public static void main(final String[] args) {
        App app = new App();

        Properties properties = Utils.getProperties();

        new CDKStack(app, properties.getProperty("cdk.stack.name"), StackProps.builder()
                // If you don't specify 'env', this stack will be environment-agnostic.
                // Account/Region-dependent features and context lookups will not work,
                // but a single synthesized template can be deployed anywhere.
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(properties.getProperty("aws.region"))
                        .build())

                .build());

        app.synth();
    }
}
