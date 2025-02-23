package com.kgh.spring_batch_pass;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
public class SpringBatchPassApplication {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public SpringBatchPassApplication(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory){
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Step passStep(){
        TaskletStep build = this.stepBuilderFactory.get("passStep")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("Execute PassStep");
                    return RepeatStatus.FINISHED;
                }).build();
        return build;
    }

    @Bean public Job passJob(){
        return this.jobBuilderFactory.get("passJob")
                .start(passStep())
                .build();
    }

    public static void main(String[] args) {
		SpringApplication.run(SpringBatchPassApplication.class, args);
	}

}
