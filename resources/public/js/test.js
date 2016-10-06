var page = require('webpage').create();
var url = require('system').args[1];

page.onConsoleMessage = function (message) {
    console.log(message);
};

page.onCallback = function (data) {
    if (data && data.hasOwnProperty("exit")) {
        phantom.exit(data.exit);
    }
};

page.open(url, function (status) {
    page.evaluate(function() {
    	setTimeout(hyjinks.browser.dev.run, 1000);
    });
});
