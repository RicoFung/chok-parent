package chok.lock.redisson;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RedissonLock
{
	/**
	 * 分布式锁的key
	 * @return
	 */
	String lockKey() default "";
	
	/**
	 * 锁类型
	 * @return
	 */
	RedissonLockType lockType() default RedissonLockType.REENTRANT_LOCK;
	
	/**
	 * 加锁失败提示
	 * @return
	 */
	String lockFailMsg() default "get lock failed";
	
//	/**
//	 * 尝试加锁的超时时间 默认一秒
//	 * 该字段只有当tryLock()返回true才有效。
//	 * @return
//	 */
//	long waitTime() default 1 * 1000;
//
//	/**
//	 * 锁释放时间 默认五秒
//     * 超时时间过后，锁自动释放。
//     * 建议：
//     *   尽量缩简需要加锁的逻辑。
//	 * @return
//	 */
//	long leaseTime() default 5 * 1000;
//
//	/**
//	 * 时间格式 默认：毫秒
//	 *
//	 * @return
//	 */
//	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
