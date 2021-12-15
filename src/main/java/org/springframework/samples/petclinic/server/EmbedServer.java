package org.springframework.samples.petclinic.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
@EnableConfigurationProperties({ EmbedServerProperties.class })
public class EmbedServer implements InitializingBean, DisposableBean {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private PetClinic petClinic;

	@Autowired
	private EmbedServerProperties serverProperties;

	private Thread closeHandler;

	private int availableProcessors = Runtime.getRuntime().availableProcessors();

	private ExecutorService embedServerExecutor = new ThreadPoolExecutor(availableProcessors, availableProcessors * 2,
			60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024), new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "embed-server-thread-" + r.hashCode());
				}
			}, new RejectedExecutionHandler() {
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					logger.error("embedServerExecutor is exhausted.");
				}
			});

	public void startServer() {
		final Integer port = serverProperties.getPort();
		final String address = serverProperties.getAddress();
		final String accessToken = serverProperties.getAccessToken();

		closeHandler = new Thread(new Runnable() {
			@Override
			public void run() {
				NioEventLoopGroup bossGroup = new NioEventLoopGroup(availableProcessors);
				NioEventLoopGroup workerGroup = new NioEventLoopGroup(availableProcessors * 2);
				try {
					ServerBootstrap bootstrap = new ServerBootstrap();
					bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
							.childHandler(new ChannelInitializer<SocketChannel>() {
								@Override
								protected void initChannel(SocketChannel channel) throws Exception {
									channel.pipeline().addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS))
											.addLast(new HttpServerCodec())
											.addLast(new HttpObjectAggregator(10 * 1024 * 1024))
											.addLast(new EmbedServerHandler(embedServerExecutor, accessToken,
													petClinic));
								}
							}).childOption(ChannelOption.SO_KEEPALIVE, true);
					ChannelFuture channelFuture = bootstrap.bind(port).sync();
					logger.info("embed Server start successfully");
					channelFuture.channel().closeFuture().sync();
				}
				catch (Exception e) {
					logger.error("start Embed Server Error", e);
				}
				finally {
					try {
						bossGroup.shutdownGracefully();
						workerGroup.shutdownGracefully();
					}
					catch (Exception e) {
						logger.error("close Netty EventLoopGroup Error", e);
					}
				}
			}
		});
		closeHandler.setDaemon(true);
		closeHandler.start();
	}

	public void stopServer() {
		if (closeHandler != null && closeHandler.isAlive()) {
			try {
				closeHandler.interrupt();
			}
			catch (Throwable e) {
				logger.error("interrupt close Handler Thread Error", e);
			}
		}

		embedServerExecutor.shutdown();
	}

	@Override
	public void destroy() throws Exception {
		stopServer();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		startServer();
	}

}
