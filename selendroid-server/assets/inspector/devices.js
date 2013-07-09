/*
* Copyright 2012 ios-driver committers.
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

$(document).ready(function () {

    $(".archive").click(function () {
        var app = $(this).attr("app");
        var device = $(this).attr("deviceId");
        console.log("archive : " + app + "on device " + device);
        var archiver = new Archiver(this, app, device);
        archiver.archive();
    });
});

function Archiver(container, app, device) {
    this.container = container;
    this.app = app;
    this.device = device;

    console.log("Archive");
    console.log(this);
}
Archiver.prototype.archive = function () {
    var me = this;
    var data = {};
    data.bundleId = this.app;
    data.uuid = this.device;
    console.log("archive()" + data.bundleId + "," + data.uuid);

    $.ajax({
               url: "/wd/hub/archive/save",
               async: false,
               type: "POST",
               contentType: 'application/json;charset=UTF-8',
               data: JSON.stringify(data)

           })
        .done(function (data) {
                  console.log("success");
                  new ProgressBar(me.container, me.device, data);
              })
        .fail(function () {
                  console.log("error");
              });
}
Archiver.prototype.displayLink = function () {
    console.log('link!');
}

function ProgressBar(container, device, data) {
    this.container = container;
    this.device = device;
    var me = this;
    this.currentStatus = {};
    me.poller = setInterval(function () {
        me.updateStatus()
    }, 50);
    console.log(this);
    console.log(device);

    $(this.container).prepend("<div id=\"detail\"></div>");
    $(this.container).prepend("<div id=\"progressbar\"></div>");

}

ProgressBar.prototype.done = function () {
    $("#progressbar").remove();
    $("#detail").remove();

    console.log("removing bar.");
    $(this.container).html("Done.");
}

ProgressBar.prototype.updateBar = function (progress) {
    $("#progressbar").progressbar({
                                      value: progress
                                  });
}
ProgressBar.prototype.updateStatus = function () {
    var me = this;
    var data = {};
    data.logId = this.device;
    $.ajax({
               url: "/wd/hub/archive/save",
               async: false,
               dataType: "json",
               type: "GET",
               contentType: 'application/json;charset=UTF-8',
               data: data

           })
        .done(function (data) {
                  //console.log("success");
                  //console.log(data);
                  var status = eval(data);
                  if (status.id === me.currentStatus.id) {
                      //console.log("no update since last call.");
                  } else {
                      me.currentStatus = status;
                      me.updateBar(status.progress);
                      $("#detail").html(JSON.stringify(status));

                      if (status.progress === 100) {
                          console.log("done. Stopping poller.");
                          me.done();
                          clearInterval(me.poller);
                      }

                  }
              })
        .fail(function () {
                  console.log("error");
                  me.done();
                  clearInterval(me.poller);
              });
}