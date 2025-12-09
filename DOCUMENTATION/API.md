# 📡 API.js - Полная документация

## 📋 Оглавление
1. [Что такое api.js](#что-такое-apijs)
2. [Структура файла](#структура-файла)
3. [Управление токеном](#управление-токеном)
4. [Interceptors (перехватчики)](#interceptors-перехватчики)
5. [Цепочка действий](#цепочка-действий)
6. [Примеры использования](#примеры-использования)
7. [Обработка ошибок](#обработка-ошибок)

---

## Что такое api.js?

**api.js** — это центральный файл для работы со всеми HTTP запросами к серверу.

**Основные функции:**
- ✅ Создание HTTP клиента (axios)
- ✅ Управление JWT токеном
- ✅ Автоматическое добавление токена к запросам
- ✅ Обработка ошибок (особенно 401)
- ✅ Логирование запросов/ответов для отладки

**Без api.js пришлось бы:**
```javascript
// ❌ Плохо
const token = localStorage.getItem('token');
const response = await fetch('http://localhost:8080/api/orders', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(data)
});
```

**С api.js это просто:**
```javascript
// ✅ Хорошо
const response = await api.post('/api/orders', data);
// Токен добавляется автоматически!
```

---

## Структура файла

### 1. Импорт и создание клиента
```javascript
import axios from "axios";

const BASE = import.meta.env.VITE_API_BASE || "/api";
const api = axios.create({
    baseURL: BASE,                              // http://localhost:8080/api
    headers: { "Content-Type": "application/json" },
    timeout: 10000,                             // таймаут 10 сек
    withCredentials: false,
});
```

| Параметр | Значение | Объяснение |
|----------|----------|-----------|
| `baseURL` | `http://localhost:8080/api` | Базовый путь для всех запросов |
| `headers` | `Content-Type: application/json` | Все данные в JSON формате |
| `timeout` | `10000 мс` | Если нет ответа за 10 сек → ошибка |
| `withCredentials` | `false` | Не отправляем cookies (используем JWT) |

---

## Управление токеном

### Функция: `setToken(token)`
**Назначение:** Сохранить токен в localStorage

```javascript
export function setToken(token) {
    localStorage.setItem('token', token);
}
```

**Когда использовать:**
```javascript
// В Login.jsx после успешного логина
const response = await api.post('/auth/login', {email, password});
const token = response.data;  // "eyJhbGci..."
setToken(token);              // Сохраняем
```

**Где хранится:** `localStorage['token']` — локальный диск браузера

---

### Функция: `getToken()`
**Назначение:** Получить токен из localStorage

```javascript
export function getToken() {
    return localStorage.getItem('token');
}
```

**Возвращает:**
- `"eyJhbGci..."` — если токен сохранен
- `null` — если токена нет

**Когда использовать:** Автоматически в request interceptor

---

### Функция: `removeToken()`
**Назначение:** Удалить токен (выход из аккаунта)

```javascript
export function removeToken() {
    localStorage.removeItem('token');
}
```

**Когда использовать:**
```javascript
// При выходе пользователя
const handleLogout = () => {
    removeToken();
    window.location.href = '/login';
};

// ИЛИ автоматически при 401 ошибке (в response interceptor)
```

---

## Interceptors (перехватчики)

### Request Interceptor (перехватчик запросов)

**Назначение:** Автоматически добавлять токен к КАЖДОМУ запросу

```javascript
api.interceptors.request.use((config) => {
    // 1. Логируем запрос (для отладки)
    console.info('[API REQUEST]', {
        method: config.method,
        url: config.url,
        data: config.data,
        headers: config.headers,
    });

    // 2. ГЛАВНОЕ: добавляем токен
    const token = getToken();
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }

    // 3. Возвращаем модифицированный запрос
    return config;
}, (err) => Promise.reject(err));
```

**Как это работает:**

```
Шаг 1: Код вызывает api.get('/api/profile')
           ↓
Шаг 2: Interceptor ПЕРЕХВАТЫВАЕТ конфиг
           ↓
Шаг 3: Достает токен: getToken() → "eyJhbGci..."
           ↓
Шаг 4: Добавляет заголовок: Authorization: Bearer eyJhbGci...
           ↓
Шаг 5: Отправляет запрос на сервер с токеном ✅
```

---

### Response Interceptor (перехватчик ответов)

**Назначение:** 
- Логировать успешные ответы
- Обработать 401 ошибку (истек токен)
- Обработать другие ошибки

```javascript
api.interceptors.response.use(
    // Успешный ответ (200-299)
    (response) => {
        console.info('[API RESPONSE]', {
            status: response.status,
            data: response.data,
        });
        return response;
    },
    // Ошибка (400, 401, 500 и т.д.)
    (error) => {
        const status = error?.response?.status;

        if (status === 401) {
            // СПЕЦИАЛЬНАЯ ОБРАБОТКА: токен истек
            removeToken();                      // Удаляем токен
            window.location.href = '/login';    // Редирект на логин
            console.warn('Токен истек!');
        } else {
            // Другие ошибки
            console.error('API error:', status, error.message);
        }
        
        return Promise.reject(error);  // Пробрасываем ошибку
    }
);
```

**Важно:** `Promise.reject(error)` пробрасывает ошибку дальше, чтобы компонент смог её обработать:

```javascript
try {
    const response = await api.get('/api/orders');
} catch (error) {
    // Сюда попадает ошибка из interceptor
    console.error('Ошибка:', error);
}
```

---

## Цепочка действий

### Сценарий 1: Успешный логин

```
┌─────────────────────────────────────────────────────┐
│ 1. Login.jsx: Пользователь вводит email + пароль   │
└────────────────┬────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────────────────┐
│ 2. api.post('/auth/login', {email, password})       │
│    Request Interceptor: нет токена → отправляем     │
└────────────────┬────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────────────────┐
│ 3. Сервер проверяет email + password                │
│    ✅ OK → возвращает токен: "eyJhbGci..."          │
└────────────────┬────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────────────────┐
│ 4. Response Interceptor: статус 200                 │
│    Логирует и возвращает ответ                      │
└────────────────┬────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────────────────┐
│ 5. Login.jsx получает ответ                         │
│    const token = response.data                       │
│    setToken(token) ← сохраняем в localStorage       │
└────────────────┬────────────────────────────────────┘
                 ↓
         ✅ Успех! Пользователь логин
```

---

### Сценарий 2: Защищенный запрос (с токеном)

```
┌──────────────────────────────────────────┐
│ 1. Order.jsx: api.post('/orders', data)  │
└────────────┬─────────────────────────────┘
             ↓
┌──────────────────────────────────────────────────────┐
│ 2. Request Interceptor перехватывает                 │
│    - getToken() → "eyJhbGci..."                      │
│    - Добавляет заголовок: Authorization: Bearer ...  │
│    - Возвращает модифицированный запрос              │
└────────────┬──────────────────────────────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 3. Сервер получает запрос с токеном      │
│    Проверяет подпись токена              │
│    ✅ OK → обрабатывает заказ             │
│    Возвращает: {orderId: 123, ...}       │
└────────────┬──────────────────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 4. Response Interceptor                  │
│    Статус 200 → логирует и возвращает    │
└────────────┬──────────────────────────────┘
             ↓
    ✅ Order.jsx получает данные
```

---

### Сценарий 3: Токен истек (401 ошибка)

```
┌──────────────────────────────────────────┐
│ 1. Profile.jsx: api.get('/profile')      │
│    Токен уже 7 минут (истек за 6 мин)   │
└────────────┬──────────────────────────────┘
             ↓
┌──────────────────────────────────────────────────────┐
│ 2. Request Interceptor                               │
│    - getToken() → "eyJhbGci..." (старый)             │
│    - Добавляет заголовок                             │
└────────────┬──────────────────────────────────────────┘
             ↓
┌──────────────────────────────────────────┐
│ 3. Сервер проверяет токен                │
│    ❌ Ошибка: токен истек!                │
│    Возвращает: 401 Unauthorized           │
└────────────┬──────────────────────────────┘
             ↓
┌──────────────────────────────────────────────────────┐
│ 4. Response Interceptor перехватывает ошибку         │
│    status === 401 → TRUE                             │
│    - removeToken() ← удаляем токен                   │
│    - window.location.href = '/login' ← редирект      │
│    - console.warn('Токен истек!')                    │
└────────────┬──────────────────────────────────────────┘
             ↓
        Пользователь видит Login страницу
        Может заново логиниться ✅
```

---

## Примеры использования

### Пример 1: Логин

```javascript
// Login.jsx
import api, { setToken } from '../api';

async function handleLogin(email, password) {
    try {
        const response = await api.post('/auth/login', {
            email,
            password
        });
        
        // Получаем токен
        const token = response.data;  // "eyJhbGci..."
        
        // Сохраняем в localStorage
        setToken(token);
        
        // Редирект в панель
        window.location.href = '/dashboard';
        
    } catch (error) {
        console.error('Ошибка входа:', error.message);
        alert('Неверный email или пароль');
    }
}
```

**Что происходит:**
1. Отправляем email + пароль
2. Request Interceptor добавит? Нет, это первый запрос, токена еще нет
3. Сервер проверяет и возвращает токен
4. Мы сохраняем через `setToken()`
5. Response Interceptor логирует
6. Редирект на dashboard

---

### Пример 2: Защищенный запрос (создание заказа)

```javascript
// Order.jsx
import api from '../api';

async function createOrder(orderData) {
    try {
        const response = await api.post('/api/orders', {
            recipientName: orderData.name,
            address: orderData.address,
            phone: orderData.phone
        });
        
        console.log('Заказ создан:', response.data);
        // response.data = {orderId: 123, status: "CREATED", ...}
        
    } catch (error) {
        console.error('Ошибка создания заказа:', error.message);
    }
}
```

**Что происходит:**
1. Вызываем `api.post('/api/orders', data)`
2. **Request Interceptor** перехватывает:
   - `getToken()` → "eyJhbGci..."
   - Добавляет: `Authorization: Bearer eyJhbГci...`
3. Сервер получает запрос с токеном
4. Проверяет токен ✅ OK
5. Обрабатывает и создает заказ
6. Возвращает {orderId, status, ...}
7. **Response Interceptor** логирует
8. Наш код получает результат

---

### Пример 3: Выход (logout)

```javascript
// Header.jsx
import { removeToken } from '../api';

function handleLogout() {
    removeToken();  // Удаляем токен из localStorage
    window.location.href = '/login';  // Редирект на логин
}
```

**Что происходит:**
1. `removeToken()` удаляет токен из localStorage
2. Следующие запросы уже не будут иметь токена (401)
3. Пользователь видит Login страницу

---

## Обработка ошибок

### Автоматическая обработка (в interceptor)

```javascript
// Когда статус 401 → автоматически:
if (status === 401) {
    removeToken();
    window.location.href = '/login';
}
```

### Ручная обработка (в компоненте)

```javascript
try {
    const response = await api.get('/api/protected-data');
    console.log(response.data);
} catch (error) {
    // Сюда попадают все ошибки
    // Включая 401 (уже обработанный в interceptor)
    
    if (error.response?.status === 400) {
        alert('Ошибка валидации: ' + error.response.data.message);
    } else if (error.response?.status === 500) {
        alert('Ошибка сервера. Попробуйте позже.');
    } else {
        alert('Неизвестная ошибка: ' + error.message);
    }
}
```

---

## Полезные команды

### Тестирование в консоли браузера

```javascript
// Проверить наличие токена
localStorage.getItem('token')
// → "eyJhbGci..." или null

// Проверить соединение с сервером
window.api.pingBackend()
// → {path: '/health', status: 200, data: {...}}

// Добавить/удалить token (для тестирования)
localStorage.setItem('token', 'fake-token-for-testing')
localStorage.removeItem('token')
```

---

## Таблица всех методов

| Метод | Параметры | Возвращает | Описание |
|-------|-----------|-----------|---------|
| `setToken(token)` | string | void | Сохраняет токен в localStorage |
| `getToken()` | - | string\|null | Получает токен из localStorage |
| `removeToken()` | - | void | Удаляет токен из localStorage |
| `api.get(url, config?)` | string | Promise | GET запрос |
| `api.post(url, data, config?)` | string, any | Promise | POST запрос |
| `api.put(url, data, config?)` | string, any | Promise | PUT запрос |
| `api.delete(url, config?)` | string | Promise | DELETE запрос |
| `setWithCredentials(flag)` | boolean | void | Включить/выключить cookies |
| `pingBackend()` | - | Promise | Проверить соединение |

---

## Переменные окружения

В файле `.env` или `vite.config.js`:

```env
VITE_API_BASE=http://localhost:8080/api
```

Или для production:
```env
VITE_API_BASE=https://api.example.com/api
```

---

## Резюме

**api.js это:**
- 🔐 Менеджер JWT токена
- 📡 HTTP клиент с базовым URL
- 🔄 Автоматическое добавление токена к запросам
- 🛡️ Обработка 401 ошибок
- 📊 Логирование для отладки

**Основной поток:**
1. **Login** → сохраняем токен через `setToken()`
2. **Запросы** → interceptor автоматически добавляет токен
3. **401 ошибка** → interceptor удаляет токен и редирект на /login
4. **Logout** → удаляем токен через `removeToken()`

**Помните:** Всегда используйте `api.get()`, `api.post()` и т.д. вместо обычного `fetch()`, чтобы токен добавлялся автоматически!
