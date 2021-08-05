
// we do not have to start immediately, as the user first needs to scan
// the qr code and make and authenticate with a wallet.
// most of the time, this takes more than 3 seconds.
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
    }).then((res) => {
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
    }).then((res) => {
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
    }).then((targetUrl) => {
      console.log('Redirecting to ' + targetUrl);
      window.location.href = targetUrl;
      return targetUrl;
    }).then((foo) => res);
  });
};

const migrateSessionRecursive = (interval, maxAttempts, attemptCounter) => {
    if (attemptCounter >= maxAttempts) {
        console.log('Login max attempts of ' + maxAttempts + ' reached - will stop session migration attempts.');
        return;
    }

    console.log('Login attempt: ' + attemptCounter);
    migrateSession().then((res) => {
        console.log('Login success - should be redirecting to saved page or "loginSuccessUrl"');
    }).catch((error) => {
        if (attemptCounter === maxAttempts) {
            console.log('Login error - giving up.');
        } else {
            console.log('Login not yet ready - continuing..');
            window.setTimeout(() => {
                migrateSessionRecursive(interval, maxAttempts, attemptCounter + 1);
            }, interval);
        }
    });
};

window.setTimeout(() => {
    migrateSessionRecursive(pollingIntervalInMillis, maxAttempts, 0);
}, initialDelayInMillis);
