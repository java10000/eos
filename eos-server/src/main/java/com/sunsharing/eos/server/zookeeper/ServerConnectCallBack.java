package com.sunsharing.eos.server.zookeeper;

import com.alibaba.fastjson.JSONObject;
import com.sunsharing.eos.common.config.ServiceConfig;
import com.sunsharing.eos.common.utils.StringUtils;
import com.sunsharing.eos.common.zookeeper.PathConstant;
import com.sunsharing.eos.common.zookeeper.ZookeeperCallBack;
import com.sunsharing.eos.common.zookeeper.ZookeeperUtils;
import com.sunsharing.eos.server.ServiceContext;
import com.sunsharing.eos.server.sys.SysProp;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by criss on 14-1-27.
 */
public class ServerConnectCallBack implements ZookeeperCallBack {
    Logger logger = Logger.getLogger(ServerConnectCallBack.class);

    @Override
    public void afterConnect(WatchedEvent event) {
        try {
            logger.info("登录成功了开始调用回调");
            ZookeeperUtils utils = ZookeeperUtils.getInstance();
            utils.watchNode(PathConstant.EOS_STATE);

            ServiceRegister serviceRegister = ServiceRegister.getInstance();
            //先注册EOSID可以多个
            //serviceRegister.registerEos("criss");

            List<ServiceConfig> serviceConfigs = ServiceContext.getServiceConfigList();

            for (ServiceConfig config : serviceConfigs) {
                if(StringUtils.isBlank(config.getAppId()))
                {
                    //你可以在这里注册服务下面是示例
                    JSONObject obj = new JSONObject();
                    obj.put("appId", SysProp.appId);
                    obj.put("serviceId", config.getId());
                    obj.put("version", config.getVersion());
                    obj.put("serialization", config.getSerialization());
                    obj.put("transporter", config.getTransporter());
                    obj.put("timeout", config.getTimeout());
                    obj.put("port", SysProp.nettyServerPort);
                    obj.put("ip", SysProp.localIp);
                    obj.put("real_ip",getRealIp());
                    serviceRegister.registerService(SysProp.eosId, obj.toJSONString());
                }
            }


//            utils.printNode("/");
        } catch (Exception e) {
            logger.error("初始化EOS出错", e);
        }
    }

    @Override
    public void watchNodeChange(WatchedEvent event) {
//        ZookeeperUtils utils = ZookeeperUtils.getInstance();
//        try
//        {
//
//            utils.watchNode(event.getPath());
//        }catch (Exception e)
//        {
//            logger.error("初始化EOS出错",e);
//        }
//
//        if(event.getType()== Watcher.Event.EventType.NodeCreated)
//        {
//            if(event.getPath().startsWith(PathConstant.EOS_STATE))
//            {
//                String eos = event.getPath().substring(event.getPath().lastIndexOf("/")+1);
//                logger.info("有EOS:"+eos+"上线了，考虑是否需要注册");
//            }
//        }
//        if(event.getType()== Watcher.Event.EventType.NodeDeleted)
//        {
//            if(event.getPath().startsWith(PathConstant.EOS_STATE))
//            {
//                String eos = event.getPath().substring(event.getPath().lastIndexOf("/")+1);
//                logger.info("有EOS:"+eos+"下线了，考虑是否做什么");
//            }
//        }

    }


    private String getRealIp()
    {
        String ip = "";
        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
//                    System.out.println("IP:"
//                            + ips.nextElement().getHostAddress());
                    String tmp = ips.nextElement().getHostAddress();
                    if(!tmp.equals("127.0.0.1") && getOccur(tmp,".")==3)
                    {
                        ip+=tmp+",";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }
    private  int getOccur(String src,String find){
        int o = 0;
        int index=-1;
        while((index=src.indexOf(find,index))>-1){
            ++index;
            ++o;
        }
        return o;
    }
}
