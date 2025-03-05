package com.kgh.spring_batch_pass.job.notification;

import com.kgh.spring_batch_pass.repository.booking.BookingEntity;
import com.kgh.spring_batch_pass.repository.booking.BookingStatus;
import com.kgh.spring_batch_pass.repository.notification.NotificationEntity;
import com.kgh.spring_batch_pass.repository.notification.NotificationEvent;
import com.kgh.spring_batch_pass.repository.notification.NotificationModelMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
public class SendNotificationBeforeClassJobConfig {

    private final int CHUNK_SIZE = 10;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final SendNotificationItemWriter sendNotificationItemWriter;

    public SendNotificationBeforeClassJobConfig(StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory, EntityManagerFactory entityManagerFactory, SendNotificationItemWriter sendNotificationItemWriter) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.sendNotificationItemWriter = sendNotificationItemWriter;
    }

    // JOB
    @Bean
    public Job sendNotificationBeforeClassJob(){
        return this.jobBuilderFactory.get("sendNotificationBeforeClassJob")
                .start(addNotificationStep())
                .next(sendNotificationStep())
                .build();
    }

    // STEP
    @Bean
    public Step addNotificationStep(){
        return this.stepBuilderFactory.get("addNotificationStep")
                .<BookingEntity, NotificationEntity>chunk(CHUNK_SIZE)
                .reader(addNotificationItemReader())
                .processor(addNotificationItemProcessor())
                .writer(addNotificationItemWriter())
                .build();
    }


    // ITEM READER
    @Bean
    public JpaPagingItemReader<BookingEntity> addNotificationItemReader(){
        return new JpaPagingItemReaderBuilder<BookingEntity>()
                .name("addNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                // pageSize: 한 번에 조회할 row 수
                .pageSize(CHUNK_SIZE)
                // 상태(status)가 준비중이며, 시작일시(startedAt)이 10분 후 시작하는 예약이 알람 대상이 됩니다.
                .queryString("select b from BookingEntity b join fetch b.userEntity where b.status = :status and b.startedAt <= :startedAt order by b.bookingSeq")
                .parameterValues(Map.of("status", BookingStatus.READY, "startedAt", LocalDateTime.now().plusMinutes(10)))
                .build();

    }
    // ITEM PROCESSOR
    @Bean
    public ItemProcessor<BookingEntity, NotificationEntity> addNotificationItemProcessor(){
        return bookingEntity -> NotificationModelMapper.INSTANCE.toNotificationEntity(bookingEntity, NotificationEvent.BEFORE_CLASS);
    }

    // ITEM WRITER
    @Bean
    public ItemWriter<NotificationEntity> addNotificationItemWriter(){
        return new JpaItemWriterBuilder<NotificationEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();

    }

    // STEP 2
    @Bean
    public Step sendNotificationStep(){
        return this.stepBuilderFactory.get("sendNotificationStep")
                .<NotificationEntity, NotificationEntity>chunk(CHUNK_SIZE)
                .reader(sendNotificationItemReader())
                .writer(sendNotificationItemWriter)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }


    // ITEM READER 2
    // 커서는 thread-safe 하지 않음. SynchronizedItemStreamReader를 사용
    @Bean
    public SynchronizedItemStreamReader<NotificationEntity> sendNotificationItemReader(){
        JpaCursorItemReader<NotificationEntity> itemReader = new JpaCursorItemReaderBuilder<NotificationEntity>()
                .name("sendNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select n from NotificationEntity n where n.event=:event and n.sent = :sent")
                .parameterValues(Map.of("event", NotificationEvent.BEFORE_CLASS, "sent", false))
                .build();
        return new SynchronizedItemStreamReaderBuilder<NotificationEntity>()
                .delegate(itemReader)
                .build();

    }
}
