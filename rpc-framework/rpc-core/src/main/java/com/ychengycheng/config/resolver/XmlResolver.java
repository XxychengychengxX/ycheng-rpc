package com.ychengycheng.config.resolver;

import com.ychengycheng.config.BootStrapConfiguration;
import com.ychengycheng.config.ProtocolConfig;
import com.ychengycheng.config.RegistryConfig;
import com.ychengycheng.config.wrapper.ObjectWrapper;
import com.ychengycheng.core.compress.Compressor;
import com.ychengycheng.core.compress.CompressorFactory;
import com.ychengycheng.core.discovery.RegistryCenter;
import com.ychengycheng.core.discovery.impl.NacosRegistryCenter;
import com.ychengycheng.core.loadbalancer.LoadBalancer;
import com.ychengycheng.core.loadbalancer.impl.RoundRobinBalancer;
import com.ychengycheng.core.protection.CircuitBreaker;
import com.ychengycheng.core.protection.impl.RequestCircuitBreaker;
import com.ychengycheng.core.serialize.Serializer;
import com.ychengycheng.core.serialize.SerializerFactory;
import com.ychengycheng.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;


@Slf4j
public class XmlResolver {
    public static void main(String[] args) {
        XmlResolver xmlResolver = new XmlResolver();
        BootStrapConfiguration bootStrapConfiguration = new BootStrapConfiguration();
        xmlResolver.loadFromXml(bootStrapConfiguration);
    }

    /**
     * 从配置文件读取配置信息,我们不使用dom4j，使用原生的api
     *
     * @param configuration 配置实例
     */
    public void loadFromXml(BootStrapConfiguration configuration) {
        try {
            // 1、创建一个document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 禁用DTD校验：可以通过调用setValidating(false)方法来禁用DTD校验。
            factory.setValidating(false);
            // 禁用外部实体解析：可以通过调用setFeature(String name, boolean value)方法并将“http://apache.org/xml/features/nonvalidating/load-external-dtd”设置为“false”来禁用外部实体解析。
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("ycheng-rpc.xml");
            Document doc = builder.parse(inputStream);

            // 2、获取一个xpath解析器
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            // 3、解析所有的标签
            //todo:这里为了不使xml未配置的项覆盖spi默认配置，传入了对应参数，后面想想怎么优化代码
            configuration.setPort(resolvePort(doc, xpath));
            configuration.setIdGenerator(resolveIdGenerator(doc, xpath, configuration.getIdGenerator()));
            configuration.setRegistryConfig(resolveRegistryConfig(doc, xpath, configuration.getRegistryCenter()));
            configuration.setProtocolConfig(resolveProtocolConfig(doc, xpath, configuration.getProtocolConfig()));
            configuration.setLoadBalancer(resolveLoadBalancer(doc, xpath, configuration.getLoadBalancer()));
            configuration.setRegistryCenter(resolveRegistryCenter(doc, xpath, configuration.getRegistryCenter()));
            configuration.setCircuitBreaker(resolveCircuitBreaker(doc, xpath, configuration.getCircuitBreaker()));


        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.warn(
                    "If no configuration file is found or an exception occurs when parsing the configuration file, " + "the default configuration is used.",
                    e);
        }
    }

    private CircuitBreaker resolveCircuitBreaker(Document doc, XPath xpath, CircuitBreaker circuitBreaker) {

        String expression = "/configuration/circuit-breaker";
        CircuitBreaker temp = parseObject(doc, xpath, expression, null);
        if (temp == null) {
            if (circuitBreaker == null) {
                circuitBreaker = new RequestCircuitBreaker();
                log.warn("circuitBreaker NOT FOUND , may use default [{}] instead",
                         circuitBreaker.getClass().getName());
            }
        } else {
            circuitBreaker = temp;
        }
        return circuitBreaker;
    }

    private RegistryCenter resolveRegistryCenter(Document doc, XPath xpath, RegistryCenter registryCenter) {
        String expression = "/configuration/registry-center";
        RegistryCenter temp = parseObject(doc, xpath, expression, null);
        if (temp == null) {
            if (registryCenter == null) {
                registryCenter = new NacosRegistryCenter();
                log.warn("registryCenter NOT FOUND , may use default [{}] instead",
                         registryCenter.getClass().getName());
            }
        } else {
            registryCenter = temp;
        }
        return registryCenter;

    }

    private ProtocolConfig resolveProtocolConfig(Document doc, XPath xpath, ProtocolConfig protocolConfig) {
        if (protocolConfig == null) {
            protocolConfig = new ProtocolConfig();
        }

        //压缩类型设置
        String compressorName = resolveCompressor(doc, xpath);
        if (compressorName == null) {
            if (protocolConfig.getCompressType() == null || protocolConfig.getCompressType().equals("")) {
                compressorName = "gzip";
                protocolConfig.setCompressType(compressorName);
            }
        }

        //序列化类型设置
        String serializerName = resolveSerializer(doc, xpath);
        if (serializerName == null) {
            if (protocolConfig.getSerializeType() == null || protocolConfig.getSerializeType().equals("")) {
                serializerName = "jdk";
                protocolConfig.setSerializeType(serializerName);
            }

        }

        log.info("use compressor: [{}] , use serializer : [{}] .", protocolConfig.getCompressType(),
                 protocolConfig.getSerializeType());

        return protocolConfig;
    }


    /**
     * 解析端口号
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 端口号
     */
    private int resolvePort(Document doc, XPath xpath) {
        String expression = "/configuration/port";
        String portString = parseString(doc, xpath, expression);
        if (portString != null) {
            return Integer.parseInt(portString);
        }
        return 9988;
    }

    /**
     * 解析应用名称
     *
     * @param doc            文档
     * @param xpath          xpath解析器
     * @param registryCenter
     * @return 应用名
     */
    private RegistryConfig resolveRegistryConfig(Document doc, XPath xpath, RegistryCenter registryCenter) {
        RegistryConfig registryConfig = new RegistryConfig();
        String registry = "/configuration/registry";

        String serverExpression = "/configuration/registry/server";
        String clientExpression = "/configuration/registry/client";
        String appName = parseString(doc, xpath, registry, "appName");
        String serverAddr = parseString(doc, xpath, serverExpression, "addr");
        String serverPort = parseString(doc, xpath, serverExpression, "port");
        String clientAddr = parseString(doc, xpath, clientExpression, "addr");
        registryConfig.setApplicationName(appName);
        registryConfig.setServerAddr(serverAddr != null ? serverAddr : "localhost");
        registryConfig.setServerPort(serverPort != null ? Integer.parseInt(serverPort) : 8848);
        registryConfig.setClientAddr(clientAddr != null ? clientAddr : "127.0.0.1");
        registryConfig.setClientPort(resolvePort(doc, xpath));

        return registryConfig;

    }

    /**
     * 解析负载均衡器
     *
     * @param doc          文档
     * @param xpath        xpath解析器
     * @param loadBalancer
     * @return 负载均衡器实例
     */
    private LoadBalancer resolveLoadBalancer(Document doc, XPath xpath, LoadBalancer loadBalancer) {
        String expression = "/configuration/loadBalancer";
        LoadBalancer temp = parseObject(doc, xpath, expression, null);
        if (temp == null) {
            if (loadBalancer == null) {
                loadBalancer = new RoundRobinBalancer();
                log.warn("circuitBreaker NOT FOUND , may use default [{}] instead",
                         loadBalancer.getClass().getName());
            }
        } else {
            loadBalancer = temp;
        }
        return loadBalancer;
    }

    /**
     * 解析id发号器
     *
     * @param doc         文档
     * @param xpath       xpath解析器
     * @param idGenerator
     * @return id发号器实例
     */
    private IdGenerator resolveIdGenerator(Document doc, XPath xpath, IdGenerator idGenerator) {
        String expression = "/configuration/id-generator";
        String aClass = parseString(doc, xpath, expression, "class");
        String dataCenterId = parseString(doc, xpath, expression, "dataCenterId");
        String machineId = parseString(doc, xpath, expression, "machineId");

        try {
            Class<?> clazz = Class.forName(aClass);
            Object instance = clazz.getConstructor(new Class[]{long.class, long.class})
                                   .newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
            return (IdGenerator) instance;
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 解析压缩的具体实现
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return ObjectWrapper<Compressor>
     */
    private String resolveCompressor(Document doc, XPath xpath) {
        String expression = "/configuration/protocol/compressor";
        Compressor compressor = parseObject(doc, xpath, expression, null);
        if (compressor != null) {
            Byte code = Byte.valueOf(Objects.requireNonNull(parseString(doc, xpath, expression, "code")));
            String name = parseString(doc, xpath, expression, "name");
            ObjectWrapper<Compressor> compressorObjectWrapper = new ObjectWrapper<>(code, name, compressor);
            CompressorFactory.addCompressor(compressorObjectWrapper);
            return name;
        }

        return null;
    }

    /**
     * 解析序列化器
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 序列化器
     */
    private String resolveSerializer(Document doc, XPath xpath) {
        String expression = "/configuration/protocol/serializer";

        Serializer serializer = parseObject(doc, xpath, expression, null);
        if (serializer != null) {
            Byte code = Byte.valueOf(Objects.requireNonNull(parseString(doc, xpath, expression, "code")));
            String name = parseString(doc, xpath, expression, "name");
            ObjectWrapper<Serializer> serializerObjectWrapper = new ObjectWrapper<>(code, name, serializer);
            SerializerFactory.addSerializer(serializerObjectWrapper);
            return name;
        }
        return null;
    }


    /**
     * 获得一个节点文本值   <port>7777</>
     *
     * @param doc        文档对象
     * @param xpath      xpath解析器
     * @param expression xpath表达式
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xpath, String expression) {
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }


    /**
     * 获得一个节点属性的值   <port num="7777"></>
     *
     * @param doc           文档对象
     * @param xpath         xpath解析器
     * @param expression    xpath表达式
     * @param AttributeName 节点名称
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xpath, String expression, String AttributeName) {
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getAttributes().getNamedItem(AttributeName).getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }

    /**
     * 解析一个节点，返回一个实例
     *
     * @param doc        文档对象
     * @param xpath      xpath解析器
     * @param expression xpath表达式
     * @param paramType  参数列表
     * @param param      参数
     * @param <T>        泛型
     * @return 配置的实例
     */
    private <T> T parseObject(Document doc, XPath xpath, String expression, Class<?>[] paramType, Object... param) {
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (targetNode == null) {
                log.warn("XML node [{}] NOT FOUND , will use default setting instead", expression);
                return null;
            }
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instant = null;
            if (paramType == null) {
                instant = aClass.getConstructor().newInstance();
            } else {
                instant = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T) instant;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | XPathExpressionException e) {
            log.error("An exception occurred while parsing the XML configuration file.", e);
        }
        return null;
    }


}
