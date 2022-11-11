(function() {
    // we do not have to start immediately, as the user first needs to scan
    // the qr code and make and authenticate with a wallet.
    // most of the time, this takes more than 3 seconds.
    const loginScriptMode = "%%_LNURL_AUTH_LOGIN_SCRIPT_MODE_%%" || "ERROR"; // "ANONYMOUS", "AUTHENTICATED", "ERROR"
    const loginErrorMessage = "%%_LNURL_AUTH_LOGIN_ERROR_MESSAGE_%%" || "Unknown error.";
    const initialDelayInMillis = parseInt("%%_LNURL_AUTH_INITIAL_DELAY_%%") || 3000;
    const pollingIntervalInMillis = parseInt("%%_LNURL_AUTH_POLLING_INTERVAL_%%") || 3000;
    const maxAttempts = parseInt("%%_LNURL_AUTH_MAX_ATTEMPTS_%%") || 100;
    const sessionMigrateEndpoint = "%%_LNURL_AUTH_SESSION_MIGRATION_ENDPOINT_%%";

    const migrateSession = () => {
        // using Fetch API is the only way to handle redirects properly.
        // https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch
        return fetch(sessionMigrateEndpoint, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            },
            mode: 'same-origin',
            referrerPolicy: 'no-referrer'
        }).then(res => {
            // session migration was successful if endpoint responds with 200 and an url in the body
            const success = res.type === 'basic'
                            && res.ok === true
                            && res.status === 200;

            // throw an error if session migration failed so downstream
            // can handle this case in the `catch` clause of the promise
            if (!success) {
                throw new Error('Session migration failed', { cause: res });
            }

            return res;
        }).then(res => {
          return res.json().then((body) => {
                // session migration was successful if endpoint responds with 200 and an url in the body
            const success = body['status'] === 'OK'
                          && !!body['headers']
                          && !!body['headers']['location'];

            // throw an error if session migration failed so downstream
            // can handle this case in the `catch` clause of the promise
            if (!success) {
              throw new Error('Erroneous migration response', { cause: res });
            }
            return body['headers']['location'];
        });
      });
    };

    const migrateSessionRecursive = (interval, maxAttempts, attemptCounter, onSuccess, onError) => {
        if (attemptCounter >= maxAttempts) {
            console.log('Login max attempts of ' + maxAttempts + ' reached - will stop session migration attempts.');
            onError('Login timeout.');
            return;
        }

        console.log('Login attempt: ' + attemptCounter);
        migrateSession().then(targetUrl => {
            console.log('Login success.');
            onSuccess(targetUrl);
        }).catch(error => {
            if (attemptCounter === maxAttempts) {
                console.log('Login error - giving up.');
                onError('Login timeout.');
            } else {
                console.log('Login not yet ready - continuing..');
                window.setTimeout(() => {
                    migrateSessionRecursive(interval, maxAttempts, attemptCounter + 1, onSuccess, onError);
                }, interval);
            }
        });
    };

    (function init() {
        function copyToClipboard(inputField) {
            // The `navigator.clipboard` API might not be available, e.g. on sites served over HTTP (onion).
            if (!navigator.clipboard) {
              return copyToClipboardFallback(inputField, 'Cannot copy value to clipboard');
            }

            return navigator.clipboard
              .writeText(inputField.value)
              .then(() => true)
              .catch((e) => copyToClipboardFallback(fallbackInputField, 'Cannot copy value to clipboard'))
        }

        function copyToClipboardFallback(inputField, errorMessage) {
            return new Promise((resolve, reject) => {
              inputField.select()
              const success = document.execCommand && document.execCommand('copy')
              inputField.blur()
              success ? resolve(success) : reject(new Error(errorMessage))
            })
        }

        function addClickHandlerToElements(elements, callback) {
            elements.forEach(elem => elem.addEventListener("click", callback, false));
        }

        function addClickHandlerToElementOfClass(className, callback) {
            addClickHandlerToElements(Array.from(document.getElementsByClassName(className)), callback);
        }
        function addClickHandlerToElementById(elementId, callback) {
            addClickHandlerToElements([document.getElementById(elementId)], callback);
        }

        function onLoginSuccess(targetUrl) {
            console.log('Redirecting to ' + targetUrl);
            window.location.href = targetUrl;
        }

        function onLoginError(errorMessage) {
            document.getElementById("lnurl-auth-error-message").innerText = errorMessage;
            document.body.classList.remove(loginScriptMode.toLowerCase());
            document.body.classList.add("error");
        }

        addClickHandlerToElementOfClass("js-back-button", () => window.history.back());
        addClickHandlerToElementOfClass("js-reload-button", () => window.location.reload(true));
        addClickHandlerToElementById("lnurl-auth-copy-button", (event) => {
            const lnurlInputElem = document.getElementById("lnurl-auth-lnurl-input");
            if (event.target && lnurlInputElem) {
                const formerTargetElemInnerText = event.target.innerText;
                copyToClipboard(lnurlInputElem)
                  .then(() => {
                      event.target.innerText = " 👍 ";
                      window.setTimeout(() => {
                          event.target.innerText = formerTargetElemInnerText;
                      }, 1337);
                  })
                  .catch((err) => {
                    console.error('Async: Could not copy text: ', err);
                  });
            }
        });

        if (loginScriptMode === "ANONYMOUS") {
            window.setTimeout(() => {
                window.setTimeout(() => {
                    migrateSessionRecursive(pollingIntervalInMillis, maxAttempts, 0, onLoginSuccess, onLoginError);
                }, initialDelayInMillis);
            }, 4);
        } else if (loginScriptMode === "ERROR") {
            document.getElementById("error-message").innerText = loginErrorMessage;
        }

        // add class "anonymous", "authenticated" or "error" to body element to make hidden elements appear
        document.body.classList.add(loginScriptMode.toLowerCase());
    })();
})();
