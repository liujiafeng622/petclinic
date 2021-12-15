package org.springframework.samples.petclinic.p6spy;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

import java.text.SimpleDateFormat;
import java.util.Date;

public class P6SpyLogger implements MessageFormattingStrategy {

	@Override
	public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared,
			String sql, String url) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		return !"".equals(sql.trim()) ? format.format(new Date()) + " | took " + elapsed + "ms | " + category
				+ " | connection " + connectionId + "\n " + sql + ";" : "";
	}

}
