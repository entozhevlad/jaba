# Аркаша: Key-Value база данных

Аркаша — это **Key-Value база данных**, построенная по модульному принципу.  
Основные компоненты и их назначение:

### Основное API
**`KeyValueStore`**  
Минимальный контракт CRUD-операций:
- `put(key, value)` — сохранить значение;
- `get(key)` — получить значение по ключу;
- `delete(key)` — удалить значение;
- `containsKey(key)` — проверить существование ключа.

### Итерация
**`IterableStore`, `Entry`**  
Интерфейсы для обхода всех ключей и значений (итератор, пары ключ–значение).

### Персистентность
**`PersistenceManager`, `WriteAheadLog`**  
Отвечают за надежность данных:
- `PersistenceManager` — сброс на диск (`flush`), загрузка при старте (`load`);
- `WriteAheadLog` (опционально) — журнал изменений для обеспечения durability.

### Конфигурация и метрики
**`DatabaseConfig`, `Metrics`**  
Позволяют настраивать и контролировать работу системы:
- параметры: путь к данным, размер кэша, флаг `fsyncOnFlush`;
- статистика: размер в байтах, количество ключей.

### Движок
**`StorageEngine`**  
Центральный интерфейс, объединяющий всё вышеперечисленное.  
Конкретная реализация (например, *in-memory* + flush на диск или LSM) подставляется на уровне движка.

---

## UML-диаграмма

![Key-Value DB UML](https://www.plantuml.com/plantuml/png/XLNTYYD55BxlfpXtTd2JB88N1bnsPqOXECCE4rSln8jAzqbIJ7LLKrKjHbsO7T0RLnR4An7n4UBYh75qHl09gb_19_5KTNSo5R8uC4tLgU-Sxpi_LNbeBDEsAaHYkHK87-2o_n4J5SBH0QHZQL7FM8Qc1_zU_01FJexXIBEPXVHCMMOnXtVk_xFg9Oavvx9acXMG2MRC8sirdrOMnvcI4_u5mlr48FbP1PhF5nPohZ6pNCaaUGFUfJyuL1g1u5Qpp9fWItWdm0iBkYRMmxvC0Dw4ihBfEIxlmMV-f1TiSmos7emQQp3cAD1YPvyg9P19VnBu69U6mkyUFqtkq8zfmQOYekVUHllKUutRJUps7cMpRE4KFWqQ6s6qZg0Jr9m9AfLkyQOpf04Vf2qjVl9fczBsF6UMRKuzZibb7FmKjU76eipmX4astv3CH6KMQQYMudam2SNoZIKEyx6cd1yjaEN7Qjv6O6M9Ca-fcqhd6nLHHBEKMSoXiHHi-HgM8qfdoWmUAZdZ7OtFyPJPHkl4QYxdJGDPja0_OU60WZHfUSh7ylqkk_rDFK6AavcktXHaB0z8lWakGhKKr3Xpg2ffj-QdtTHIX-TS8gIyA0KMA2tpGzsd-AImjpl5Vdoe-erJn-jlO5oUJ2Y3MzEMxN7fepC1GvXLjj98tjNKxD9yg9aqBFFOkDrJ9E8TBbKKV8TVAo6cB3j_NOEuQQztJd6xVlACMOpZc0NJ-Ruypb24Efa5BrlrSMa6WwyUn7T-3o2-bKcqWtx_GNFzOaTlttSTHb0PD7kmySmtC4XLsuZU7ezuViabHtz532Yv1xszz_-7xCPr1FXvgRpQH2gBxOEgPZlr0d0_kRMxeV-_tChzMJ-dzQsxWiEp9qVmw7Gy8aZVFwB3ETfXyqOExpo82Ka1pz3oJ9MrgY2YFVNpR3_Nb-wbkwelwsVDYlY8qU_gvwFDKqEs5RZVtBf-vgxl9T0zEExl-iAj8JYlwWlwNWtWWAQmAeSQZS_9_mWHZhButRsYurs50cU-477FWZGA_erRa-FDjXGZKa7h5v1ND3TSSBiSUF0lf9n0RaqiA-z644zQlw2arY4zCho4j_fl0y5MxbU2VHs9YTeITT8J_1XqN5EwL2ntKt_h_l1IABXxHKVNa7BPBx1GUjazqN7LV92rLmZ7avFU0DptK7z7eMufKAle2lm-f7hjRi5_YEcIE5V4V7b7xKDQLONu3m00)

## Ориентировочный стек технологий

- **Язык:** Java 21 LTS   
- **Сборка:** предварительно Gradle + Gradle Wrapper  
- **Тесты:** предварительно JUnit 5  
- **Форматирование и линтинг:** предварительно Spotless  
- **Контейнеризация:** Всё окружение планируется запускать через **Docker Compose**  
- **CI/CD:** предварительно Gitlab CI/CD 
- **Документация:** PlantUML для диаграмм, Markdown для описаний, Atlassian Confluence при необходимости