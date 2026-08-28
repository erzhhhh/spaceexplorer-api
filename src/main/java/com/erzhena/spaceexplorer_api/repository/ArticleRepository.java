package com.erzhena.spaceexplorer_api.repository;

import com.erzhena.spaceexplorer_api.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

// ArticleRepository — да, бин. Но объявлен третьим способом:
// Spring Data сама находит интерфейсы, унаследованные от JpaRepository,
// и создаёт для них реализации. Ставить @Bean не надо, иначе получилось бы два.
public interface ArticleRepository extends JpaRepository<Article, Long> {

    //    select ... from article order by published_at desc limit 20 offset 0
    //    select count(*) from article
    //    Второй нужен, чтобы посчитать totalElements и totalPages в Page.
    //    То есть каждая страница стоит на один запрос дороже.
    //    Slice — он второй запрос не делает. Если общее количество клиенту не нужно, это бесплатная экономия вдвое.
    //    findAllBy — это derived query: Spring Data строит SQL из имени метода. find → SELECT, All → все записи.
    //    findAllBy — после By не написано ничего. Раз условий не задано, фильтровать нечего, и в SQL секция WHERE
    //    просто не появляется. Запрос забирает все строки таблицы.
    //    Твой репозиторий расширяет JpaRepository, а там уже есть готовый метод findAll(Pageable). Он возвращает Page,
    //    а Page делает лишний COUNT(*).
    //    Тебе нужен Slice, без подсчёта. Значит, надо объявить свой метод. Но назвать его findAll нельзя — конфликт с
    //    унаследованным.
    //    findAllBy — это способ получить другое имя, не меняя смысла запроса. By без условий после него работает как
    //    пустой хвост: SQL остаётся тем же, а имя становится уникальным.
    Slice<Article> findAllBy(Pageable pageable);

    @Query(value = """
            SELECT * FROM article
            ORDER BY published_at DESC, id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Article> findLatest(@Param("limit") int limit);

    @Query(value = """
            SELECT * FROM article
            WHERE (published_at, id) < (CAST(:publishedAt AS timestamptz), :id)
            ORDER BY published_at DESC, id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Article> findOlderThan(@Param("publishedAt") Instant publishedAt,
                                @Param("id") Long id,
                                @Param("limit") int limit);
}
