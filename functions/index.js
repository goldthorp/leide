const functions = require('firebase-functions');
const auth = require("google-auth-library");
const serviceAccount = require("./credential.json");

const googleAuthOptions = {
    scopes: ['https://www.googleapis.com/auth/cloud-platform'],
    credentials: serviceAccount
};

const googleAuth = new auth.GoogleAuth(googleAuthOptions);

 exports.getAccessToken = functions.https.onCall((request, context) => {
    return googleAuth.getClient().then(client => {
        return client.getAccessToken().then(response => {
            return response.token;
        }).then(token => {
            return client.getTokenInfo(token).then(info => {
                const expiry = info.expiry_date;
                return {token, expiry};
            });
        });
    });
 });
