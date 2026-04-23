# CV Bot 🤖

Telegram üzərindən peşəkar CV yaradan bot.

## 🚀 Xüsusiyyətlər

- 5 fərqli CV şablonu
- Gemini AI ilə mətn polish
- PDF export (Puppeteer)
- Azərbaycan dilində tam dəstək

## 🛠 Texniki Stack

- Java 21 + Spring Boot 3.4.5
- PostgreSQL
- Telegram Bot API
- Puppeteer (Node.js) → PDF
- Gemini AI API

## ⚙️ Quraşdırma

### Tələblər
- Java 21+
- Node.js + Puppeteer
- PostgreSQL

### Environment Variables
DATABASE_URL=jdbc:postgresql://localhost:5432/cvbot
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=...
TELEGRAM_BOT_TOKEN=...
TELEGRAM_BOT_USERNAME=cv_az_bot
GEMINI_API_KEY=...
STRIPE_SECRET_KEY=...
STRIPE_WEBHOOK_SECRET=...
PUPPETEER_SCRIPT_PATH=...

## 📱 İstifadə

Telegram-da @cv_az_bot botunu tap və /start yaz.
