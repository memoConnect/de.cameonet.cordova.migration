var argscheck = require('cordova/argscheck'),
    channel = require('cordova/channel'),
    exec = require('cordova/exec'),
    cordova = require('cordova');

function cmMigrate() {

    this.getOldLocalStorage = function(win, fail, isCrosswalk) {
        var args = [isCrosswalk]
        exec(win, fail, "Migrate", "getOldLocalStorage", args)
    }
}

module.exports = new cmMigrate();
