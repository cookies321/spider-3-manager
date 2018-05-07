package cn.jj.service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Repository;

import com.holyrobot.common.Hotelinfo;

@Repository
public class hotelCommentListenner implements MessageListener{
	
	@Autowired  
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	@Autowired
	private CtripCommentByPhantomJs ctripCommentByPhantomJs;

	@Override
	public void onMessage(final Message message) {
		// TODO Auto-generated method stub
		threadPoolTaskExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ObjectMessage objMsg=(ObjectMessage) message;
				try {
					Hotelinfo hotel = (Hotelinfo) objMsg.getObject();
					
					ctripCommentByPhantomJs.ctripCommentByPhantomJs(hotel);
					
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

}
