package com.adityachandel.booklore.quartz;

import com.adityachandel.booklore.exception.ApiError;
import com.adityachandel.booklore.model.dto.request.MetadataRefreshRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.KeyMatcher;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSchedulerService {

    private final Scheduler scheduler;
    private final BlockingQueue<MetadataRefreshRequest> jobQueue = new LinkedBlockingQueue<>();
    private boolean isJobRunning = false;

    public synchronized void scheduleMetadataRefreshV2(MetadataRefreshRequest request) {
        log.info("Received request to schedule metadata refresh: {}", request);
        jobQueue.offer(request);
        log.debug("Added request to job queue. Queue size: {}", jobQueue.size());
        processQueue();
    }

    private synchronized void processQueue() {
        if (isJobRunning) {
            log.debug("A job is already running. Queue processing is paused.");
            return;
        }
        if (jobQueue.isEmpty()) {
            log.debug("Job queue is empty. Nothing to process.");
            return;
        }

        isJobRunning = true;
        MetadataRefreshRequest request = jobQueue.poll();
        log.info("Processing job from queue. Remaining queue size: {}", jobQueue.size());
        if (request != null) {
            String jobId = generateUniqueJobId(request);
            try {
                log.info("Scheduling job with ID: {}", jobId);
                scheduleJob(request, jobId);
            } catch (Exception e) {
                isJobRunning = false;
                log.error("Failed to schedule job with ID: {}. Error: {}", jobId, e.getMessage(), e);
                throw e;
            }
        }
    }

    private <T> void scheduleJob(T request, String jobId) {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("request", request);

            JobDetail jobDetail = JobBuilder.newJob(RefreshMetadataJob.class)
                    .withIdentity(jobId, "metadataRefreshJobGroup")
                    .usingJobData(jobDataMap)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withIdentity(jobId, "metadataRefreshJobGroup")
                    .startNow()
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Job scheduled successfully with ID: {}", jobId);

            scheduler.getListenerManager().addJobListener(new JobListener() {
                @Override
                public String getName() {
                    return "JobCompletionListener";
                }

                @Override
                public void jobToBeExecuted(JobExecutionContext context) {
                    log.debug("Job is about to be executed. JobKey: {}", context.getJobDetail().getKey());
                }

                @Override
                public void jobExecutionVetoed(JobExecutionContext context) {
                    log.warn("Job execution was vetoed. JobKey: {}", context.getJobDetail().getKey());
                }

                @Override
                public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
                    log.info("Job executed successfully. JobKey: {}", context.getJobDetail().getKey());
                    if (jobException != null) {
                        log.error("Job execution encountered an error: {}", jobException.getMessage(), jobException);
                    }
                    isJobRunning = false;
                    log.debug("Job completion handled. Processing next job in the queue.");
                    processQueue();
                }
            }, KeyMatcher.keyEquals(jobDetail.getKey()));

        } catch (SchedulerException e) {
            log.error("Error while scheduling job with ID: {}. Error: {}", jobId, e.getMessage(), e);
            throw ApiError.SCHEDULE_REFRESH_ERROR.createException(e.getMessage());
        }
    }

    private String generateUniqueJobId(MetadataRefreshRequest request) {
        String jobId = "metadataRefreshJob_" + System.currentTimeMillis();
        log.debug("Generated unique job ID: {}", jobId);
        return jobId;
    }
}