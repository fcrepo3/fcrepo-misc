var service = new CloudSyncClient(document.location.href + "api/rest/");

function esc(value) {
  return value.replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/'/g, "&apos;")
              .replace(/"/g, "&quot;");
}

function refreshTasks() {
  service.listTasks(function(data) {
    doSection(data.tasks, "tasks-active", getActiveTaskHtml);
    doSection(data.tasks, "tasks-idle", getIdleTaskHtml);
  });
  service.listTaskLogs(function(data) {
    doSection(data.tasklogs, "tasks-completed", getTaskLogHtml);
  });
}

function refreshSets() {
  service.listObjectSets(function(data) {
    doSection(data.objectsets, "sets-pidpatterns", getPidPatternSetHtml);
    doSection(data.objectsets, "sets-pidlists", getPidListSetHtml);
    doSection(data.objectsets, "sets-queries", getQuerySetHtml);
  });
}

function refreshStores() {
  service.listObjectStores(function(data) {
    doSection(data.objectstores, "stores-duracloud", getDuraCloudStoreHtml);
    doSection(data.objectstores, "stores-fedora", getFedoraStoreHtml);
  });
}

function getActiveTaskHtml(item) {
  var html = "";
  html += "<div class='item-actions'>";
  html += "  <button class='button-pauseTask'>Pause</button>";
  html += "  <button class='button-abortTask'>Abort</button>";
  html += "</div>";
  html += "<div class='item-attributes'>Attributes:";
  $.each(item, function(key, value) {
    html += "<br/>" + key + ": " + value;
  });
  html += "</div>";
  return html;
}

function getIdleTaskHtml(item) {
  var html = "";
  html += "<div class='item-actions'>";
  html += "  <button class='button-runTask'>Run</button>";
  html += "  <button class='button-editTask'>Edit</button>";
  html += "  <button class='button-deleteTask'>Delete</button>";
  html += "</div>";
  html += "<div class='item-attributes'>Attributes:";
  $.each(item, function(key, value) {
    html += "<br/>" + key + ": " + value;
  });
  html += "</div>";
  return html;
}

function getTaskLogHtml(item) {
  var html = "";
  html += "<div class='item-actions'>";
  html += "  <button>View Log</button>";
  html += "</div>";
  html += "<div class='item-attributes'>Attributes:";
  $.each(item, function(key, value) {
    html += "<br/>" + key + ": " + value;
  });
  html += "</div>";
  return html;
}

function doDeleteObjectSet(id, name) {
  $("#dialog-confirm").html("<span class=\"ui-icon ui-icon-alert\" style=\"float:left; margin:0 7px 20px 0;\"/>Delete Set <strong>" + esc(name) + "</strong>?");
  $("#dialog-confirm").dialog("option", "buttons", {
    "No": function() {
      $(this).dialog("close");
    },
    "Yes": function() {
      $(this).dialog("close");
      service.deleteObjectSet(id, function() {
        refreshSets();
      });
    }
  });
  $("#dialog-confirm").dialog("open");
}

function getPidPatternSetHtml(item) {
  var html = "";
  if (item.type == "pidPattern") {
    html += "<div class='item-actions'>";
    if (item.id != 1) {
      html += "  <button onClick='doDeleteObjectSet(" + item.id + ", \"" + esc(item.name) + "\");'>Delete</button>";
    }
    html += "</div>";
    html += "<div><table>";
    html += "  <tr><td><strong>Pattern:</strong></td><td>" + esc(item.data) + "</td></tr>";
    html += "</table></div>";
  }
  return html;
}

function getPidListSetHtml(item) {
  var html = "";
  if (item.type == "pidList") {
    html += "<div class='item-actions'>";
    html += "  <button onClick='doDeleteObjectSet(" + item.id + ", \"" + esc(item.name) + "\");'>Delete</button>";
    html += "</div>";
    html += "<div><table>";
    html += "  <tr><td><strong>PIDs:</strong></td><td>" + esc(item.data) + "</td></tr>";
    html += "</table></div>";
  }
  return html;
}

function getQuerySetHtml(item) {
  var html = "";
  if (item.type == "query") {
    var data = $.parseJSON(item.data);
    html += "<div class='item-actions'>";
    html += "  <button onClick='doDeleteObjectSet(" + item.id + ", \"" + esc(item.name) + "\");'>Delete</button>";
    html += "</div>";
    html += "<div><table>";
    html += "  <tr><td><strong>Query Language:</strong></td><td>" + esc(data.queryType) + "</td></tr>";
    html += "  <tr><td><strong>Query Text:</strong></td><td><pre>" + esc(data.queryText) + "</pre></td></tr>";
    html += "</table></div>";
  }
  return html;
}

function doForgetObjectStore(id, name) {
  $("#dialog-confirm").html("<span class=\"ui-icon ui-icon-alert\" style=\"float:left; margin:0 7px 20px 0;\"/>Forget Store <strong>" + esc(name) + "</strong>?");
  $("#dialog-confirm").dialog("option", "buttons", {
    "No": function() {
      $(this).dialog("close");
    },
    "Yes": function() {
      $(this).dialog("close");
      service.deleteObjectStore(id, function() {
        refreshStores();
      });
    }
  });
  $("#dialog-confirm").dialog("open");
}

function getDuraCloudStoreHtml(item) {
  var html = "";
  if (item.type == "duracloud") {
    var data = $.parseJSON(item.data);
    html += "<div class='item-actions'>";
    html += "  <button onClick='doForgetObjectStore(" + item.id + ", \"" + esc(item.name) + "\");'>Forget</button>";
    html += "</div>";
    html += "<div><table>";
    html += "  <tr><td><strong>DuraStore URL:</strong></td><td>" + esc(data.url) + "</td></tr>";
    html += "  <tr><td><strong>Username:</strong></td><td>" + esc(data.username) + "</td></tr>";
    html += "  <tr><td><strong>Password:</strong></td><td>(Not shown)</td></tr>";
    html += "  <tr><td><strong>Storage Provider:</strong></td><td>" + esc(data.providerName) + "</td></tr>";
    html += "  <tr><td><strong>Space:</strong></td><td>" + esc(data.space) + "</td></tr>";
    var prefix = data.prefix;
    if (prefix == "") {
      prefix = "(None)";
    }
    html += "  <tr><td><strong>Content Id Prefix:</strong></td><td>" + esc(prefix) + "</td></tr>";
    html += "</table></div>";
  }
  return html;
}

function getFedoraStoreHtml(item) {
  var html = "";
  if (item.type == "fedora") {
    var data = $.parseJSON(item.data);
    html += "<div class='item-actions'>";
    html += "  <button onClick='doForgetObjectStore(" + item.id + ", \"" + esc(item.name) + "\");'>Forget</button>";
    html += "</div>";
    html += "<div><table>";
    html += "  <tr><td><strong>Base URL:</strong></td><td>" + esc(data.url) + "</td></tr>";
    html += "  <tr><td><strong>Username:</strong></td><td>" + esc(data.username) + "</td></tr>";
    html += "  <tr><td><strong>Password:</strong></td><td>(Not shown)</td></tr>";
    html += "</table></div>";
  }
  return html;
}

function doSection(items, sectionName, itemHtmlGetter) {
  var html = "";
  var count = 0;
  $.each(items, function(index, item) {
    var body = itemHtmlGetter(item);
    if (body) {
      count++;
      html += getExpandable(item.name, body);
    }
  });
  if (count > 0) {
    $("#" + sectionName).html(html);
    $("#" + sectionName + " .item-actions button").button();
    $("#" + sectionName + " .item-actions .button-pauseTask").button();
    $("#" + sectionName + " .item-actions .button-abortTask").button();
    $("#" + sectionName + " .expandable").accordion({collapsible: true, active: false});
  } else {
    $("#" + sectionName).html("None.");
  }
}

function getExpandable(title, bodyHtml) {
  var html = "";
  html += "<div class='expandable'>";
  html += "  <h3><a href='#'>" + esc(title) + "</a></h3>";
  html += "  <div class='expandable-body'>" + bodyHtml + "</div>";
  html += "</div>";
  return html;
}

var loadedTasks = false;
var loadedSets = false;
var loadedStores = false;

$(function() {

  // initialize ui elements

  $(".button-Refresh").button({
    icons: { primary: "ui-icon-arrowrefresh-1-e" }//,
  });

  $("#tasks .button-Refresh").click(function() { refreshTasks(); });
  $("#sets .button-Refresh").click(function() { refreshSets(); });
  $("#stores .button-Refresh").click(function() { refreshStores(); });

  $("#button-Logout").button({
    icons: { primary: "ui-icon-power" }
  });

  $("#button-Logout").click(
    function() {
      document.location = 'j_spring_security_logout';
    }
  );


  $("#tabs").tabs({
    show: function(event, ui) {
      if (ui.index == 0 && !loadedTasks) {
        refreshTasks();
        loadedTasks = true;
      } else if (ui.index == 1 && !loadedSets) {
        refreshSets();
        loadedSets = true;
      } else if (ui.index == 2 && !loadedStores) {
        loadedStores = true;
        refreshStores();
      }
    }
  });

  $("#button-NewTask").button({
    icons: { primary: "ui-icon-plus" }
  });

  $("#button-NewTask").click(
    function() {
      $("#dialog-NewTask").dialog("open");
    }
  );

  $("#button-NewSet").button({
    icons: { primary: "ui-icon-plus" }
  });

  $("#button-NewSet").click(
    function() {
      $("#dialog-NewSet").dialog("open");
    }
  );

  $("#button-NewStore").button({
    icons: { primary: "ui-icon-plus" }
  });

  $("#button-NewStore").click(
    function() {
      $("#dialog-NewStore").dialog("open");
    }
  );

  $("#dialog-confirm").dialog({
    autoOpen: false,
    autoOpen: false,
    modal: true,
    width: 'auto',
    show: 'fade',
    hide: 'fade'
  });

  $("#dialog-NewTask").dialog({
    autoOpen: false,
    width: 550,
    modal: true,
    show: 'fade',
    hide: 'fade',
    buttons: {
      OK: function() {
        $(this).dialog("close");
      }
    }
  });

  $("#dialog-NewSet").dialog({
    autoOpen: false,
    modal: true,
    width: 'auto',
    show: 'fade',
    hide: 'fade'
  });

  $("#dialog-NewPidPattern").dialog({
    autoOpen: false,
    modal: true,
    width: 'auto',
    show: 'fade',
    hide: 'fade',
    buttons: {
      Save: function() {
        var data = { objectset: {
          "name": $("#NewPidPattern-name").val(),
          "type": "pidPattern",
          "data": $("#NewPidPattern-data").val()
        }};
        service.createObjectSet(data, function() {
          $("#dialog-NewPidPattern").dialog("close");
          refreshSets();
        });
      }
    }
  });

  $("#dialog-NewPidList").dialog({
    autoOpen: false,
    modal: true,
    width: 'auto',
    show: 'fade',
    hide: 'fade',
    buttons: {
      Save: function() {
        var data = { objectset: {
          "name": $("#NewPidList-name").val(),
          "type": "pidList",
          "data": $("#NewPidList-data").val()
        }};
        service.createObjectSet(data, function() {
          $("#dialog-NewPidList").dialog("close");
          refreshSets();
        });
      }
    }
  });

  $("#dialog-NewQuery").dialog({
    autoOpen: false,
    modal: true,
    width: 'auto',
    show: 'fade',
    hide: 'fade',
    buttons: {
      Save: function() {
        var typeSpecificData = {
          "queryType": $("#NewQuery-queryType").val(),
          "queryText": $("#NewQuery-queryText").val()
        };
        var data = { objectset: {
          "name": $("#NewQuery-name").val(),
          "type": "query",
          "data": JSON.stringify(typeSpecificData)
        }};
        service.createObjectSet(data, function() {
          $("#dialog-NewQuery").dialog("close");
          refreshSets();
        });
      }
    }
  });


  $("#dialog-NewStore").dialog({
    autoOpen: false,
    modal: true,
    width: 'auto',
    show: 'fade',
    hide: 'fade'
  });

  $("#NewDuraCloudStore-providerId").change(function() {
    showSpacesForProvider($("#NewDuraCloudStore-providerId").val());
  });

  $("#dialog-NewDuraCloudStore").dialog({
    autoOpen: false,
    modal: true,
    width: 'auto',
    show: 'fade',
    hide: 'fade',
    buttons: {
      Next: function() {
        service.listProviderAccounts(
          $("#NewDuraCloudStore-url").val(),
          $("#NewDuraCloudStore-username").val(),
          $("#NewDuraCloudStore-password").val(),
          function(data) {
            // success -- close dialog, then construct and show the next one
            $("#dialog-NewDuraCloudStore").dialog("close");
            var html = "";
            $.each(data.provideraccounts, function(index, account) {
              html += "<option value='" + account.id + "'>";
              html += account.type;
              html += "</option>";
            });
            $("#NewDuraCloudStore-providerId").html(html);
            showSpacesForProvider($("#NewDuraCloudStore-providerId").val());
            $("#dialog-NewDuraCloudStoreStep2").dialog("open");
          },
          function() {
            // failure -- alert and keep dialog open
            alert("Error connecting to DuraCloud instance.\nWrong URL, Username, or Password?");
          }
        );
      }
    }
  });

  $("#dialog-NewDuraCloudStoreStep2").dialog({
    autoOpen: false,
    modal: true,
    show: 'fade',
    hide: 'fade',
    buttons: {
      Next: function() {
        $(this).dialog("close");
        $("#NewDuraCloudStoreStep3-url").html($("#NewDuraCloudStore-url").val());
        $("#NewDuraCloudStoreStep3-username").html($("#NewDuraCloudStore-username").val());
        var providerName = $("#NewDuraCloudStore-providerId option:selected").text();
        $("#NewDuraCloudStoreStep3-providerName").html(providerName);
        $("#NewDuraCloudStoreStep3-space").html($("#NewDuraCloudStore-space").val());
        $("#NewDuraCloudStoreStep3-prefix").html($("#NewDuraCloudStore-prefix").val());
        var prefix = $("#NewDuraCloudStore-prefix").val();
        if (prefix != "") {
          prefix = "/" + prefix;
        }
        $("#NewDuraCloudStoreStep3-name").val(
            "DuraCloud Space "
            + $("#NewDuraCloudStore-space").val() + prefix + " at "
            + $("#NewDuraCloudStore-url").val().split("/")[2]
            + " (" + providerName + ") ");
        $("#dialog-NewDuraCloudStoreStep3").dialog("open");
      }
    }
  });

  $("#dialog-NewDuraCloudStoreStep3").dialog({
    autoOpen: false,
    modal: true,
    width: 'auto',
    show: 'fade',
    hide: 'fade',
    buttons: {
      Save: function() {
        var typeSpecificData = {
          "url": $("#NewDuraCloudStore-url").val(),
          "username": $("#NewDuraCloudStore-username").val(),
          "password": $("#NewDuraCloudStore-password").val(),
          "providerId": $("#NewDuraCloudStore-providerId").val(),
          "providerName": $("#NewDuraCloudStore-providerId option:selected").text(),
          "space": $("#NewDuraCloudStore-space").val(),
          "prefix": $("#NewDuraCloudStore-prefix").val()
        };
        var data = { objectstore: {
          "name": $("#NewDuraCloudStoreStep3-name").val(),
          "type": "duracloud",
          "data": JSON.stringify(typeSpecificData)
        }};
        service.createObjectStore(data,
            function() {
              $("#dialog-NewDuraCloudStoreStep3").dialog("close");
              refreshStores();
            });
      }
    }
  });

  $("#dialog-NewFedoraStore").dialog({
    autoOpen: false,
    modal: true,
    width: 'auto',
    show: 'fade',
    hide: 'fade',
    buttons: {
      Next: function() {
        $(this).dialog("close");
        $("#NewFedoraStoreStep2-url").html($("#NewFedoraStore-url").val());
        $("#NewFedoraStoreStep2-username").html($("#NewFedoraStore-username").val());
        $("#NewFedoraStoreStep2-name").val(
          "Fedora Repository at " +
          $("#NewFedoraStore-url").val().split("/")[2]);
        $("#dialog-NewFedoraStoreStep2").dialog("open");
      }
    }
  });

  $("#dialog-NewFedoraStoreStep2").dialog({
    autoOpen: false,
    modal: true,
    width: 'auto',
    show: 'fade',
    hide: 'fade',
    buttons: {
      Save: function() {
        var typeSpecificData = {
          "url": $("#NewFedoraStore-url").val(),
          "username": $("#NewFedoraStore-username").val(),
          "password": $("#NewFedoraStore-password").val()
        };
        var data = { objectstore: {
          "name": $("#NewFedoraStoreStep2-name").val(),
          "type": "fedora",
          "data": JSON.stringify(typeSpecificData)
        }};
        service.createObjectStore(data,
            function() {
              $("#dialog-NewFedoraStoreStep2").dialog("close");
              refreshStores();
            });
      }
    }
  });

  $("#dialog-NewStore button").button();

  $("#button-NewDuraCloudStore").click(
      function() {
        $("#dialog-NewStore").dialog("close");
        $("#dialog-NewDuraCloudStore").dialog("open");
      }
  );

  $("#button-NewFedoraStore").click(
    function() {
      $("#dialog-NewStore").dialog("close");
      $("#dialog-NewFedoraStore").dialog("open");
    }
  );

  $("#dialog-NewSet button").button();

  $("#button-NewPidPattern").click(
      function() {
        $("#dialog-NewSet").dialog("close");
        $("#dialog-NewPidPattern").dialog("open");
      }
      );

  $("#button-NewPidList").click(
      function() {
        $("#dialog-NewSet").dialog("close");
        $("#dialog-NewPidList").dialog("open");
      }
      );

  $("#button-NewQuery").click(
      function() {
        $("#dialog-NewSet").dialog("close");
        $("#dialog-NewQuery").dialog("open");
      }
  );

  service.getCurrentUser(function(data, status, x) {
    $("#username").text(data.user.name);
  });

});

function showSpacesForProvider(id) {
  service.listSpaces(
    $("#NewDuraCloudStore-url").val(),
    $("#NewDuraCloudStore-username").val(),
    $("#NewDuraCloudStore-password").val(),
    id,
    function(data) {
      var html = "";
      $.each(data.spaces, function(index, space) {
        html += "<option value='" + space.id + "'>";
        html += space.id;
        html += "</option>";
      });
      $("#NewDuraCloudStore-space").html(html);
    }
  );
}