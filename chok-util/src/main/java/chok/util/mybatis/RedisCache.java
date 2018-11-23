package chok.util.mybatis;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import chok.common.BeanFactory;

public class RedisCache implements Cache
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final String id; // cache instance id
    private RedisTemplate<Object, Object> redisTemplate;
    private static final long EXPIRE_TIME_IN_MINUTES = 30; // redis过期时间:30分钟
    
    public RedisCache(String id) 
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }
    
    @Override
    public String getId() 
    {
        return id;
    }
    
    /**
     * Put query result to redis
     *
     * @param key
     * @param value
     */
    @Override
    public void putObject(Object key, Object value) 
    {
        RedisTemplate<Object, Object> redisTemplate = getRedisTemplate();
        ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
        opsForValue.set(key, value, EXPIRE_TIME_IN_MINUTES, TimeUnit.MINUTES);
        if(log.isInfoEnabled()) log.info("Put query result to redis");
    }
    
    /**
     * Get cached query result from redis
     *
     * @param key
     * @return
     */
    @Override
    public Object getObject(Object key) 
    {
        RedisTemplate<Object, Object> redisTemplate = getRedisTemplate();
        ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
        if(log.isInfoEnabled()) log.info("Get cached query result from redis");
        return opsForValue.get(key);
    }
    
    /**
     * Remove cached query result from redis
     *
     * @param key
     * @return
     */
    @Override
    public Object removeObject(Object key) 
    {
        RedisTemplate<Object, ?> redisTemplate = getRedisTemplate();
        redisTemplate.delete(key);
        if(log.isInfoEnabled()) log.info("Remove cached query result from redis");
        return null;
    }
    
    /**
     * Clears this cache instance
     */
    @Override
    public void clear() 
    {
        RedisTemplate<?, ?> redisTemplate = getRedisTemplate();
        redisTemplate.execute((RedisCallback<?>) connection -> 
        {
            connection.flushDb();
            return null;
        });
        if(log.isInfoEnabled()) log.info("Clear all the cached query result from redis");
    }
    
    @Override
    public int getSize() 
    {
        return 0;
    }
    
    @Override
    public ReadWriteLock getReadWriteLock() 
    {
        return readWriteLock;
    }
    
    @SuppressWarnings("unchecked")
	private RedisTemplate<Object, Object> getRedisTemplate() 
    {
        if (redisTemplate == null) 
        {
            redisTemplate = (RedisTemplate<Object, Object>) BeanFactory.getBean("redisTemplate");
        }
        return redisTemplate;
    }
}
