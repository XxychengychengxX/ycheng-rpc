import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;

import java.util.Properties;

/**
 * @author Valar Morghulis
 * @Date 2023/10/6
 */
public class NacosListnerTest {


    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, "8.130.12.102:8848");
        properties.put(PropertyKeyConst.USERNAME, "nacos");
        properties.put(PropertyKeyConst.PASSWORD, "nacos");

        try {
            NamingService namingService = NamingFactory.createNamingService(properties);
            namingService.subscribe("com.ychengycheng.service.GreetingService", new MyNacosListener());
            Thread.sleep(100000);
        } catch (NacosException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static class MyNacosListener implements EventListener {
        /**
         * callback event.
         *
         * @param event event
         */
        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent) {
                System.out.println("12312u38239214376937suhd........................");
                System.out.println("12312u38239214376937suhd........................");
                System.out.println("12312u38239214376937suhd........................");
                System.out.println(
                        "((NamingEvent) event).getServiceName() = " + ((NamingEvent) event).getServiceName());
                System.out.println("((NamingEvent) event).getClusters() = " + ((NamingEvent) event).getClusters());
                System.out.println("((NamingEvent) event).getInstances() = " + ((NamingEvent) event).getInstances());
                System.out.println("((NamingEvent) event).getGroupName() = " + ((NamingEvent) event).getGroupName());
            }
        }
    }
}
