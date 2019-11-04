import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;

import cc.mashroom.squirrel.client.QstoreSubmitter;
import cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import cc.mashroom.squirrel.paip.message.connect.PendingAckPacket;
import cc.mashroom.squirrel.paip.message.connect.PingPacket;
import cc.mashroom.squirrel.paip.message.extensions.ByteArrayPacket;
import cc.mashroom.squirrel.transport.TransportAndConnectivityGuarantorHandlerAdapter;
import cc.mashroom.squirrel.transport.TransportConfig;
import cc.mashroom.squirrel.transport.TransportFuture;
import cc.mashroom.squirrel.transport.TransportFutureListener;
import cc.mashroom.util.SecureUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class OverloadTest implements TransportFutureListener<PendingAckPacket<?>> {
	static QstoreSubmitter  submitter = new  QstoreSubmitter();
	private static  AtomicLong  counter = new  AtomicLong(  0 );
	static int  total = 100;
	public static void main(String[] args) throws InterruptedException {
		Set<Long> ids = new HashSet<>();
		OverloadTest listener = new OverloadTest();
		submitter.connect( new  TransportConfig(SecureUtils.getSSLContext("/squirrel.cer"),"127.0.0.1",8014,5000,15*60) );
		System.err.println(DateTime.now());
		for( int  i = 0;i < total;i = i+1 )
		{
			ByteArrayPacket bap = new  ByteArrayPacket(RandomStringUtils.random(100).getBytes()).setAckLevel(1,0);
			ids.add(bap.getId());
			submitter.write(bap).addTransportFutureListener( listener );
		}
		System.err.println(ids.size());
	}

	@Override
	public void onComplete(TransportFuture<PendingAckPacket<?>> transportFuture) {
		// TODO Auto-generated method stub
		long current = counter.incrementAndGet();
		if( current%10000 == 0 )
		{
			System.err.println(current);
		}
		if( current == total )
		{
			System.err.println(DateTime.now());
			submitter.disconnect();
			submitter.release();
			
		}
	}
}
