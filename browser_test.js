'use strict';

/*jslint node: true, browser: true, indent: 2*/
/*global phantom*/

var page = require("webpage").create();
var url = "./resources/public/index.html";

page.onConsoleMessage = function (message) {
  console.log(message);
};

page.onCallback = function (data) {
  if (data && data.hasOwnProperty("exit")) {
    phantom.exit(data.exit);
  }
};

page.open(url);
