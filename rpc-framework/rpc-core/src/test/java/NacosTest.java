import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ychengycheng.constant.RegisterConfigConstant;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Properties;

/**
 * @author Valar Morghulis
 * @Date 2023/9/2
 */

public class NacosTest {
    @Test
    public void NacosTest() throws NacosException {

        int clientPort = RegisterConfigConstant.DEFAULT_NACOS_CLIENT_PORT;
        String clientAddr = RegisterConfigConstant.DEFAULT_NACOS_CLIENT_ADDR;
        Instance instance = new Instance();
        //TODO:不需要指定nacos注册中心的地址吗（参数connectPath）吗？？
        //创建服务节点
        instance.setIp(clientAddr);
        instance.setPort(clientPort);
        instance.setHealthy(true);
        instance.setWeight(1.0);
        //默认非持久化
        instance.setEphemeral(true);
        //设置节点的元数据
        HashMap<String, String> metaHashMap = new HashMap<>();
        instance.setMetadata(metaHashMap);

        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, "127.0.0.1:8848");
        //properties.put(PropertyKeyConst.CONTEXT_PATH, "/nacos");
        properties.put(PropertyKeyConst.USERNAME, "nacos");
        properties.put(PropertyKeyConst.PASSWORD, "nacos");
        NamingService naming = NamingFactory.createNamingService(properties);
        System.out.println(naming);


    }
}
