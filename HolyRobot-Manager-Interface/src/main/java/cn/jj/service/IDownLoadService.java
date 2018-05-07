package cn.jj.service;

import java.util.Map;

public interface IDownLoadService {
	//代理ip 发送get请求
	String httpClientGet(String url, Map<?, ?>... maps);
	//本地ip 发送get请求
	String httpClientDefultGet(String url, Map<?, ?>... maps);
	//代理ip 发送post请求（赵乐）
	String sendPost(String url, String param,Map<?, ?>... maps);
	//代理ip 发送post请求（徐仁杰）
	String post(String url, String param, Map<?, ?>... maps);
	

}
