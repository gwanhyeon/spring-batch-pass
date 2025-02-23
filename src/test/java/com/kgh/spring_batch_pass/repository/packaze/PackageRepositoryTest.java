package com.kgh.spring_batch_pass.repository.packaze;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

}
