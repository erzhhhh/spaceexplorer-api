package com.erzhena.spaceexplorer_api.repository;

import com.erzhena.spaceexplorer_api.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository repository;

    @Test
    void contextLoads() {
        assertThat(repository.count()).isZero();
    }
}