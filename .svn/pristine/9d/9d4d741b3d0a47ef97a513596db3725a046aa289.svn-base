package cn.jj.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import cn.jj.service.IDownLoadService;
import cn.jj.service.JedisClient;

/**
 * @Description: 定时获取代理ip
 * @author 徐仁杰
 * @date 2017年11月30日 下午2:28:11
 */

@Repository
public class ProxyJob{
	
	@Value("${PROXY_IP_ORDER_NUMBER}")
	private String PROXY_IP_ORDER_NUMBER;
	
	@Value("${PROXY_IP_REDIS_KEY}")
	private String PROXY_IP_REDIS_KEY;
	
	@Autowired
	private IDownLoadService downLoadService;
	
	@Autowired
	private JedisClient jedis;
	
	@Scheduled(cron = "0/1 * * * * ?")
	public void scheduleProxyIP() {
		try {
			String url = PROXY_IP_ORDER_NUMBER;
			String data = downLoadService.httpClientDefultGet(url);
			String[] split = data.split("\n");
			if (split.length == 2) {
				String fristIp = split[0].trim();
				String secondIp = split[1].trim();
				jedis.set(PROXY_IP_REDIS_KEY + fristIp, "1");
				jedis.set(PROXY_IP_REDIS_KEY + secondIp, "1");
				jedis.expire(PROXY_IP_REDIS_KEY + fristIp, 290);
				jedis.expire(PROXY_IP_REDIS_KEY + secondIp, 290);
				System.err.println(fristIp+"--"+secondIp);
			} else {
				System.out.println("获取代理ip出错");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 *获取代理ip
	 * @return
	 */
	public String getProxyIpPort() {
		Set<String> keys = jedis.getKeys(PROXY_IP_REDIS_KEY + "*");
		jedis.close();
		String ip_port = null;
		List<String> list = new ArrayList<>();
		for (String string : keys) {
			list.add(string);
		}
		for (int i = 0; i < list.size();) {
			ip_port = list.get(NumUtils.getRandom(list.size(), 0));
			break;
		}
		
		return ip_port;
	}

}
