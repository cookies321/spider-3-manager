package cn.jj.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.jj.utils.HeadersUtils;
import cn.jj.utils.NumUtils;

@Service
public class DownLoadServiceImpl implements IDownLoadService{
	
	@Value("${PROXY_IP_REDIS_KEY}")
	private String PROXY_IP_REDIS_KEY;
	
	@Autowired
	private JedisClient jedis;
	
	@Override
	public String httpClientGet(String url, Map<?, ?>... maps) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String key = "";
		try {
			HttpGet get = new HttpGet(url);
			/******************* 设置代理IP ******************/
			key = getProxyIpPort();
			RequestConfig config = getRequestConfig(key);
			/******************* 设置代理IP ******************/
			get.setConfig(config);
			setGetHeaders(get, maps);
			CloseableHttpResponse response = httpClient.execute(get);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			return result;
		} catch (Exception e) {
			//e.printStackTrace();
			try {
				jedis.del(key);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @author 徐仁杰
	 * @date 2017年12月13日 下午4:58:09
	 * @action httpClientDefultGet
	 * @return String
	 */
	public String httpClientDefultGet(String url, Map<?, ?>... maps) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		StringBuffer key = new StringBuffer();
		try {
			HttpGet get = new HttpGet(url);
			setGetHeaders(get, maps);
			CloseableHttpResponse response = httpClient.execute(get);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			return result;
		} catch (Exception e) {
			jedis.del(key.toString());
			return null;
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @Description 代理ip链接post 参数为json
	 * @author 赵乐
	 * @date 2017年12月13日 下午4:58:09
	 * @action httpClientDefultGet
	 * @return String
	 */
	public String sendPost(String url, String param, Map<?,?>...maps) {
		String key = "";
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		URLConnection conn= null;
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			if(url.contains("192")){
				key="";
			}else{
				key = getProxyIpPort();
			}
			if (StringUtils.isNotBlank(key)) {
				String replace = key.replace(PROXY_IP_REDIS_KEY, "");
				String[] arr = replace.split(":");
				String proxy_ip = arr[0];
				int proxy_port = Integer.parseInt(arr[1]);
				InetSocketAddress addr = new InetSocketAddress(proxy_ip,proxy_port);  
				Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
				conn = realUrl.openConnection(proxy);
			}else{
				conn = realUrl.openConnection();
			}
			// 设置通用的请求属性
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36"
					+ " (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
			for (Map<?, ?> map : maps) {
				Iterator<?> iterator=map.keySet().iterator();
				while (iterator.hasNext()) {
					String next = (String) iterator.next();
					String object = (String) map.get(next);
					conn.setRequestProperty(next, object);
				}
			}
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			//System.out.println("发送 POST 请求出现异常！" + e);
			//System.out.println(url + "\n" + param);
			//e.printStackTrace();
			return result;
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				//ex.printStackTrace();
			}
		}
		return result;
	}



	/**
	 * @Description 发送post请求
	 * @author 徐仁杰
	 * @date 2017年12月13日 下午2:56:20
	 * @action post
	 * @return String
	 */
	public String post(String url, String param, Map<?, ?>... maps) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String key = "";
		try {
			HttpPost post = new HttpPost(url);
			/******************* 设置代理IP ******************/
			key=getProxyIpPort();
			RequestConfig config = getRequestConfig(key);
			/******************* 设置代理IP ******************/
			post.setConfig(config);
			setPostHeaders(post, maps);
			StringEntity stringEntity = new StringEntity(param, "UTF-8");
			post.setEntity(stringEntity);
			CloseableHttpResponse response = httpClient.execute(post);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			return result;
		} catch (Exception e) {
			jedis.del(key.toString());
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @Description 获取httpclient上下文
	 * @author 徐仁杰
	 * @date 2017年12月13日 下午2:11:18
	 * @action getRequestConfig
	 * @return RequestConfig
	 */
	public RequestConfig getRequestConfig(String key) {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(5000)
				.setSocketTimeout(2000).build();
		if (StringUtils.isNotBlank(key)) {
			String replace = key.replace(PROXY_IP_REDIS_KEY, "");
			String[] arr = replace.split(":");
			String proxy_ip = arr[0];
			int proxy_port = Integer.parseInt(arr[1]);
			HttpHost proxy = new HttpHost(proxy_ip, proxy_port);
			config = RequestConfig.custom().setProxy(proxy).setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(5000).build();
		}
		return config;
	}

	/**
	 * @Description 获取代理IP
	 * @author 徐仁杰
	 * @date 2017年12月15日 上午11:38:53
	 * @action getProxyIpPort
	 * @return String
	 */
	public String getProxyIpPort() {
	
		Set<String> keys = jedis.getKeys(PROXY_IP_REDIS_KEY + "*");
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

	/**
	 * @Description 设置get请求头部信息
	 * @author 徐仁杰
	 * @date 2017年12月13日 下午3:16:36
	 * @action setGetHeaders
	 * @return void
	 */
	public static void setGetHeaders(HttpGet get, Map<?, ?>... maps) {
		get.setHeader("User-Agent", HeadersUtils.getUserAgent());
		for (Map<?, ?> map : maps) {
			Iterator<?> iterator = map.keySet().iterator();
			while (iterator.hasNext()) {
				String next = (String) iterator.next();
				String object = (String) map.get(next);
				get.setHeader(next, object);
			}
		}
	}

	/**
	 * @Description 设置post请求头部信息
	 * @author 徐仁杰
	 * @date 2017年12月13日 下午3:16:36
	 * @action setGetHeaders
	 * @return void
	 */
	public static void setPostHeaders(HttpPost post, Map<?, ?>... maps) {
		post.setHeader("User-Agent", HeadersUtils.getUserAgent());
		for (Map<?, ?> map : maps) {
			Iterator<?> iterator = map.keySet().iterator();
			while (iterator.hasNext()) {
				String next = (String) iterator.next();
				String object = (String) map.get(next);
				post.setHeader(next, object);
			}
		}
	}

	public  void main(String[] args) throws Exception {
		//while(true) {
		
			/*String proxyIpPort = getProxyIpPort(new StringBuffer());
			System.out.println(proxyIpPort);
			Thread.sleep(500);*/

			String url="https://gny.ly.com/list?src=%E4%B8%8A%E6%B5%B7&dest=%E4%B8%89%E4%BA%9A";
			//String param="CityId=321&BizSectionId=0&SectionId=0&Word=&PriceRegion=&Range=&HotelStar=&ChainId=&Facilities=&BreakFast=&PayType=&SortType=0&Instant=&LabelId=0&WordType=0&ThemeId=&Latitude=&Longitude=&ComeDate=2017-12-25&LeaveDate=2017-12-26&PageSize=20&Page=2&antitoken=9f4a84e0fd992a726f59c7ed242a5edd&IsSeo=0&iid=0.09717973612948816&HotelType=0";

			//String url="https://www.ly.com/go/RainbowClientAjax/GetAladdin?_dAjax=callback&requsetParms={cityId:6321,selectCityId:6321,fromSite:1,poiType:1,type2Id:999,pageIndex:1,pageSize:15}&serviceName=getpoiorderfilterchoice";
			//String httpClientGet = sendPost(url,"");

		//}
			Map<String,String> header=new HashMap<>();
			header.put("Cookie", "Hm_lvt_7cda147e17cf103b1fb19362796011e2=1511923242; Qs_lvt_86790=1511923247; Qs_pv_86790=1174653012080433000%2C1582880676893488400%2C3701143814556470000%2C2085264719259586000%2C1523536871126638300; _ga=GA1.2.2084284690.1511923242; abtkey=f3c6036c-3111-4cf9-8370-a7e61cc27591; NewProvinceId=25; NCid=321; __tctmu=144323752.0.0; __tctmz=144323752.1514162466951.30.1.utmccn=(organic)|utmcmd=organic|utmEsl=gb2312|utmcsr=baidu|utmctr=; longKey=151133388079664; __tctrack=0; COMSEInfo=RefId=1308721&SEFrom=&SEKeyWords=&RefUrl=; Hm_lvt_c6a93e2a75a5b1ef9fb5d4553a2226e5=1512369164,1512701079,1513845594,1514162469; Hm_lvt_d3dcbf14b43296ccba0611c7d61e927a=1512350053,1512369164,1513845607,1514162469; Hm_lpvt_d3dcbf14b43296ccba0611c7d61e927a=1514180875; __tctma=144323752.151133388079664.1511333880751.1514176976919.1514180875219.35; gny_start_city_id=321; tc_clhistory=33005%2C%25E4%25B8%258A%25E6%25B5%25B7%2C1; gnypvStatis=user%3D1da2e20c-fff0-4e93-af51-21fcd16a13ce; gny_city_info=%7B%22CityId%22%3A321%2C%22CityArea%22%3A%22%E5%8D%8E%E4%B8%9C%22%2C%22CityName%22%3A%22%E4%B8%8A%E6%B5%B7%22%2C%22FullPinyinName%22%3A%22shanghai%22%2C%22FirstZiMu%22%3A%22S%22%2C%22ProvinceId%22%3A25%2C%22ProvinceName%22%3A%22%E4%B8%8A%E6%B5%B7%22%2C%22ShortPy%22%3A%22sh%22%2C%22TcShortPy%22%3A%22sh%22%7D; _gat=1; tdt=1|1514186954.971|fc3f57689a1f81ecd51af7535f7195101686e72155492d484bad5feb81fba073; td_sid=MTUxNDE4Njk1Nyw4ZDMzNmQyODllOTY0OTEyOGFkMDljYjdiMWM1OTgzMTE4NmNmYmU2NzFkNDdhMDY2NDcxYjAzNTUyOGVhMmQ4LGFmMWQ5NTE3YTVmN2UwMmU3MTM4MGM4MzliOGYyYzViYmRhOWQ2ZjBlMTU3OGUwOWVmNjVmODUzNzJhOTI3MjE=; k_st=8.8.8.8|1514186957; td_did=1SmyIZkg5fJUviW5V3p8hZBA2Eq0KIKZNLrAmLua1LrW1DIAGfhdQaYgTYJdm2yczk%2BZOmz6BbcO08pAojmIwFwclMwU4SixkZu2pcKNe8LtFCbUC4PBTSyblpMUcKW5vy3EgzMg07ThwP7dPz%2BjncRCB2uzZ2J99XNt7E8dj1Pf%2B6TW%2BMK6HVPYO5mk62%2F%2FcByN%2FsVYKyctmcAqq3I7Hw%3D%3D; t_q=1514186956362; 17uCNRefId=RefId=18024458&SEFrom=baidu&SEKeyWords=; TicketSEInfo=RefId=18024458&SEFrom=baidu&SEKeyWords=; CNSEInfo=RefId=18024458&tcbdkeyid=&SEFrom=baidu&SEKeyWords=&RefUrl=https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3DKXykV3VmskX-jY7vFYPd_hzhQ68Sj1pEWYOvAH5-LPu%26wd%3D%26eqid%3De86bf5d400022134000000045a40491c; tc_sehistory=%E4%B8%89%E4%BA%9A%2C%E4%B8%8A%E6%B5%B7; qdid=39264|1|6928722|0a6c16,39264|1|6928722|0a6c16,39264|1|6928722|0a6c16; Hm_lvt_64941895c0a12a3bdeb5b07863a52466=1513845580,1514162466,1514187020,1514187258; Hm_lpvt_64941895c0a12a3bdeb5b07863a52466=1514187258; Hm_lpvt_c6a93e2a75a5b1ef9fb5d4553a2226e5=1514187418; __tctmc=144323752.121597022; __tctmd=144323752.737325; __tctmb=144323752.1519343234723718.1514187270750.1514187418628.49; route=b3bb5d9990ad33d5a9a0460f6078f8d6; _Jo0OQK=210E7A95362A4ABC0D6AF4203E7D492084D3D6AA06EA2DAC9E876954FB18C3E0650C3CD1A3699DBAE14079FB4E2A4C0211806141C9D0BB27468125B1E91D4C0BD6A4EE0A4BC91AA0EDF1D13A20F6125D6ED1D13A20F6125D6ED5EC4D37514CCBE0857F416A98C7DD268GJ1Z1IQ==");
			header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
			String httpClientGet = httpClientGet(url,header);
			System.out.println(httpClientGet);

	}
	
	
}
