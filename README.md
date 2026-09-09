# Описание
- Бэкенд приложения-блога на Spring Framework
- REST API для готового фронтенда
- Учебный проект 3-его спринта

## Стек

- Java 21
- Spring Framework 6.2 (без Spring Boot)
- Spring Data JDBC
- H2
- Maven
- Tomcat 10.1
- JUnit 5

## Сборка

```
mvn clean package
```

После сборки war-файл лежит в `target/my-blog-back-app-1.0-SNAPSHOT.war`

## Тесты

```
mvn test
```

- Тесты репозиториев, сервиса и контроллеров
- Схема таблиц накатывается из `schema.sql` при старте контекста
- Все тестовые классы используют одну конфигурацию контекста

## Деплой

- Собрать проект: `mvn clean package`
- Остановить Tomcat: `bin/shutdown.bat`
- Удалить из `webapps` папку `ROOT` и файл `ROOT.war`
- Скопировать war из `target` в `webapps` под именем `ROOT.war`
- Запустить Tomcat: `bin/startup.sh`


## Запуск и использование

- Бэкенд — Tomcat на порту 8080
- Фронтенд — готовый docker-контейнер на порту 80, открывается по адресу `http://localhost`


## API

Посты:

- `GET /api/posts?search=&pageNumber=1&pageSize=10` — лента постов с поиском и пагинацией
- `GET /api/posts/{id}` — один пост
- `POST /api/posts` — добавить пост
- `PUT /api/posts/{id}` — изменить пост
- `DELETE /api/posts/{id}` — удалить пост
- `POST /api/posts/{id}/likes` — добавить лайк
- `GET /api/posts/{id}/image` — картинка поста
- `PUT /api/posts/{id}/image` — загрузить картинку

Комментарии:

- `GET /api/posts/{postId}/comments` — комментарии поста
- `GET /api/posts/{postId}/comments/{id}` — один комментарий
- `POST /api/posts/{postId}/comments` — добавить комментарий
- `PUT /api/posts/{postId}/comments/{id}` — изменить комментарий
- `DELETE /api/posts/{postId}/comments/{id}` — удалить комментарий
