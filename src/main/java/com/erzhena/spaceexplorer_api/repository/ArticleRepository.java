package com.erzhena.spaceexplorer_api.repository;

import com.erzhena.spaceexplorer_api.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

// ArticleRepository — да, бин. Но объявлен третьим способом:
// Spring Data сама находит интерфейсы, унаследованные от JpaRepository,
// и создаёт для них реализации. Ставить @Bean не надо, иначе получилось бы два.
public interface ArticleRepository extends JpaRepository<Article, Long> {

    //    select ... from article order by published_at desc limit 20 offset 0
    //    select count(*) from article
    //    Второй нужен, чтобы посчитать totalElements и totalPages в Page.
    //    То есть каждая страница стоит на один запрос дороже.
    //    Slice — он второй запрос не делает. Если общее количество клиенту не нужно, это бесплатная экономия вдвое.
    Slice<Article> findAllBy(Pageable pageable);
}
