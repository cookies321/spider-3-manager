/** 
 * Project Name:HolyRobot-Manager-Service 
 * File Name:ProducerServiceImpl.java 
 * Package Name:cn.jj.dao.impl 
 * Date:2018年1月11日 下午2:39:24 
 * author 汤玉林
 */ 
package cn.jj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 * @Description: TODO
 * @author 汤玉林
 * @date 2018年1月11日 下午2:39:24 
 */
@Service
public class ProducerServiceImpl implements ProducerService{

	@Autowired
	private JmsTemplate jmsTemplate;
	
	
	@Override
	public void sendMessage(String destination, Object obj) {
		//System.out.println("向队列" + destination.toString() + "发送了消息------------" + obj);  
		jmsTemplate.convertAndSend(destination,obj);
		
	}

	
	@Override
	public void sendMessage(Object obj) {
		String destination =  jmsTemplate.getDefaultDestination().toString();  
	   // System.out.println("向队列" +destination+ "发送了消息------------" + obj); 
	    
	    jmsTemplate.convertAndSend(obj);
	    
	}

}
