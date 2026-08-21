package com.erzhena.spaceexplorer_api;

import org.springframework.data.jpa.repository.JpaRepository;

// ArticleRepository — да, бин. Но объявлен третьим способом:
// Spring Data сама находит интерфейсы, унаследованные от JpaRepository,
// и создаёт для них реализации. Ставить @Bean не надо, иначе получилось бы два.
public interface ArticleRepository extends JpaRepository<Article, Long> {
}
