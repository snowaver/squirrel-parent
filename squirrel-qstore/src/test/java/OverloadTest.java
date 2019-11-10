import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;

import com.google.common.collect.Lists;

import cc.mashroom.squirrel.client.QstoreSubmitter;
import cc.mashroom.squirrel.paip.message.Packet;
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
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class OverloadTest implements TransportFutureListener<PendingAckPacket<?>> {
	static List<QstoreSubmitter> submitters = Lists.newArrayList(
			new  QstoreSubmitter(),new  QstoreSubmitter(),new  QstoreSubmitter(),new  QstoreSubmitter());
	private static  AtomicLong  counter = new  AtomicLong(  0 );
	static int  total = 100000;
	static CountDownLatch cdl = new CountDownLatch(1);
	static DateTime startTime;
	static DateTime endTime;
	public static void main(String[] args) throws InterruptedException {
		OverloadTest listener = new OverloadTest();
		try {
			for(QstoreSubmitter submitter : submitters) {
				submitter.connect( new  TransportConfig(null,"127.0.0.1",8014,5000,15*60) );
			}
			DateTime now = DateTime.now();
			previous = now;
			startTime = now;
			System.err.println(now);
			for(QstoreSubmitter submitter : submitters) {
				run(submitter,listener);
			}
			cdl.await();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			cdl.countDown();
		} finally {
			for(QstoreSubmitter submitter : submitters) {
				submitter.disconnect();
				submitter.release();
			}
		}
	}
	public static byte[] bytes = "hsdfhd9999999999999999999999999999999999999999999999999999999999999999999999999999".getBytes();
	public static void run(QstoreSubmitter submitter, TransportFutureListener<PendingAckPacket<?>> listener)
	{
		List<Packet>  packets = new  LinkedList<>();
		for( int  i = 0;i < total;i = i+1 ) {
			packets.add(new  ByteArrayPacket(bytes).setAckLevel(1,0));
		}
		packets.forEach((bap) -> submitter.write(bap).addTransportFutureListener(listener));
	}
	static DateTime previous;
	@Override
	public void onComplete(TransportFuture<PendingAckPacket<?>> transportFuture) {
		// TODO Auto-generated method stub
		long current = counter.incrementAndGet();
		if( current%200000 == 0 )
		{
			DateTime now = DateTime.now();
			System.err.println(current+"\t"+now+"\t"+((double) 200000*1000/(now.getMillis()-previous.getMillis()))+"/s");
			previous = now;
			endTime = now;
		}
		if( current == total*submitters.size() )
		{
			cdl.countDown();
			System.err.println("finished."+((double) total*submitters.size()*1000/(endTime.getMillis()-startTime.getMillis()))+"/s");
		}
	}
}
