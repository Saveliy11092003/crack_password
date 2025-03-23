# CrackHash

**CrackHash** - это распределенная система, которая по MD-5 хэшу пароля и его максимальной длине возвращает пароль.

# Общая логика системы:
1. В рамках системы существует **Менеджер**, который принимает от пользователя запрос, содержащий MD-5 хэш пароля и его максимальную длину.
2. **Менеджер** обрабатывает запрос и генерирует задачи в соответствии с заданным числом воркеров на поиск возможного пароля. После чего отправляет их на исполнение **воркерами**. 
3. Каждый **воркер** принимает задачу, перебирает слова в заданном диапазоне и вычисляет их хэш. Находит слова у которых хеш совпадает с заданным, и результат работы возвращает **менеджеру**.

## 🚀 Запуск системы

### 1. Клонирование репозитория:
```sh
git clone https://github.com/Saveliy11092003/crack_password.git
```

### 2. Запуск фронтенда:
- Открыть проект **client-crack-password** в WebStorm
- Запустить команду:
```sh
npm start
```

### 3. Сборка и запуск бэкенда:
- Открыть проекты **crack-manager** и **worker** в IntelliJ IDEA
- Выполнить команду для сборки:
```sh
gradle bootJar
```
- В проекте **worker** перейти в директорию `deploy` и запустить команду:
```sh
docker compose -f docker-compose.yml up -d
```

### 4. Использование системы:
- Открыть браузер и перейти на `http://localhost:3000`
- Ввести MD5-хэш и максимальную длину пароля
- Нажать кнопку **Crack Hash**
- Наблюдать прогресс на **Progress Bar**
- Для проверки статуса нажать на кнопку **Check Status**
- Когда процесс завершится (100%), по нажатию на кнопку **Check Status** отобразится список возможных паролей

## Реализация
- **Backend**: Spring Boot
- **Frontend**: ReactJS
- **Компоненты системы**:
  - **Клиент** (React-приложение)
  - **Менеджер** (Spring Boot)
  - **Воркеры** (Spring Boot)

 ## 📡 API менеджера
 **Менеджер** предоставляет клиенту REST API в формате JSON для взаимодействия с ним.

### Запрос на взлом хэша
#### **POST /api/hash/crack**
**Пример запроса нахождения пароля bot23:**
```json
{
    "hash": "c139e429f817751f826f83b9f5b2fdef",
    "maxLength": 5
}
```
**Пример ответа:**
```json
{
    "requestId": "730a04e6-4de9-41f9-9d5b-53b88b17afac"
}
```

### Получение статуса запроса
#### **GET /api/hash/status?requestId=730a04e6-4de9-41f9-9d5b-53b88b17afac**

🔹 **Если запрос ещё обрабатывается:**
```json
{
    "status": "IN_PROGRESS",
    "data": null
}
```

🔹 **Если запрос выполнен:**
```json
{
   "status": "READY",
   "data": ["bot23"]
}
```

🔹 **Если произошла ошибка :**
```json
{
   "status": "ERROR",
   "data": []
}
```

🔹 **Если один из воркеров упал, т.е. ответ неполный:**
```json
{
   "status": "PARTIAL_ERROR",
   "data": [bot23]
}
```

### Получение ответа от воркера (формат сообщения - XML)
#### **PATCH** /internal/api/manager/hash/crack/request  
Для обработки ответов от **воркера** в **менеджере** реализован контроллер.  
Сообщение формируется на основе [xsd-схемы](https://drive.google.com/file/d/14tliqQQCTxg0HwrdPyxVQjVQYMC0o8zl/view).  

## ⚡ API воркера  
**Воркер** предоставляет **менеджеру** REST API в формате JSON для взаимодействия.  

### Обработка сообщений от менеджера  

#### **POST** /internal/api/worker/hash/crack/task  
Сообщение генерируется на основе [xsd-схемы](https://drive.google.com/file/d/1tC1Ji5YNzedfVET9jR5GrcNez4ipaaNk/view).  
В качестве алфавита **воркер** использует строчные латинские буквы и цифры.   

### 🔢 Генерация слов  
- Для генерации слов по заданному алфавиту используется библиотека `combinatoricslib3`.  
- Расчет диапазона слов выполняется на основе значений `PartNumber` и `PartCount` из запроса от менеджера, равномерно распределяя пространство слов между всеми воркерами.  
