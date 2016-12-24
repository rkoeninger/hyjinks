'use strict';

/*jslint node: true, browser: true, indent: 2*/
/*global phantom*/

var page = require("webpage").create();
var args = require("system").args;
var url;

if (args.length > 1 && args[1] === "server") {
  url = "http://localhost:3450/";
} else {
  url = "resources/public/index.html";
}

console.log("Opening " + url);

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
