#!/usr/bin/env bash
set -euo pipefail

# Готуємо директорії для логів Maven і артефактів Playwright.
mkdir -p target/ci-logs target/e2e-artifacts/videos target/e2e-artifacts/screenshots
LOG_FILE="target/ci-logs/e2e-ui-tests.log"

log() {
  echo "$1" | tee -a "$LOG_FILE"
}

# Очищаємо лог на початку кожного прогону, щоб артефакт містив лише актуальний запуск.
: > "$LOG_FILE"

# Чекаємо, поки Selenium почне стабільно відповідати на /status після старту контейнера.
for attempt in {1..90}; do
  # Читаємо статус Grid з окремими timeout, щоб короткі мережеві збої не валили весь скрипт.
  ready_response="$(
    curl --silent --show-error \
      --connect-timeout 2 \
      --max-time 10 \
      http://127.0.0.1:4444/status
  )" || ready_response=""

  # Якщо відповідь є, намагаємось витягнути прапорець готовності з JSON.
  if [[ -n "$ready_response" ]]; then
    ready="$(jq -r '.value.ready // false' <<<"$ready_response" 2>/dev/null)" || ready="false"
  else
    ready="false"
  fi

  # Коли Grid готовий, можна переходити до створення браузерної сесії.
  if [[ "$ready" == "true" ]]; then
    log "Selenium Grid reported ready on attempt ${attempt}."
    break
  fi

  # Логуємо повторну спробу, щоб у CI було видно, чи є повільний старт Selenium.
  log "Selenium Grid not ready on attempt ${attempt}."
  sleep 2
done

# Якщо навіть після всіх спроб Selenium не готовий, завершуємо прогін явною помилкою.
if [[ "${ready:-false}" != "true" ]]; then
  log "Selenium Grid did not become ready in time."
  exit 1
fi

# Відкриваємо Selenium session і дістаємо CDP endpoint для Playwright.
SESSION_ID=""
CDP_URL=""

for attempt in {1..30}; do
  # Тимчасовий файл потрібен, щоб окремо зчитати тіло відповіді і HTTP-код від curl.
  tmp_response_file="$(mktemp)"
  http_code="$(
    curl --silent --show-error \
      --connect-timeout 2 \
      --max-time 15 \
      --output "$tmp_response_file" \
      --write-out "%{http_code}" \
      --request POST \
      --url http://127.0.0.1:4444/session \
      --header "Content-Type: application/json" \
      --data '{
        "capabilities": {
          "alwaysMatch": {
            "browserName": "chrome",
            "goog:chromeOptions": {
              "args": ["--headless=new", "--disable-dev-shm-usage", "--no-sandbox"]
            }
          }
        }
      }'
  )" || http_code="curl_failed"

  # Читаємо JSON відповіді незалежно від того, успішний це статус чи ні.
  response="$(cat "$tmp_response_file")"
  rm -f "$tmp_response_file"

  if [[ "$http_code" == "200" ]]; then
    # Для успішної відповіді витягуємо id Selenium session і CDP URL браузера.
    SESSION_ID="$(jq -r '.value.sessionId // empty' <<<"$response")"
    CDP_URL="$(jq -r '.value.capabilities["se:cdp"] // empty' <<<"$response")"
    if [[ -n "$SESSION_ID" && -n "$CDP_URL" ]]; then
      log "Created Selenium session on attempt ${attempt}."
      break
    fi
    # Іноді Selenium відповідає 200, але ще не повертає повні capabilities; це теж повторюємо.
    log "Selenium session response on attempt ${attempt} did not contain session metadata."
    log "$response"
  else
    # Для неуспішного статусу залишаємо в логах HTTP-код і відповідь сервера.
    log "Selenium session creation attempt ${attempt} failed with status ${http_code}."
    if [[ -n "$response" ]]; then
      log "$response"
    fi
  fi

  # Даємо Selenium короткий час на стабілізацію перед наступною спробою.
  sleep 2
done

# Якщо не вдалося отримати session id або CDP URL, далі запускати Playwright немає сенсу.
if [[ -z "$SESSION_ID" || -z "$CDP_URL" ]]; then
  log "Unable to resolve Selenium session metadata after retries."
  exit 1
fi

cleanup() {
  if [[ -n "${SESSION_ID:-}" ]]; then
    # Закриваємо Selenium session навіть після падіння Maven, щоб не залишати "висячі" браузери.
    curl --silent --show-error --fail-with-body \
      --request DELETE \
      --url "http://127.0.0.1:4444/session/${SESSION_ID}" >/dev/null || true
  fi
}

trap cleanup EXIT

# Selenium повертає внутрішню адресу контейнера, тому переписуємо її на localhost runner-а.
CDP_PATH="${CDP_URL#ws://*/}"
export E2E_SELENIUM_CDP_URL="ws://127.0.0.1:4444/${CDP_PATH}"
export E2E_ARTIFACTS_DIR="${E2E_ARTIFACTS_DIR:-target/e2e-artifacts}"
export DEBUG="pw:api,pw:browser"

# Логуємо ключові параметри перед стартом Maven, щоб легше діагностувати CI-запуск.
log "Resolved Playwright CDP endpoint: ${E2E_SELENIUM_CDP_URL}"
log "Enabled Playwright debug namespaces: ${DEBUG}"
log "Starting Maven E2E UI test run."

# Запускаємо Maven-профіль E2E UI і одночасно пишемо консольний вивід у CI-лог.
set -o pipefail
mvn -B -P e2e-ui -Dcheckstyle.skip=true -DskipUnitTests=true -DskipIntegrationTests=true verify \
  | tee -a "$LOG_FILE"