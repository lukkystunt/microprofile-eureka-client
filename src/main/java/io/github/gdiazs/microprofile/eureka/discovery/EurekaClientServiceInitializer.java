package io.github.gdiazs.microprofile.eureka.discovery;

import javax.enterprise.context.Destroyed;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;

import io.github.gdiazs.microprofile.eureka.util.ConfigurationUtil;
import io.github.gdiazs.microprofile.eureka.util.InetUtils;
import io.github.gdiazs.microprofile.eureka.util.InetUtilsProperties;

@Dependent
public class EurekaClientServiceInitializer {

    private static ApplicationInfoManager applicationInfoManager;
    private static EurekaClient eurekaClient;
    private static final String CONFIG_NAME = "eureka-client";


    private static synchronized ApplicationInfoManager initializeApplicationInfoManager(EurekaInstanceConfig instanceConfig) {
        if (applicationInfoManager == null) {
            InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
            applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
        }

        return applicationInfoManager;
    }

    private static synchronized EurekaClient initializeEurekaClient(ApplicationInfoManager applicationInfoManager, EurekaClientConfig clientConfig) {
        if (eurekaClient == null) {
            eurekaClient = new DiscoveryClient(applicationInfoManager, clientConfig);
        }

        return eurekaClient;
    }

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init){
        Properties properties = ConfigurationUtil.loadCascadedProperties(CONFIG_NAME);

        ApplicationInfoManager applicationInfoManager = initializeApplicationInfoManager(new WebAppInstanceConfig(properties));
        System.out.println(applicationInfoManager.getInfo().getInstanceId());
        eurekaClient = initializeEurekaClient(applicationInfoManager, new DefaultEurekaClientConfig());
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.STARTING);
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);


    }

    @Produces
    public EurekaClient eurekaClient(){
        return EurekaClientServiceInitializer.eurekaClient;

    }

    public void shutdownEurekaClient(@Observes @Destroyed(ApplicationScoped.class) Object event) {
         eurekaClient.shutdown();
    }

    private class WebAppInstanceConfig extends MyDataCenterInstanceConfig {

    private static final String HOST_NAME = "eureka.hostname";
    private Properties properties;


    public WebAppInstanceConfig(Properties properties) {
        this.properties = properties;
    }

//    public String getAppname() {
//        return "smatt-sample-service";
//    }
//
//    @Override
//    public String getStatusPageUrl() {
//        return "http://localhost:9005/actuator/info";
//    }
//
//    @Override
//    public String getHomePageUrl() {
//        return "http://localhost:9005/";
//    }
//
//    @Override
//    public String getHealthCheckUrl() {
//        return "http://localhost:9005/actuator/health";
//    }

    @Override
    public String getHostName(boolean refresh) {
        return properties.getProperty(HOST_NAME, "localhost");
    }

    @Override
    public String getInstanceId() {
        InetUtilsProperties target = new InetUtilsProperties();
        InetUtils utils = new InetUtils(target);
        InetUtils.HostInfo hostInfo = utils.findFirstNonLoopbackHostInfo();
        return hostInfo.getHostname() + ":" + getVirtualHostName() + ":" + getNonSecurePort();
    }

    }
}
