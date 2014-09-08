/*
* Copyright 2012-2014 eBay Software Foundation, ios-driver and selendroid committers.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License
* is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing permissions and limitations under
* the License.
*/

function Logger(object) {
    this.obj = object;
    this.lvl = object.lvl ? obj.lvl : 3;
}

Logger.prototype.error = function (msg) {
    if (Inspector.logLevel > 0) {
        var st = this.getOrigin();
        console.error(st.method + ":" + st.line + " " + msg);
    }
}

Logger.prototype.warning = function (msg) {
    if (Inspector.logLevel > 1) {
        var st = this.getOrigin();
        console.warn(st.method + ":" + st.line + " " + msg);
    }
}
Logger.prototype.info = function (msg) {
    if (Inspector.logLevel > 2) {
        var st = this.getOrigin();
        console.info("INFO :" + st.method + ":" + st.line + " " + msg);
    }
}
Logger.prototype.debug = function (msg) {
    if (Inspector.logLevel > 3) {
        var st = this.getOrigin();
        console.log("DEBUG :" + st.method + ":" + st.line + " " + msg);
    }
}

Logger.prototype.getStackTrace = function () {
    var s = new Error().stack;
    var sts = s.split("\n");
    return sts;
}

Logger.prototype.getOrigin = function () {
    var st = this.getStackTrace();
    var s;
    if (st[0] === "Error") { //chrome
        s = this.getStackTrace()[4];
    } else { // FF
        s = this.getStackTrace()[3];
    }

    var pieces = s.split("@"); // Firefox
    if (pieces.length === 1) {
        pieces = s.split(" "); // chrome
    }

    var res = Object();
    res.file = pieces[pieces.length - 1];
    res.method = pieces[pieces.length - 2];
    pieces = res.file.split(":");
    res.line = pieces[3].replace(")", "");
    return res;
}