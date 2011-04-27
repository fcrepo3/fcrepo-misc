// Javascript Client Library for Fedora CloudSync.
// Requires jQuery 1.5+ and http://www.JSON.org/json2.js

function CloudSyncClient(baseURL) {

  //==========================================================================
  //                           PRIVATE METHODS
  //==========================================================================

  function doGet(path, success, error) {
    doGetOrDelete("GET", "json", path, success, error);
  };

  function doGetText(path, success, error) {
    doGetOrDelete("GET", "text", path, success, error);
  };

  function doPost(path, data, success, error) {
    doPostOrPut("POST", path, data, success, error);
  };

  function doPut(path, data, success, error) {
    doPostOrPut("PUT", path, data, success, error);
  };

  function doDelete(path, success, error) {
    doGetOrDelete("DELETE", "json", path, success, error);
  };

  function doGetOrDelete(method, dataType, path, success, error) {
    var url = baseURL + path;
//    alert("Request:\n\n" + method + " " + url);
    var errorCallback = error;
    if (typeof error === 'undefined') {
      errorCallback = this.errorCallback;
    }
    $.ajax({
      type: method,
      url: url,
      dataType: dataType,
      success: success,
      error: function(httpRequest) {
        //callback(method, url, httpRequest);
        errorCallback("GET", url, httpRequest)
      }
    })
  };

  function doPostOrPut(method, path, data, success, error) {
    var url = baseURL + path;
    alert("Request:\n\n" + method + " " + url + "\n\n" + JSON.stringify(data));
    var errorCallback = error;
    if (typeof error === 'undefined') {
      errorCallback = this.errorCallback;
    }
    $.ajax({
      type: method,
      url: url,
      contentType: "application/json",
      data: JSON.stringify(data),
      dataType: "json",
      success: success,
      error: function(jqXHR) {
        errorCallback(method, url, jqXHR)
      }
    })
  };

  //==========================================================================
  //                            PUBLIC METHODS
  //==========================================================================

  // Default behavior is to put up an alert when a non-20x response occurs
  // in response to a REST request. To override, the caller may pass a
  // callback (with the same signature as this one) as the last argument to
  // the method.
  this.errorCallback = function(method, url, httpRequest) {
    alert("[CloudSync Service Error]\n\nUnexpected HTTP response code ("
        + httpRequest.status + ") from request:\n\n" + method + " " + url);
  };

  //--------------------------------------------------------------------------
  //                             Configuration
  //--------------------------------------------------------------------------

  this.getConfiguration = function(success, error) {
    doGet("configuration", success, error);
  };

  this.updateConfiguration = function(data, success, error) {
    doPut("configuration", data, success, error);
  };

  //--------------------------------------------------------------------------
  //                                Users
  //--------------------------------------------------------------------------

  this.createUser = function(data, success, error) {
    doPost("users", data, success, error);
  };

  this.listUsers = function(success, error) {
    doGet("users", success, error);
  };

  this.getUser = function(id, success, error) {
    doGet("users/" + id, success, error);
  };

  this.getCurrentUser = function(success, error) {
    doGet("users/current", success, error);
  };

  this.updateUser = function(id, data, success, error) {
    doPut("users/" + id, data, success, error);
  };

  this.deleteUser = function(id, success, error) {
    doDelete("users/" + id, success, error);
  };

  //--------------------------------------------------------------------------
  //                                Tasks
  //--------------------------------------------------------------------------

  this.createTask = function(data, success, error) {
    doPost("tasks", data, success, error);
  };

  this.listTasks = function(success, error) {
    doGet("tasks", success, error);
  };

  this.getTask = function(id, success, error) {
    doGet("tasks/" + id, success, error);
  };

  this.updateTask = function(id, data, success, error) {
    doPut("tasks/" + id, data, success, error);
  };

  this.deleteTask = function(id, success, error) {
    doDelete("tasks/" + id, success, error);
  };

  //--------------------------------------------------------------------------
  //                              Object Sets
  //--------------------------------------------------------------------------

  this.createObjectSet = function(data, success, error) {
    doPost("objectsets", data, success, error);
  };

  this.listObjectSets = function(success, error) {
    doGet("objectsets", success, error);
  };

  this.getObjectSet = function(id, success, error) {
    doGet("objectsets/" + id, success, error);
  };

  this.updateObjectSet = function(id, data, success, error) {
    doPut("objectsets/" + id, data, success, error);
  };

  this.deleteObjectSet = function(id, success, error) {
    doDelete("objectsets/" + id, success, error);
  };

  //--------------------------------------------------------------------------
  //                             Object Stores
  //--------------------------------------------------------------------------

  this.createObjectStore = function(data, success, error) {
    doPost("objectstores", data, success, error);
  };

  this.listObjectStores = function(success, error) {
    doGet("objectstores", success, error);
  };

  this.getObjectStore = function(id, success, error) {
    doGet("objectstores/" + id, success, error);
  };

  this.queryObjectStore = function(id, setId, limit, offset, success, error) {
    doGet("objectstores/" + id + "/objects?set=" + setId + "&limit=" + limit + "&offset=" + offset, success, error);
  };

  this.updateObjectStore = function(id, data, success, error) {
    doPut("objectstores/" + id, data, success, error);
  };

  this.deleteObjectStore = function(id, success, error) {
    doDelete("objectstores/" + id, success, error);
  };

  //--------------------------------------------------------------------------
  //                             System Logs
  //--------------------------------------------------------------------------

  this.listSystemLogs = function(success, error) {
    doGet("systemlogs", success, error);
  };

  this.getSystemLog = function(id, success, error) {
    doGet("systemlogs/" + id, success, error);
  };

  this.getSystemLogContent = function(id, success, error) {
    doGetText("systemlogs/" + id + "/content", success, error);
  };

  this.deleteSystemLog = function(id, success, error) {
    doDelete("systemlogs/" + id, success, error);
  };

  //--------------------------------------------------------------------------
  //                              Task Logs
  //--------------------------------------------------------------------------

  this.listTaskLogs = function(success, error) {
    doGet("tasklogs", success, error);
  };

  this.getTaskLog = function(id, success, error) {
    doGet("tasklogs/" + id, success, error);
  };

  this.getTaskLogContent = function(id, success, error) {
    doGetText("tasklogs/" + id + "/content", success, error);
  };

  this.deleteTaskLog = function(id, success, error) {
    doDelete("tasklogs/" + id, success, error);
  };

}