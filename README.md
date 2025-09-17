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

![Key-Value DB UML](https://www.plantuml.com/plantuml/png/XLNTYYD55BxlfpXtTd2JB88N1bnsVbO8ph33nDKByQBIVP8KKrtLL5MBKHTc1_J6bGNnIaHyXBWu6aStWazG_Ge-YQSgbSnKoEB2XkfJNttVTtwgvg6nJDkc4fdbLY2yZ_FkXqmq2CU7a0-bHJrX1PeE_7lvFJmvFO5ZpIOMydDbcSKIthd_pwAJPUQ2ovffLa4Xc347rce-RYmE2oL7_7E4-vb0EcSLQ3wTMIYvni9o9RFi3NYN_i6HqWW4jveLreHOngE1O2pu6baF-o83U1FgnkONEBy7d_gTJeXDCSGwC5X7Gx14WHOtyR5I0fdqEq6FSMc8VdVxQNP7VaWBDXQ9FFSn-ixzgM7yI8yzecpc4KxqUV2uDaRh13f2pPcWKkc8Dvj07l31sjpYnv_4b6xtIsRPTjVZc9odv6Ue3JSMPO6dJBBfLcGY6ZFBGxKKBqD8A5Pk8odDHvfoFfWXAq_KD3AmkaPPvjHDfSkjYuJHp6MHQcYi1Pk_HkMOqXapWqTAJlX6nkTunkpQwyXgBgVh1h9YXdx2mWQHhDFoae_bUvlizZVr58cdC9jw4ybG7f9z4uu85IMeSUP8DTBUXfzkf_QHd7A9aFEg5bYXjCmFTPVuoM5dTuhz-53rruUYRY6KmQGKaVfnxQcOW3vC6jje16ERiTdb_40pQLZXiMblnqWgQTqBLLNSfZ6jX1Ypuk9rtU0ctkKoylhcyO9PJ7dCZEboNnxdA49Hp8pNqNrQXrxlomVf1Ty3IAzWbdn1j_jWVTVIWpw-R_O7q1WqUx39W4SOv2esehFdH3giTAH4VnyCABa7VNl9_myPPvDW-5cjlDjCAelnzLIJdNe1k1_TqhsY_t-vXVkpVKxhbRk6e_Cdnt1mDXmGfEjVpFuKRN_z8FRll7uPMG6l47N6obfLaT2U-dcrdzehzz9TjrVji_MAz4ZHVxNF1zjtXM8BSB-wPVlCtTpBOFEwkB_RIxU4S7ZHNjBlTGyEQGgRkg_H-9pyNnp2KHQ_kzzeUzUXm8alHDgpO8t8lwP2h4XyMue1kQ3r2oWRcXikk9ttFFXdSauWjoILXJz642_Qlg2abY4z2ho4jxfl0y4MxXU2VPMOITgITD8B_11yt52wL2ptglt6_K5MF3bbjN8ta7FPhR1IUhvvZzEgUPAbTmWdezDE3zntq7vBL2iYYewkmN-7L6_S2lmFALsHveAKh-wuVKYhfXB_0G00)

## Ориентировочный стек технологий

- **Язык:** Java 21 LTS   
- **Сборка:** предварительно Gradle + Gradle Wrapper  
- **Тесты:** предварительно JUnit 5  
- **Форматирование и линтинг:** предварительно Spotless  
- **Контейнеризация:** Всё окружение планируется запускать через **Docker Compose**  
- **CI/CD:** предварительно Gitlab CI/CD 
- **Документация:** PlantUML для диаграмм, Markdown для описаний, Atlassian Confluence при необходимости