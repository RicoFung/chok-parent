package chok.redis.mybatis;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import chok.common.BeanFactory;

public class RedisCache implements Cache
{
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ReadWriteLock				readWriteLock			= new ReentrantReadWriteLock();
	private final String					id;														// cache instance id
	private RedisTemplate<Object, Object>	redisTemplate;
	private static final long				EXPIRE_TIME_IN_MINUTES	= 30;							// redis过期时间:30分钟

	public RedisCache(final String id)
	{
		if (id == null)
		{
			throw new IllegalArgumentException("Cache instances require an ID");
		}
		this.id = id;
		if (log.isDebugEnabled())
		{
			log.debug("Redis Cache id " + id);
		}
	}

	@Override
	public String getId()
	{
		return this.id;
	}

	@Override
	public void putObject(Object key, Object value)
	{
		if (value != null)
		{
			// 向Redis中添加数据，有效时间是30分钟
			getRedisTemplate().opsForValue().set(key.toString(), value, EXPIRE_TIME_IN_MINUTES, TimeUnit.MINUTES);
		}
	}

	@Override
	public Object getObject(Object key)
	{
		try
		{
			if (key != null)
			{
				Object obj = getRedisTemplate().opsForValue().get(key.toString());
				return obj;
			}
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
		}
		return null;
	}

	@Override
	public Object removeObject(Object key)
	{
		try
		{
			if (key != null)
			{
				getRedisTemplate().delete(key.toString());
			}
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
		}
		return null;
	}

	@Override
	public void clear()
	{
		if (log.isDebugEnabled())
		{
			log.debug("清空缓存id: {}", this.id);
		}
		try
		{
			Set<Object> keys = getRedisTemplate().keys("*:" + this.id + "*");
			if (!CollectionUtils.isEmpty(keys))
			{
				getRedisTemplate().delete(keys);
			}
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
		}
	}

	@Override
	public int getSize()
	{
		Long size = (Long) getRedisTemplate().execute(new RedisCallback<Long>()
		{
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException
			{
				return connection.dbSize();
			}
		});
		return size.intValue();
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

	@Override
	public ReadWriteLock getReadWriteLock()
	{
		return this.readWriteLock;
	}
}
