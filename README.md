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

![Key-Value DB UML](https://www.plantuml.com/plantuml/png/fLPVR-D447_tfvWt7D4a28cNY3jTsnnIHQjMxHqy81usyIInwknQk-kxC-MaNgNZXKEL44y88SGd22TwX1ujNy7-2dmIPcp7yUPSyK2bHlRCxFn-ysTc_S1OeMqoZpmRsWZXKqoxduaeGHZkWBydBUg96AFfm3_dFy3ZWtqOQZ6nu2Tn82m67Sypfw6CXHPp64V2c6rhTJXABEwDbJm9lqBumFFUWtlq1xjA8v3OQZ6s1lpTuyV3Jg7omXKKKoWOd5YsFlC0teSuiVuffblmX3MTGZR5GjQ1GIajX056Q74b7oaLeP2iAM15A0svtrG_znhm9wX34H5lNM6RbS0lmVbWQl6BBol0jPuo8cejsmcPkiwFK9lGM9HZF11IJ6kGIPIOcL_4eiAW44LA1BN4TVEv3Yrkpr04-sfQUH1nZ3BmDOwL3ceMZaUJoh6BeJ6EH7e7of326Gc3kqfEmXKCntWaRCdrX4ejfsLwnNY6NEv2GKxAi1ZoK7vIHzUQyWCaFsEpoZSvsPCxHDyKHo9LGL3LpAvAf5sBdzUjDHpkGn-acAE9kM71VoH64HgdlPfHLMYAibPO-dosZeaGt4OiWzGeBBQORv4V6offI5Ae3yjN9zc1Ld4Rb5DqwewIU9bWj1-NLlTB0f4ow9Ihc3a7imhigOWUIXO7pY2uDTbGpSMpCc8gGL60gfJhD3zAO-nET4Yj5gNmL7FNQL1FK7F3SThBH8W8I0NKNAYh4ZIO7kEKujHfGRKBmGW6TLrOifuvpHGEH5nbOQindcL6TE4AjK-desP1rYvOMByusURYRnpih2guaToAwanGuDZI7Dsx0DlsLyt1NMD2flzhYLKrUYYdeKJmmtaSuHpf15UaIztFyMtE0mSWfbWTEcjhimgPba0XS_T26Tzwhx59ovuh-wiST3PP3Ns3z-6gS_emIMoY4OnDHkQT0GvDTSC4S5PliN0ix0PbCnFQtQ0Lq356HKhCB8mhzsuMUhslxwyRijuOtUvzj-umQEoFZcFeZkJ0MHsgMWwU2yl7MZ97XnE3fiNMlGROh3c0dPOJRkdeI81yQnXGiiLwtSJ_PLaLaypmMQoOhUSCkWFUedAGwf7kb9QjUUTOQxNhO3s9_SRjx7bIMGGTJcSMrEIT0Sv-ofRP3Vt_bIsoj_ahUhxDhe0_KM3xQ6_0CyvptAU1xPSJsc-CeqV90KQeS4RAMZLde3fiu4lAwOH-VINqruk_NxTKZsdzIdH-ozvco_oRR07trbSSTJWn_9l4osm9-Slyd2nliYiE8h_itKceVJYpd_EBx7LsbL_aB_Y9_N0A-2r_DQW_K4YsWEntWdYHNMzviFfC8IBdJ8CFB_9p-htgmOuOdoPnNwFX9FCd9DaHwJ-oDwJUP1ZXX2lZjdL1hOhobi3hsWo81JrVGf1eCGgZqAP5lduXvWlE5w4i-1YPC6X-IK4jEJmMl8OFkny1cL5ooUobGyRf4wVP6U37WiSrXKl9ocxoRxC_cGevpzwGwXhyK7RdE5SwNNtOkLbZ9qjc2Fid1vqUPDz3_XsvkYL75QChuFSYrEli5lY7a2wun8HyqM3xW9wIUVGl)

## Ориентировочный стек технологий

- **Язык:** Java 21 LTS   
- **Сборка:** предварительно Gradle + Gradle Wrapper  
- **Тесты:** предварительно JUnit 5  
- **Форматирование и линтинг:** предварительно Spotless  
- **Контейнеризация:** Всё окружение планируется запускать через **Docker Compose**  
- **CI/CD:** предварительно Gitlab CI/CD 
- **Документация:** PlantUML для диаграмм, Markdown для описаний, Atlassian Confluence при необходимости