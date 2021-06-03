package com.alekseytyan.infotable.bean;

import com.alekseytyan.infotable.reader.PropertyReader;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@WebListener
public class Listener implements ServletContextListener {

   private static final Logger logger = LoggerFactory.getLogger(Listener.class);

   @Inject
   private PushBean pushBean;

   @SneakyThrows
   @Override
   public void contextInitialized(ServletContextEvent event) {

      logger.info("Startup");
      logger.info(" [*] Waiting for messages. ");

      String projectId = PropertyReader.getPropValue("PROJECT_ID_ENV");
      String subscriptionId = PropertyReader.getPropValue("SUBSCRIPTION_ID");

      logger.info("Project id: " + projectId);
      logger.info("Subscription id: " + subscriptionId);

      new Thread(() -> {

         ProjectSubscriptionName subscriptionName =
                 ProjectSubscriptionName.of(projectId, subscriptionId);

         while (true) {

            // Instantiate an asynchronous message receiver.
            MessageReceiver receiver =
                    (PubsubMessage message, AckReplyConsumer consumer) -> {
                       // Handle incoming message, then ack the received message.
                       logger.info("Id: " + message.getMessageId());
                       logger.info("Data: " + message.getData().toStringUtf8());

                       consumer.ack();

                       pushBean.sendMessage("update");
                    };

            Subscriber subscriber = null;
            try {
               subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
               // Start the subscriber.
               subscriber.startAsync().awaitRunning();
               System.out.printf("Listening for messages on %s:\n", subscriptionName);
               // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
               subscriber.awaitTerminated(30, TimeUnit.SECONDS);
            } catch (TimeoutException timeoutException) {
               // Shut down the subscriber after 30s. Stop receiving messages.
               subscriber.stopAsync();
            }
         }
      }).start();

   }
}