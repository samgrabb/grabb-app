/**
 * OneSignal Push Notifications Init
 * grabb.ch App
 */

document.addEventListener('deviceready', function() {
    // OneSignal App ID (same as grabb.ch web)
    const ONESIGNAL_APP_ID = '695cd630-8904-4044-962a-012f52f667ef';

    if (window.plugins && window.plugins.OneSignal) {
        window.plugins.OneSignal.initialize(ONESIGNAL_APP_ID);
        
        // Request permission
        window.plugins.OneSignal.Notifications.requestPermission(true).then((accepted) => {
            console.log("Push permission accepted: " + accepted);
        });

        // Handle notification clicks
        window.plugins.OneSignal.Notifications.addEventListener('click', (event) => {
            console.log('Notification clicked:', event);
            // Open URL if provided
            if (event.notification.launchURL) {
                window.location.href = event.notification.launchURL;
            }
        });

        console.log('OneSignal initialized for grabb.ch');
    } else {
        console.log('OneSignal plugin not available');
    }
}, false);
