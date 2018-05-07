/** 
 * Project Name:HolyRobot-Manager-Service 
 * File Name:ProducerService.java 
 * Package Name:cn.jj.dao 
 * Date:2018年1月11日 下午2:01:52 
 * author 汤玉林
 */ 
package cn.jj.service;

import javax.jms.Destination;


/**
 * @Description: TODO
 * @author 汤玉林
 * @date 2018年1月11日 下午2:01:52 
 */
public interface ProducerService {

	/**
	 * 
	 * @Description 向指定队列发送消息
	 * @author 汤玉林
	 * @date 2018年1月11日 下午2:21:35
	 * @action sendMessage
	 * @param destination
	 * @param msg
	 */
	void sendMessage(String destination,Object obj);
	
	/**
	 * 
	 * @Description 向默认队列发送消息
	 * @author 汤玉林
	 * @date 2018年1月11日 下午2:22:36
	 * @action sendMessage
	 * @param msg
	 */
	void sendMessage(Object obj);
}
