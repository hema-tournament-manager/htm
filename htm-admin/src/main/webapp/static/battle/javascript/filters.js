angular.module("common.filters", []).
  filter("property", function() { return function(value, property) {
    if (angular.isObject(value)) {
      if (value.hasOwnProperty(property)) {
        return value[property];
      }
    }
  }}).
  
  filter("size", function() { return function(value) {
	 return value.length; 
  }}).
  
  filter("inRangeOf", function() { return function(value, index) {
	  if (angular.isArray(value)) {
		  return value.slice(index - 2, index + 2);
	  }
  }}).
  
  filter("hours", function() { return function(value) {
	  var date = new Date(value);
	  var hh = date.getUTCHours();
	  var mm = date.getUTCMinutes();
	  if (mm < 10)
		  mm = "0" + mm;
	  return hh + ":" + mm;
  }}).
  
  filter("minutes", function() { return function(value) {
    var date = new Date(value);
    var mm = date.getUTCMinutes();
    var ss = date.getUTCSeconds();
    if (ss < 10)
        ss = "0" + ss;
    return mm + ":" + ss;
  }}).
  
  filter("seconds", function() { return function(value) {
    var date = new Date(value);
    return date.getUTCSeconds();
  }});
  
