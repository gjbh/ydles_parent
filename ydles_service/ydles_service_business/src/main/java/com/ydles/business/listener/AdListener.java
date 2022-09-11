package com.ydles.business.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Configuration
public class AdListener {
    @Autowired
    RestTemplate restTemplate;

    @RabbitListener(queues = "ad_update_queue")
    public void reveiveMsg(String position) {
        System.out.println("接受到了广告消息，位置:" + position);

        //发请求 http://192.168.200.128/ad_update?position=web_index_lb
        String url = "http://192.168.200.128/ad_update?position=" + position;
        restTemplate.getForObject(url, Map.class);

        ////1,创建okHttpClient对象
        //OkHttpClient okHttpClient = new OkHttpClient();
        ////2,创建一个Request
        //String url = "http://192.168.200.128/ad_update?position=" + position;
        //final Request request = new Request.Builder()
              //  .url(url)
               // .build();
        ////3,新建一个call对象
       // Call call = okHttpClient.newCall(request);
        ////4，请求加入调度，这里是异步Get请求回调
       // call.enqueue(new Callback() {
           // //失败了怎么样
         //   @Override
          //  public void onFailure(Call call, IOException e) {
        //        System.out.println("发送请求失败");
         //  }

        //    //成功了应该怎么样
          //  @Override
          // public void onResponse(Call call, Response response) throws IOException {
           //    System.out.println("发送请求成功"+response.message());
           // }});

    }
}
