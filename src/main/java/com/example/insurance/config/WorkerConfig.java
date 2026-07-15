package com.example.insurance.config;

import com.example.insurance.activity.DecisionActivityImpl;
import com.example.insurance.activity.NotificationActivityImpl;
import com.example.insurance.activity.PersistActivityImpl;
import com.example.insurance.activity.ValidateActivityImpl;
import com.example.insurance.workflow.InsuranceQuoteWorkflow;
import com.example.insurance.workflow.InsuranceQuoteWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class WorkerConfig implements SmartLifecycle {

    private WorkerFactory workerFactory;
    private volatile boolean running = false;

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient,
                                       ValidateActivityImpl validateActivity,
                                       DecisionActivityImpl decisionActivity,
                                       PersistActivityImpl persistActivity,
                                       NotificationActivityImpl notificationActivity) {
        log.info("Creating Temporal worker factory");

        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(InsuranceQuoteWorkflow.TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(InsuranceQuoteWorkflowImpl.class);
        worker.registerActivitiesImplementations(
                validateActivity,
                decisionActivity,
                persistActivity,
                notificationActivity
        );

        this.workerFactory = factory;
        log.info("Temporal worker factory created with task queue: {}", InsuranceQuoteWorkflow.TASK_QUEUE);

        return factory;
    }

    @Override
    public void start() {
        if (workerFactory != null && !running) {
            log.info("Starting Temporal worker factory");
            workerFactory.start();
            running = true;
            log.info("Temporal worker factory started");
        }
    }

    @Override
    public void stop() {
        if (workerFactory != null && running) {
            log.info("Shutting down Temporal worker factory");
            workerFactory.shutdown();
            workerFactory.awaitTermination(5, TimeUnit.SECONDS);
            running = false;
            log.info("Temporal worker factory shut down");
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;
    }
}
