package cn.jj.service;

import java.util.List;
import java.util.Set;

public interface JedisClient {
	//redis常用方法
	String get(String key);
	String set(String key, String value);
	String hget(String hkey, String key);
	long hset(String hkey, String key, String value);
	long incr(String key);
	long expire(String key, int second);
	long ttl(String key);
	long del(String key);
	long hdel(String hkey, String key);
	void close();
	void flushDB();
	//模糊查询key的值
	Set<String> getKeys(String key);
	//查询list
	List<Object> lrange(String key, int start, int end);
	//list中添加string类型的值
	void addObjectToList(String Key, Object obj);
	//从list中获取
	Object pollFromList(String key);
	//序列化
	byte[] serialize(Object obj);
	//反序列化
	Object unserizlize(byte[] byt);
	
	//多条件查询
	<T> List<T> selectByMultipleAttribute(Object record);
	//根据id查询单个对象
	Object getBeaninfoById(Class clazz,String id);
	//添加一条记录,并得到id
	String insertAndGetId(Object record);
	//添加一个list里面的记录
	<T> long insertBatch(List<T> objectList);
	//删除一个带id的记录
	int deleteById(Object record);
	//多条件删除
	void deleteByMultipleAttribute(Object record);
	//添加对象到队列
	void addQueue(String Key, Object obj);
	
	
}
