package com.kgh.spring_batch_pass.job.pass;

import com.kgh.spring_batch_pass.repository.pass.*;
import com.kgh.spring_batch_pass.repository.user.UserGroupMappingEntity;
import com.kgh.spring_batch_pass.repository.user.UserGroupMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AddPassesTasklet implements Tasklet {

    private final PassRepository passRepository;
    private final BulkPassRepository bulkPassRepository;
    private final UserGroupMappingRepository userGroupMappingRepository;

    public AddPassesTasklet(PassRepository passRepository, BulkPassRepository bulkPassRepository, UserGroupMappingRepository userGroupMappingRepository) {
        this.passRepository = passRepository;
        this.bulkPassRepository = bulkPassRepository;
        this.userGroupMappingRepository = userGroupMappingRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        // 이용권 시작 일시 1일 전 user group 내 각 사용자에게 이용권을 추가해줍니다.
        final LocalDateTime startedAt = LocalDateTime.now().minusDays(1);
        // 처리가 안된 대량 데이터들만 가져온다.
        final List<BulkPassEntity> bulkPassEntityList = bulkPassRepository.findByStatusAndStartedAtGreaterThan(BulkPassStatus.READY, startedAt);

        int count = 0;

        for(BulkPassEntity bulkPassEntity : bulkPassEntityList){
            final List<String> userIds = userGroupMappingRepository.findByUserGroupId(bulkPassEntity.getUserGroupId())
                    .stream()
                    .map(UserGroupMappingEntity::getUserId)
                    .toList();
            count += addPasses(bulkPassEntity, userIds);
            bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
        }
        log.info("AddPasses Tasklet - execute : 이용권 {} 건 추가 완료, startedAt = {}",count,startedAt);
        return RepeatStatus.FINISHED;

    }

    private int addPasses(BulkPassEntity bulkPassEntity, List<String> userIds){
        List<PassEntity> passEntities = new ArrayList<>();
        for(String userId : userIds){
            PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);
            passEntities.add(passEntity);
        }
        return passRepository.saveAll(passEntities).size();
    }
}
