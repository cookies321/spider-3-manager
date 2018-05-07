/** 
 * Project Name:HolyRobot-Manager-Service 
 * File Name:KafkaUtils.java 
 * Package Name:cn.jj.utils 
 * Date:2018年2月28日 上午9:41:34 
 * author 汤玉林
 */ 
package cn.jj.utils;

import com.alibaba.fastjson.JSON;
import com.holyrobot.data.ReceiverData;

/**
 * @Description: TODO
 * @author 汤玉林
 * @date 2018年2月28日 上午9:41:34 
 */
public class KafkaUtils {

	
	public static String KAFKAURL="http://192.168.0.230:8081/send";
	
	private int type;
	
	private Object o;
	
	public KafkaUtils() {
		
	}
	
	public KafkaUtils(Object o,int type) {
		this.o=o;
		this.type=type;
	}
	
	public static String parseJsonObject(Object o,int type,int flag){
		ReceiverData receiver = new ReceiverData();
		receiver.setType(type);
		receiver.setFlag(flag); 
		receiver.setData(o);
		receiver.setVersion("v4.0");
		//String json = JSONObject.fromObject(receiver).toString();
		String jsonString = JSON.toJSONString(receiver);
		return jsonString;
	} 
}
