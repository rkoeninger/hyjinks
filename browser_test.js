'use strict';

/*jslint node: true, browser: true, indent: 2*/
/*global phantom*/

var page = require("webpage").create();
var url = "./resources/public/index.html";

page.onConsoleMessage = function (message) {
  console.log(message);
};

page.onCallback = function (data) {
  if (data && data.hasOwnProperty("exitCode")) {
    phantom.exit(data.exitCode);
  }
};

page.open(url, function (status) {
  if (status !== "success") {
    console.log("Unable to access test site");
    phantom.exit(1);
  } else {
    console.log("Page loaded...");
  }
});
