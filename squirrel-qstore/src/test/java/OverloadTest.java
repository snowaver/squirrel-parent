import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class OverloadTest {
	public static void main(String[] args) throws InterruptedException {
		ThreadPoolExecutor  threadPool = new  ThreadPoolExecutor( 4,4,2,TimeUnit.MINUTES,new  LinkedBlockingQueue<Runnable>() );
		
		JedisPoolConfig  poolConfig = new  JedisPoolConfig();
		poolConfig.setTestWhileIdle( true );
		poolConfig.setMaxIdle( 5 );
		poolConfig.setTestOnBorrow( false );
		poolConfig.setMaxTotal(32);
        JedisPool  jedisPool = new  JedisPool( poolConfig,"127.0.0.1",8014 );
        
        int  count = 1000000;
		CountDownLatch  latch = new  CountDownLatch( count );
		System.err.println(DateTime.now());
		
		for( int  i = 1;i <= count;i = i+1 )
		{
			threadPool.submit
			(
				() ->
				{
					try(Jedis jedis = jedisPool.getResource())
					{
						jedis.set( String.valueOf(RandomUtils.nextLong()),"{\"content\":\""+RandomStringUtils.random(255)+"\"}" );
					}
					finally
					{
						latch.countDown();
						
						long  countDowns =  latch.getCount();
						
						if( countDowns%50000 == 0 ) {
							System.err.println(countDowns);
						}
					}
				}
			);
		}
		
		latch.await();
		
		threadPool.shutdown();
		
        jedisPool.close();
        
        System.err.println(DateTime.now());
	}
}
