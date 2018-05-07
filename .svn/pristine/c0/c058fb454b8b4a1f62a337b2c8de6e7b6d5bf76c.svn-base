/** 
 * Project Name:HolyRobot-Manager-Service 
 * File Name:ConsumerService.java 
 * Package Name:cn.jj.dao 
 * Date:2018年1月11日 下午2:19:59 
 * author 汤玉林
 */ 
package cn.jj.service;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * @Description: TODO
 * @author 汤玉林
 * @date 2018年1月11日 下午2:19:59 
 */
public interface ConsumerService {
	/**
	 * 
	 * @Description 消费者高低优先级队列接收数据
	 * @author 汤玉林
	 * @date 2018年1月11日 下午2:35:38
	 * @action receive
	 * @param destination
	 * @throws JMSException 
	 */
	ObjectMessage receive();
	
	/**
	 * 
	 * @Description 消费者指定队列接收消息
	 * @author 汤玉林
	 * @date 2018年1月11日 下午2:35:38
	 * @action receive
	 * @param destination
	 * @throws JMSException 
	 */
	ObjectMessage receive(String destination);
}
