package com.kgh.spring_batch_pass.repository.packaze;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@ActiveProfiles("test")
public class PackageRepositoryTest {
    @Autowired
    private PackageRepository packageRepository;

    @Test
    public void test_save(){
        // given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("heal pass 12 weeks");
        packageEntity.setPeriod(4);

        //when
        packageRepository.save(packageEntity);

        // then
        assertNotNull(packageEntity.getPackageSeq());


    }

    @Test
    public void test_findByCreatedAtAfter(){
        //given
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(1);

        PackageEntity packageEntity0 =new PackageEntity();
        packageEntity0.setPackageName("패키지 0:대학생 3개월");
        packageEntity0.setPeriod(90);
        packageRepository.save(packageEntity0);

        PackageEntity packageEntity1 = new PackageEntity();
        packageEntity1.setPackageName("패키지1:직장인 3개월");
        packageEntity1.setPeriod(180);
        packageRepository.save(packageEntity1);

        //when
        List<PackageEntity> packageEntities = packageRepository.findByCreatedAtAfter(dateTime, PageRequest.of(0, 1, Sort.by("packageSeq").descending()));

        //then
        assertEquals(1, packageEntities.size());
        assertEquals(packageEntity1.getPackageSeq(), packageEntities.get(0).getPackageSeq());
    }

    @Test
    public void test_updateCountAndPeriod(){
        //given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("이벤트 4개월");
        packageEntity.setPeriod(90);
        packageRepository.save(packageEntity);

        //when
        int updatedCount = packageRepository.updateCountAndPeriod(packageEntity.getPackageSeq(), 30, 120);
        final PackageEntity updatePacakgeEntity = packageRepository.findById(packageEntity.getPackageSeq()).get();

        //then
        assertEquals(1, updatedCount);
        assertEquals(30, updatePacakgeEntity.getCount());
        assertEquals(120, updatePacakgeEntity.getPeriod());

    }

    @Test
    public void test_delete(){
        //given
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("이용권 제거");
        packageEntity.setCount(1);
        PackageEntity newPackageEntity = packageRepository.save(packageEntity);

        //when
        packageRepository.deleteById(newPackageEntity.getPackageSeq());

        //then
        assertTrue(packageRepository.findById(newPackageEntity.getPackageSeq()).isEmpty());

    }

}
