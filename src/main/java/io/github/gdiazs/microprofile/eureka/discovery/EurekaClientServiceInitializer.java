package io.github.gdiazs.microprofile.eureka.discovery;

import javax.enterprise.context.Destroyed;
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

@Dependent
public class EurekaClientServiceInitializer {

    private static ApplicationInfoManager applicationInfoManager;
    private static EurekaClient eurekaClient;


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
        ApplicationInfoManager applicationInfoManager = initializeApplicationInfoManager(new MyDataCenterInstanceConfig());
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
}
