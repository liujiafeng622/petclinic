package org.springframework.samples.petclinic.server;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.util.HttpUtils;

import java.util.List;

public class EmbedServerTest {

	private String url = "http://192.168.3.9:10000/queryOwner";

	private String accessToken = "21e4cd40301b4461aa5f014fd2cd099d";

	private int timeout = 10;

	public static void main(String[] args) {
		EmbedServerTest embedServerTest = new EmbedServerTest();
		for (int i = 0; i < 100; i++) {
			System.out.println("第" + i + "次查询测试开始");
			embedServerTest.testQueryOwner();
			System.out.println("第" + i + "次查询测试结束");
		}
	}

	public void testQueryOwner() {
		Owner owner = new Owner();
		PetClinicResponse petClinicResponse = HttpUtils.postBody(url, accessToken, timeout, owner);
		if (petClinicResponse.getCode() == PetClinicResponse.SUCCESS) {
			List<Owner> responseList = (List<Owner>) petClinicResponse.getResponse();
			System.out.println("查询到Owner数量:" + responseList.size());
		}
		else {
			System.out.println("Failed, Error: " + petClinicResponse.getMessage());
		}
	}

}
