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

![Key-Value DB UML](https://www.plantuml.com/plantuml/png/RLJTZXir5BwVfpZg3JCY6-Xj54LTeFn8JLEH01V0XMVc91dgiKUsXsqAI6sHGAWNAwwvWYS8e8K8jBklu7cZUknaOcSsKgJnET_vEP_FvmTI4Q7gaaRoQS4g8aW96ILIdYibYhHM-4d6sRnuZl2k1v5halEBWgrWIQX4pqCBXcfJ8N2XrZoA2gPGB4c6y05H92KIuRi8u6rU8LkGb6BCI8aZc2jXqYKZiCQF2SifjhXC857OHmw0LwhWJEuZPkwKj36vu5K_mhfe8PLroJWn3bHZvvpOMfHBZ9Fe1xzjhn_NULMh-2bkHf1k57xvzG2-9RJ6mz5MMM40CPLSUuIEdSMMov6YmXDWoZb5mcmxl3ISuZdBvnUa2clYimepXVdHKbSvKTZFr4vZRpJWoOroGUX3rbhpeF1IRbWsOn_IMgu3HqcUVTuEs-x3UwQE8TyG5V8rLroG5JvaguBj6SiuMnOhGtYt0-zRYsKJXIoaGfPX2tXoF4u9Ctc4hOjA59czigdxF4uLfdJ63kNrrS_icgESv76U7djBB0NjxB4nK6z_WZc_48N2ypMI_15VkLIagf3bOTG0-hio4mjUfb9n5gvDIxjRlxVWpFn0_w5t-jRy_zNRvYUzQowjtMSRHK5eyVmmZUBsNkBzJixIRp1JoQ6AdKpmqc5Qc_Htr89RZ6bviKzaFljfmfNw55U6MB6n7PpvLBPdsNLeQar9DTxpuhLkrI1i5XIdz1tZHrv8SdX30dZtVfnqw3fRMn6VlZ1dTnS8bExc7q30QgXqP_ELxYo1qWEWhtGI4EtBsrawUVV2_MtvNV_LN9fTsRaj2PKu_duuF5xCuSCW9y4JDuBZovm4iF3z687_CdDdJSAQDYhKPnzmb-H3q70uwGJT1mT5J5jMpqaKoDEc29BQETr5HL6mnK6-lijNt0YwfKqiyYuwQudISmRXtUgzasFt7KMCAuHx7o4pfWpKfa89iKqnNanyuPXZ0isF-hPveM-QN_ILlcvU3xvYpS_DAtsZ_snUmnDAcFfi-WZqjRw2veL1_wst-l_cPNCv1FsRlZ7ddVvFRy6aU6dYjislpQlM3Uq1z3_wQdWFY8J7zwF7zmsLUuazG9RN9Nq3)

## Ориентировочный стек технологий

- **Язык:** Java 21 LTS   
- **Сборка:** предварительно Gradle + Gradle Wrapper  
- **Тесты:** предварительно JUnit 5  
- **Форматирование и линтинг:** предварительно Spotless  
- **Контейнеризация:** Всё окружение планируется запускать через **Docker Compose**  
- **CI/CD:** предварительно Gitlab CI/CD 
- **Документация:** PlantUML для диаграмм, Markdown для описаний, Atlassian Confluence при необходимости