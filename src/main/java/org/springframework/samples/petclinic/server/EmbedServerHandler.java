package org.springframework.samples.petclinic.server;

import cn.hutool.json.JSONUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.samples.petclinic.owner.Owner;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class EmbedServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private ExecutorService embedServerExecutor = null;

	private String accessToken;

	private PetClinic petClinic;

	public EmbedServerHandler(ExecutorService embedServerExecutor, String accessToken, PetClinic petClinic) {
		this.embedServerExecutor = embedServerExecutor;
		this.accessToken = accessToken;
		this.petClinic = petClinic;

	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		HttpMethod method = msg.method();
		String uri = msg.uri();
		String reqMessage = msg.content().toString(Charset.forName("UTF-8"));
		boolean keepAlive = HttpUtil.isKeepAlive(msg);
		String accessTokenInReq = msg.headers().get("ACCESS-TOKEN");

		embedServerExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Object responseObj = processRequest(method, uri, reqMessage, accessTokenInReq);
				String responseMessage = JSONUtil.toJsonStr(responseObj);
				writeResponse(ctx, keepAlive, responseMessage);
			}
		});
	}

	// @Override
	// public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	// ctx.flush();
	// }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			ctx.channel().close();
		}
		else {
			super.userEventTriggered(ctx, evt);
		}
	}

	private void writeResponse(ChannelHandlerContext context, boolean keepAlive, String responseJson) {
		ByteBuf byteBuf = Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8);
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
		response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
		response.headers().add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		if (keepAlive) {
			response.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		context.writeAndFlush(response);
	}

	private Object processRequest(HttpMethod method, String uri, String reqMessage, String accessTokenInReq) {
		if (method != HttpMethod.POST) {
			return new PetClinicResponse<String>(PetClinicResponse.FAIL, "http method should be post");
		}
		if (StringUtils.isBlank(uri)) {
			return new PetClinicResponse<String>(PetClinicResponse.FAIL, "uri cannot be blank");
		}
		if (!StringUtils.equals(accessTokenInReq, accessToken)) {
			return new PetClinicResponse<String>(PetClinicResponse.FAIL, "access token error");
		}
		if (uri.equals("/queryOwner")) {
			try {
				Owner ownerExample = JSONUtil.toBean(reqMessage, Owner.class);
				List<Owner> owners = petClinic.getOwner(ownerExample);
				return new PetClinicResponse<>(owners);
			}
			catch (Exception e) {
				return new PetClinicResponse<String>(PetClinicResponse.FAIL, "error:" + e.getMessage());
			}
		}
		return new PetClinicResponse<String>(PetClinicResponse.FAIL, uri + " is not valid");
	}

}
