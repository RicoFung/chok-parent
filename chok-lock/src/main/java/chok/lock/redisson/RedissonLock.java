package chok.lock.redisson;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RedissonLock
{
	/**
	 * 分布式锁的key
	 * 
	 * @return
	 */
	String lockKey() default "";

	/**
	 * 锁类型
	 * 
	 * @return
	 */
	RedissonLockType lockType() default RedissonLockType.REENTRANT_LOCK;

	/**
	 * 加锁失败提示
	 * 
	 * @return
	 */
	String lockFailMsg() default "get lock failed";
	
	/**
	 * 选择调用哪个tryLock()方法。默认1。
	 * 1:tryLock();
	 * 2:tryLock(waitTime, timeUnit);
	 * 3:tryLock(waitTime, leaseTime, timeUnit);
	 * @return
	 */
	int tryLockOption() default 1;

	/**
	 * 尝试获取锁的最大等待时间，超过这个值，则认为获取锁失败。 
	 * 默认一秒。 
	 * 该字段只有当tryLock()返回true才有效。
	 * 
	 * @return
	 */
	long waitTime() default 1 * 1000;

	/**
	 * 锁的持有时间,超过这个时间锁会自动失效（值应设置为大于业务处理的时间，确保在锁有效期内业务能处理完）。 
	 * 默认五秒。 
	 * 超时时间过后，锁自动释放。 
	 * 建议：尽量缩简需要加锁的业务逻辑。
	 * 
	 * @return
	 */
	long leaseTime() default 5 * 1000;

	/**
	 * 时间格式 默认：毫秒
	 *
	 * @return
	 */
	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
