# GoalsApp 💗

A small Spring Boot + Thymeleaf web app for tracking goals by category (Long-term, Short-term, Todos, Wish list) and generating “guidance” based on your current list.

## Features

- ✅ Register + Login (Spring Security form login)
- ✅ Goals split into categories:
    - 🎯 LONG_TERM
    - 📅 SHORT_TERM
    - ✅ TODO
    - ✨ WISH
- ✅ Add items to a category
- ✅ “Get guidance” button that generates guidance text based on your items
- ✅ Works without OpenAI credentials (stub guidance mode)

## UI Pages (Thymeleaf)

- `/` → Home page (`home.html`)
- `/login` → Login page (`login.html`)
- `/register` → Register page (`register.html`)
- `/goals/{category}` → Category page (`category.html`)

Templates in this project:
- `src/main/resources/templates/home.html`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/category.html`

The category page shows:
- a list of items
- a form to add a new item
- a “Get guidance 💗” button (disabled when there are no items)
- a guidance panel that appears only after the guidance action is triggered

(See `category.html` for the exact layout/behavior.)

## Security

Security is configured in `SecurityConfig`.

Public endpoints:
- `/`
- `/login`
- `/register`
- `/h2/**` (H2 console)

All other endpoints require authentication.

Login:
- Custom login page at `/login`
- Redirect after success: `/goals/LONG_TERM`

Logout:
- POST `/logout`
- Redirect after logout: `/login?logout`

H2 console support:
- CSRF ignored for `/h2/**`
- Frame options disabled to allow H2 console rendering

## Guidance generation (AI / Stub mode)

Guidance is provided by `GuidanceService`.

It builds a prompt from:
- the current category
- the user’s items in that category

### Stub mode (default if missing config)

If `ai.openai.base-url` or `ai.openai.key` is blank, the app returns a stubbed response that includes:
- the system prompt
- the generated prompt
- a note on how to enable real AI

### Real AI mode (OpenAI-compatible endpoint)

If both properties are set, the service sends a POST request with:
- `model` (default: `gpt-4.1-mini`)
- `input` (system prompt + category prompt)

Properties used:
- `ai.openai.base-url`
- `ai.openai.key`
- `ai.openai.model` (optional)

Example `application.properties`:

```properties
ai.openai.base-url=https://api.openai.com/v1/responses
ai.openai.key=YOUR_API_KEY
ai.openai.model=gpt-4.1-mini
