;(function(omidGlobal, factory, exports) {
  // CommonJS support
  if (typeof exports === 'object' && typeof exports.nodeName !== 'string') {
    factory(omidGlobal, exports);

  // If neither AMD nor CommonJS are used, export to a versioned name in the
  // global context.
  } else {
    var exports = {};
    var versions = ['1.2.15-iab990'];
    var additionalVersionString = 'default';
    if (!!additionalVersionString) {
       versions.push(additionalVersionString);
    }

    factory(omidGlobal, exports);

    function deepFreeze(object) {
      for (var key in object) {
        if (object.hasOwnProperty(key)) {
          object[key] = deepFreeze(object[key]);
        }
      }
      return Object.freeze(object);
    }

    // Inject and freeze the exported components of omid.
    for (var key in exports) {
      if (exports.hasOwnProperty(key)) {
        if (Object.getOwnPropertyDescriptor(omidGlobal, key) == null) {
          // Define the top level property in the global scope
          Object.defineProperty(omidGlobal, key, {
            value: {},
          });
        }
        versions.forEach((version) => {
          if (Object.getOwnPropertyDescriptor(omidGlobal[key], version) == null) {
            var frozenObject = deepFreeze(exports[key]);
            // Define the object exports keyed-off versions
            Object.defineProperty(omidGlobal[key], version, {
              get: function () {
                return frozenObject;
              },
              enumerable: true,
            });
          }
        });
      }
    }
  }
}(typeof global === 'undefined' ? this : global, function(omidGlobal, omidExports) {
  'use strict';var $jscomp = $jscomp || {};
$jscomp.scope = {};
$jscomp.inherits = function(a, b) {
  function c() {
  }
  c.prototype = b.prototype;
  a.superClass_ = b.prototype;
  a.prototype = new c;
  a.prototype.constructor = a;
  for (var d in b) {
    if ("prototype" != d) {
      if (Object.defineProperties) {
        var e = Object.getOwnPropertyDescriptor(b, d);
        e && Object.defineProperty(a, d, e);
      } else {
        a[d] = b[d];
      }
    }
  }
};
$jscomp.ASSUME_ES5 = !1;
$jscomp.ASSUME_NO_NATIVE_MAP = !1;
$jscomp.ASSUME_NO_NATIVE_SET = !1;
$jscomp.defineProperty = $jscomp.ASSUME_ES5 || "function" == typeof Object.defineProperties ? Object.defineProperty : function(a, b, c) {
  a != Array.prototype && a != Object.prototype && (a[b] = c.value);
};
$jscomp.getGlobal = function(a) {
  return "undefined" != typeof window && window === a ? a : "undefined" != typeof global && null != global ? global : a;
};
$jscomp.global = $jscomp.getGlobal(this);
$jscomp.SYMBOL_PREFIX = "jscomp_symbol_";
$jscomp.initSymbol = function() {
  $jscomp.initSymbol = function() {
  };
  $jscomp.global.Symbol || ($jscomp.global.Symbol = $jscomp.Symbol);
};
$jscomp.symbolCounter_ = 0;
$jscomp.Symbol = function(a) {
  return $jscomp.SYMBOL_PREFIX + (a || "") + $jscomp.symbolCounter_++;
};
$jscomp.initSymbolIterator = function() {
  $jscomp.initSymbol();
  var a = $jscomp.global.Symbol.iterator;
  a || (a = $jscomp.global.Symbol.iterator = $jscomp.global.Symbol("iterator"));
  "function" != typeof Array.prototype[a] && $jscomp.defineProperty(Array.prototype, a, {configurable:!0, writable:!0, value:function() {
    return $jscomp.arrayIterator(this);
  }});
  $jscomp.initSymbolIterator = function() {
  };
};
$jscomp.arrayIterator = function(a) {
  var b = 0;
  return $jscomp.iteratorPrototype(function() {
    return b < a.length ? {done:!1, value:a[b++]} : {done:!0};
  });
};
$jscomp.iteratorPrototype = function(a) {
  $jscomp.initSymbolIterator();
  a = {next:a};
  a[$jscomp.global.Symbol.iterator] = function() {
    return this;
  };
  return a;
};
$jscomp.makeIterator = function(a) {
  $jscomp.initSymbolIterator();
  var b = a[Symbol.iterator];
  return b ? b.call(a) : $jscomp.arrayIterator(a);
};
$jscomp.arrayFromIterator = function(a) {
  for (var b, c = []; !(b = a.next()).done;) {
    c.push(b.value);
  }
  return c;
};
$jscomp.arrayFromIterable = function(a) {
  return a instanceof Array ? a : $jscomp.arrayFromIterator($jscomp.makeIterator(a));
};
var module$exports$omid$common$argsChecker = {assertTruthyString:function(a, b) {
  if (!b) {
    throw Error("Value for " + a + " is undefined, null or blank.");
  }
  if ("string" !== typeof b && !(b instanceof String)) {
    throw Error("Value for " + a + " is not a string.");
  }
  if ("" === b.trim()) {
    throw Error("Value for " + a + " is empty string.");
  }
}, assertNotNullObject:function(a, b) {
  if (null == b) {
    throw Error("Value for " + a + " is undefined or null");
  }
}, assertNumber:function(a, b) {
  if (null == b) {
    throw Error(a + " must not be null or undefined.");
  }
  if ("number" !== typeof b || isNaN(b)) {
    throw Error("Value for " + a + " is not a number");
  }
}, assertNumberBetween:function(a, b, c, d) {
  (0,module$exports$omid$common$argsChecker.assertNumber)(a, b);
  if (b < c || b > d) {
    throw Error("Value for " + a + " is outside the range [" + c + "," + d + "]");
  }
}, assertFunction:function(a, b) {
  if (!b) {
    throw Error(a + " must not be truthy.");
  }
}, assertPositiveNumber:function(a, b) {
  (0,module$exports$omid$common$argsChecker.assertNumber)(a, b);
  if (0 > b) {
    throw Error(a + " must be a positive number.");
  }
}};
var module$exports$omid$common$exporter = {};
function module$contents$omid$common$exporter_getOmidExports() {
  return "undefined" === typeof omidExports ? null : omidExports;
}
function module$contents$omid$common$exporter_getOrCreateName(a, b) {
  return a && (a[b] || (a[b] = {}));
}
module$exports$omid$common$exporter.packageExport = function(a, b, c) {
  if (c = void 0 === c ? module$contents$omid$common$exporter_getOmidExports() : c) {
    a = a.split("."), a.slice(0, a.length - 1).reduce(module$contents$omid$common$exporter_getOrCreateName, c)[a[a.length - 1]] = b;
  }
};
var module$exports$omid$sessionClient$Partner = function(a, b) {
  module$exports$omid$common$argsChecker.assertTruthyString("Partner.name", a);
  module$exports$omid$common$argsChecker.assertTruthyString("Partner.version", b);
  this.name = a;
  this.version = b;
};
(0,module$exports$omid$common$exporter.packageExport)("OmidSessionClient.Partner", module$exports$omid$sessionClient$Partner);
var module$exports$omid$sessionClient$VerificationScriptResource = function(a, b, c) {
  module$exports$omid$common$argsChecker.assertTruthyString("VerificationScriptResource.resourceUrl", a);
  this.resourceUrl = a;
  this.vendorKey = b;
  this.verificationParameters = c;
};
(0,module$exports$omid$common$exporter.packageExport)("OmidSessionClient.VerificationScriptResource", module$exports$omid$sessionClient$VerificationScriptResource);
var module$exports$omid$sessionClient$Context = function(a, b) {
  module$exports$omid$common$argsChecker.assertNotNullObject("Context.partner", a);
  this.partner = a;
  this.verificationScriptResources = b;
  this.videoElement = this.slotElement = null;
};
module$exports$omid$sessionClient$Context.prototype.setVideoElement = function(a) {
  module$exports$omid$common$argsChecker.assertNotNullObject("Context.videoElement", a);
  this.videoElement = a;
};
module$exports$omid$sessionClient$Context.prototype.setSlotElement = function(a) {
  module$exports$omid$common$argsChecker.assertNotNullObject("Context.slotElement", a);
  this.slotElement = a;
};
(0,module$exports$omid$common$exporter.packageExport)("OmidSessionClient.Context", module$exports$omid$sessionClient$Context);
var module$exports$omid$common$constants = {AdEventType:{IMPRESSION:"impression", STATE_CHANGE:"stateChange", GEOMETRY_CHANGE:"geometryChange", SESSION_START:"sessionStart", SESSION_ERROR:"sessionError", SESSION_FINISH:"sessionFinish", VIDEO:"video", LOADED:"loaded", START:"start", FIRST_QUARTILE:"firstQuartile", MIDPOINT:"midpoint", THIRD_QUARTILE:"thirdQuartile", COMPLETE:"complete", PAUSE:"pause", RESUME:"resume", BUFFER_START:"bufferStart", BUFFER_FINISH:"bufferFinish", SKIPPED:"skipped", VOLUME_CHANGE:"volumeChange", 
PLAYER_STATE_CHANGE:"playerStateChange", AD_USER_INTERACTION:"adUserInteraction"}, VideoEventType:{LOADED:"loaded", START:"start", FIRST_QUARTILE:"firstQuartile", MIDPOINT:"midpoint", THIRD_QUARTILE:"thirdQuartile", COMPLETE:"complete", PAUSE:"pause", RESUME:"resume", BUFFER_START:"bufferStart", BUFFER_FINISH:"bufferFinish", SKIPPED:"skipped", VOLUME_CHANGE:"volumeChange", PLAYER_STATE_CHANGE:"playerStateChange", AD_USER_INTERACTION:"adUserInteraction"}, ErrorType:{GENERIC:"generic", VIDEO:"video"}, 
AdSessionType:{NATIVE:"native", HTML:"html"}, EventOwner:{NATIVE:"native", JAVASCRIPT:"javascript", NONE:"none"}, AccessMode:{FULL:"full", LIMITED:"limited"}, AppState:{BACKGROUNDED:"backgrounded", FOREGROUNDED:"foregrounded"}, Environment:{APP:"app", WEB:"web"}, InteractionType:{CLICK:"click", INVITATION_ACCEPT:"invitationAccept"}, MediaType:{DISPLAY:"display", VIDEO:"video"}, Reason:{NOT_FOUND:"notFound", HIDDEN:"hidden", BACKGROUNDED:"backgrounded", VIEWPORT:"viewport", OBSTRUCTED:"obstructed", 
CLIPPED:"clipped"}, SupportedFeatures:{CONTAINER:"clid", VIDEO:"vlid"}, VideoPosition:{PREROLL:"preroll", MIDROLL:"midroll", POSTROLL:"postroll", STANDALONE:"standalone"}, VideoPlayerState:{MINIMIZED:"minimized", COLLAPSED:"collapsed", NORMAL:"normal", EXPANDED:"expanded", FULLSCREEN:"fullscreen"}, NativeViewKeys:{X:"x", LEFT:"left", Y:"y", TOP:"top", WIDTH:"width", HEIGHT:"height", AD_SESSION_ID:"adSessionId", IS_FRIENDLY_OBSTRUCTION_FOR:"isFriendlyObstructionFor", CLIPS_TO_BOUNDS:"clipsToBounds", 
CHILD_VIEWS:"childViews", END_X:"endX", END_Y:"endY", OBSTRUCTIONS:"obstructions"}, MeasurementStateChangeSource:{CONTAINER:"container", CREATIVE:"creative"}, ElementMarkup:{OMID_ELEMENT_CLASS_NAME:"omid-element"}, CommunicationType:{NONE:"NONE", DIRECT:"DIRECT", POST_MESSAGE:"POST_MESSAGE"}, OmidImplementer:{OMSDK:"omsdk"}};
var module$contents$omid$common$InternalMessage_GUID_KEY = "omid_message_guid", module$contents$omid$common$InternalMessage_METHOD_KEY = "omid_message_method", module$contents$omid$common$InternalMessage_VERSION_KEY = "omid_message_version", module$contents$omid$common$InternalMessage_ARGS_KEY = "omid_message_args", module$exports$omid$common$InternalMessage = function(a, b, c, d) {
  this.guid = a;
  this.method = b;
  this.version = c;
  this.args = d;
};
module$exports$omid$common$InternalMessage.isValidSerializedMessage = function(a) {
  return !!a && void 0 !== a[module$contents$omid$common$InternalMessage_GUID_KEY] && void 0 !== a[module$contents$omid$common$InternalMessage_METHOD_KEY] && void 0 !== a[module$contents$omid$common$InternalMessage_VERSION_KEY] && "string" === typeof a[module$contents$omid$common$InternalMessage_GUID_KEY] && "string" === typeof a[module$contents$omid$common$InternalMessage_METHOD_KEY] && "string" === typeof a[module$contents$omid$common$InternalMessage_VERSION_KEY] && (void 0 === a[module$contents$omid$common$InternalMessage_ARGS_KEY] || 
  void 0 !== a[module$contents$omid$common$InternalMessage_ARGS_KEY]);
};
module$exports$omid$common$InternalMessage.deserialize = function(a) {
  return new module$exports$omid$common$InternalMessage(a[module$contents$omid$common$InternalMessage_GUID_KEY], a[module$contents$omid$common$InternalMessage_METHOD_KEY], a[module$contents$omid$common$InternalMessage_VERSION_KEY], a[module$contents$omid$common$InternalMessage_ARGS_KEY]);
};
module$exports$omid$common$InternalMessage.prototype.serialize = function() {
  var a = {};
  a = (a[module$contents$omid$common$InternalMessage_GUID_KEY] = this.guid, a[module$contents$omid$common$InternalMessage_METHOD_KEY] = this.method, a[module$contents$omid$common$InternalMessage_VERSION_KEY] = this.version, a);
  void 0 !== this.args && (a[module$contents$omid$common$InternalMessage_ARGS_KEY] = this.args);
  return a;
};
var module$exports$omid$common$Communication = function(a) {
  this.to = a;
  this.communicationType_ = module$exports$omid$common$constants.CommunicationType.NONE;
};
module$exports$omid$common$Communication.prototype.sendMessage = function(a, b) {
};
module$exports$omid$common$Communication.prototype.handleMessage = function(a, b) {
  if (this.onMessage) {
    this.onMessage(a, b);
  }
};
module$exports$omid$common$Communication.prototype.generateGuid = function() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function(a) {
    var b = 16 * Math.random() | 0;
    a = "y" === a ? (b & 3 | 8).toString(16) : b.toString(16);
    return a;
  });
};
module$exports$omid$common$Communication.prototype.serialize = function(a) {
  return JSON.stringify(a);
};
module$exports$omid$common$Communication.prototype.deserialize = function(a) {
  return JSON.parse(a);
};
module$exports$omid$common$Communication.prototype.isDirectCommunication = function() {
  return this.communicationType_ === module$exports$omid$common$constants.CommunicationType.DIRECT;
};
var module$exports$omid$common$logger = {error:function(a) {
  for (var b = [], c = 0; c < arguments.length; ++c) {
    b[c - 0] = arguments[c];
  }
  module$contents$omid$common$logger_executeLog(function() {
    throw new (Function.prototype.bind.apply(Error, [null].concat(["Could not complete the test successfully - "], $jscomp.arrayFromIterable(b))));
  }, function() {
    return console.error.apply(console, [].concat($jscomp.arrayFromIterable(b)));
  });
}, debug:function(a) {
  for (var b = [], c = 0; c < arguments.length; ++c) {
    b[c - 0] = arguments[c];
  }
  module$contents$omid$common$logger_executeLog(function() {
  }, function() {
    return console.error.apply(console, [].concat($jscomp.arrayFromIterable(b)));
  });
}};
function module$contents$omid$common$logger_executeLog(a, b) {
  "undefined" !== typeof jasmine && jasmine ? a() : "undefined" !== typeof console && console && console.error && b();
}
;var module$exports$omid$common$Rectangle = function(a, b, c, d) {
  this.x = a;
  this.y = b;
  this.width = c;
  this.height = d;
};
var module$exports$omid$common$eventTypedefs = {};
var module$exports$omid$common$VersionUtils = {}, module$contents$omid$common$VersionUtils_SEMVER_DIGITS_NUMBER = 3;
module$exports$omid$common$VersionUtils.isValidVersion = function(a) {
  return /\d+\.\d+\.\d+(-.*)?/.test(a);
};
module$exports$omid$common$VersionUtils.versionGreaterOrEqual = function(a, b) {
  a = a.split("-")[0].split(".");
  b = b.split("-")[0].split(".");
  for (var c = 0; c < module$contents$omid$common$VersionUtils_SEMVER_DIGITS_NUMBER; c++) {
    var d = parseInt(a[c], 10), e = parseInt(b[c], 10);
    if (d > e) {
      break;
    } else {
      if (d < e) {
        return !1;
      }
    }
  }
  return !0;
};
var module$exports$omid$common$ArgsSerDe = {}, module$contents$omid$common$ArgsSerDe_ARGS_NOT_SERIALIZED_VERSION = "1.0.3";
module$exports$omid$common$ArgsSerDe.serializeMessageArgs = function(a, b) {
  return (0,module$exports$omid$common$VersionUtils.isValidVersion)(a) && (0,module$exports$omid$common$VersionUtils.versionGreaterOrEqual)(a, module$contents$omid$common$ArgsSerDe_ARGS_NOT_SERIALIZED_VERSION) ? b : JSON.stringify(b);
};
module$exports$omid$common$ArgsSerDe.deserializeMessageArgs = function(a, b) {
  return (0,module$exports$omid$common$VersionUtils.isValidVersion)(a) && (0,module$exports$omid$common$VersionUtils.versionGreaterOrEqual)(a, module$contents$omid$common$ArgsSerDe_ARGS_NOT_SERIALIZED_VERSION) ? b ? b : [] : b && "string" === typeof b ? JSON.parse(b) : [];
};
var module$exports$omid$common$DirectCommunication = function(a) {
  module$exports$omid$common$Communication.call(this, a);
  this.communicationType_ = module$exports$omid$common$constants.CommunicationType.DIRECT;
  this.handleExportedMessage = module$exports$omid$common$DirectCommunication.prototype.handleExportedMessage.bind(this);
};
$jscomp.inherits(module$exports$omid$common$DirectCommunication, module$exports$omid$common$Communication);
module$exports$omid$common$DirectCommunication.prototype.sendMessage = function(a, b) {
  b = void 0 === b ? this.to : b;
  if (!b) {
    throw Error("Message destination must be defined at construction time or when sending the message.");
  }
  b.handleExportedMessage(a.serialize(), this);
};
module$exports$omid$common$DirectCommunication.prototype.handleExportedMessage = function(a, b) {
  module$exports$omid$common$InternalMessage.isValidSerializedMessage(a) && this.handleMessage(module$exports$omid$common$InternalMessage.deserialize(a), b);
};
var module$exports$omid$common$OmidGlobalProvider = {}, module$contents$omid$common$OmidGlobalProvider_globalThis = eval("this");
function module$contents$omid$common$OmidGlobalProvider_getOmidGlobal() {
  if ("undefined" !== typeof omidGlobal && omidGlobal) {
    return omidGlobal;
  }
  if ("undefined" !== typeof global && global) {
    return global;
  }
  if ("undefined" !== typeof window && window) {
    return window;
  }
  if ("undefined" !== typeof module$contents$omid$common$OmidGlobalProvider_globalThis && module$contents$omid$common$OmidGlobalProvider_globalThis) {
    return module$contents$omid$common$OmidGlobalProvider_globalThis;
  }
  throw Error("Could not determine global object context.");
}
module$exports$omid$common$OmidGlobalProvider.omidGlobal = module$contents$omid$common$OmidGlobalProvider_getOmidGlobal();
var module$exports$omid$common$PostMessageCommunication = function(a, b) {
  b = void 0 === b ? module$exports$omid$common$OmidGlobalProvider.omidGlobal : b;
  module$exports$omid$common$Communication.call(this, b);
  var c = this;
  this.communicationType_ = module$exports$omid$common$constants.CommunicationType.POST_MESSAGE;
  a.addEventListener("message", function(a) {
    if ("object" === typeof a.data) {
      var b = a.data;
      module$exports$omid$common$InternalMessage.isValidSerializedMessage(b) && (b = module$exports$omid$common$InternalMessage.deserialize(b), a.source && c.handleMessage(b, a.source));
    }
  });
};
$jscomp.inherits(module$exports$omid$common$PostMessageCommunication, module$exports$omid$common$Communication);
module$exports$omid$common$PostMessageCommunication.isCompatibleContext = function(a) {
  return !!(a && a.addEventListener && a.postMessage);
};
module$exports$omid$common$PostMessageCommunication.prototype.sendMessage = function(a, b) {
  b = void 0 === b ? this.to : b;
  if (!b) {
    throw Error("Message destination must be defined at construction time or when sending the message.");
  }
  b.postMessage(a.serialize(), "*");
};
var module$exports$omid$common$DetectOmid = {OMID_PRESENT_FRAME_NAME:"omid_v1_present", isOmidPresent:function(a) {
  try {
    return a.frames ? !!a.frames[module$exports$omid$common$DetectOmid.OMID_PRESENT_FRAME_NAME] : !1;
  } catch (b) {
    return !1;
  }
}, declareOmidPresence:function(a) {
  a.frames && a.document && (module$exports$omid$common$DetectOmid.OMID_PRESENT_FRAME_NAME in a.frames || (null == a.document.body && module$exports$omid$common$DetectOmid.isMutationObserverAvailable_(a) ? module$exports$omid$common$DetectOmid.registerMutationObserver_(a) : a.document.body ? module$exports$omid$common$DetectOmid.appendPresenceIframe_(a) : a.document.write('<iframe style="display:none" ' + ('id="' + module$exports$omid$common$DetectOmid.OMID_PRESENT_FRAME_NAME + '"') + (' name="' + 
  module$exports$omid$common$DetectOmid.OMID_PRESENT_FRAME_NAME + '">') + "</iframe>")));
}, appendPresenceIframe_:function(a) {
  var b = a.document.createElement("iframe");
  b.id = module$exports$omid$common$DetectOmid.OMID_PRESENT_FRAME_NAME;
  b.name = module$exports$omid$common$DetectOmid.OMID_PRESENT_FRAME_NAME;
  b.style.display = "none";
  a.document.body.appendChild(b);
}, isMutationObserverAvailable_:function(a) {
  return "MutationObserver" in a;
}, registerMutationObserver_:function(a) {
  var b = new MutationObserver(function(c) {
    c.forEach(function(c) {
      "BODY" === c.addedNodes[0].nodeName && (module$exports$omid$common$DetectOmid.appendPresenceIframe_(a), b.disconnect());
    });
  });
  b.observe(a.document.documentElement, {childList:!0});
}};
var module$exports$omid$common$serviceCommunication = {resolveTopWindowContext:function(a) {
  "undefined" === typeof a && "undefined" !== typeof window && window && (a = window);
  if ("undefined" === typeof a || !a || "undefined" === typeof a.top || !a.top) {
    return module$exports$omid$common$OmidGlobalProvider.omidGlobal;
  }
  if (a === a.top) {
    return a;
  }
  try {
    var b = a.top;
    return "undefined" === typeof b.location.hostname ? a : "" === b.x || "" !== b.x ? b : a;
  } catch (c) {
    return a;
  }
}};
function module$contents$omid$common$serviceCommunication_getUnobfuscatedKey(a, b) {
  return b.reduce(function(a, b) {
    return a && a[b];
  }, a);
}
module$exports$omid$common$serviceCommunication.startServiceCommunication = function(a, b, c) {
  c = void 0 === c ? module$exports$omid$common$DetectOmid.isOmidPresent : c;
  return (b = module$contents$omid$common$serviceCommunication_getUnobfuscatedKey(a, b)) ? new module$exports$omid$common$DirectCommunication(b) : a.top && c(a.top) ? new module$exports$omid$common$PostMessageCommunication(a, a.top) : null;
};
var module$exports$omid$common$version = {ApiVersion:"1.0", Version:"1.2.15-iab990"};
var module$contents$omid$sessionClient$AdSession_SESSION_CLIENT_VERSION = module$exports$omid$common$version.Version, module$exports$omid$sessionClient$AdSession = function(a, b) {
  b = void 0 === b ? (0,module$exports$omid$common$serviceCommunication.startServiceCommunication)((0,module$exports$omid$common$serviceCommunication.resolveTopWindowContext)(window), ["omid", "v1_SessionServiceCommunication"]) : b;
  module$exports$omid$common$argsChecker.assertNotNullObject("AdSession.context", a);
  this.context = a;
  this.impressionOccurred_ = !1;
  this.communication_ = b;
  this.isSessionRunning_ = this.hasVideoEvents_ = this.hasAdEvents_ = !1;
  this.callbackMap_ = {};
  this.communication_ && (this.communication_.onMessage = this.handleMessage_.bind(this), this.sendOneWayMessage("setClientInfo", module$exports$omid$common$version.Version, this.context.partner.name, this.context.partner.version));
  this.injectVerificationScripts_(a.verificationScriptResources);
  this.sendSlotElement_(a.slotElement);
  this.sendVideoElement_(a.videoElement);
  this.watchSessionEvents_();
};
module$exports$omid$sessionClient$AdSession.prototype.isSupported = function() {
  return !!this.communication_;
};
module$exports$omid$sessionClient$AdSession.prototype.registerSessionObserver = function(a) {
  this.sendMessage("registerSessionObserver", a);
};
module$exports$omid$sessionClient$AdSession.prototype.error = function(a, b) {
  this.sendOneWayMessage("sessionError", a, b);
};
module$exports$omid$sessionClient$AdSession.prototype.registerAdEvents = function() {
  if (this.hasAdEvents_) {
    throw Error("AdEvents already registered.");
  }
  this.hasAdEvents_ = !0;
  this.sendOneWayMessage("registerAdEvents");
};
module$exports$omid$sessionClient$AdSession.prototype.registerVideoEvents = function() {
  if (this.hasVideoEvents_) {
    throw Error("VideoEvents already registered.");
  }
  this.hasVideoEvents_ = !0;
  this.sendOneWayMessage("registerVideoEvents");
};
module$exports$omid$sessionClient$AdSession.prototype.sendOneWayMessage = function(a, b) {
  for (var c = [], d = 1; d < arguments.length; ++d) {
    c[d - 1] = arguments[d];
  }
  this.sendMessage.apply(this, [].concat([a, null], $jscomp.arrayFromIterable(c)));
};
module$exports$omid$sessionClient$AdSession.prototype.sendMessage = function(a, b, c) {
  for (var d = [], e = 2; e < arguments.length; ++e) {
    d[e - 2] = arguments[e];
  }
  this.isSupported() && (e = this.communication_.generateGuid(), b && (this.callbackMap_[e] = b), d = new module$exports$omid$common$InternalMessage(e, "SessionService." + a, module$exports$omid$common$version.Version, (0,module$exports$omid$common$ArgsSerDe.serializeMessageArgs)(module$exports$omid$common$version.Version, d)), this.communication_.sendMessage(d));
};
module$exports$omid$sessionClient$AdSession.prototype.handleMessage_ = function(a, b) {
  b = a.method;
  var c = a.guid;
  a = a.args;
  if ("response" === b && this.callbackMap_[c]) {
    var d = (0,module$exports$omid$common$ArgsSerDe.deserializeMessageArgs)(module$exports$omid$common$version.Version, a);
    this.callbackMap_[c].apply(this, d);
  }
  "error" === b && window.console && module$exports$omid$common$logger.error(a);
};
module$exports$omid$sessionClient$AdSession.prototype.assertSessionRunning = function() {
  if (!this.isSessionRunning_) {
    throw Error("Session not started.");
  }
};
module$exports$omid$sessionClient$AdSession.prototype.impressionOccurred = function() {
  this.impressionOccurred_ = !0;
};
module$exports$omid$sessionClient$AdSession.prototype.injectVerificationScripts_ = function(a) {
  a && (a = a.map(function(a) {
    return {resourceUrl:a.resourceUrl, vendorKey:a.vendorKey, verificationParameters:a.verificationParameters};
  }), this.sendOneWayMessage("injectVerificationScriptResources", a));
};
module$exports$omid$sessionClient$AdSession.prototype.sendSlotElement_ = function(a) {
  null != a && (this.communication_.isDirectCommunication() ? this.sendOneWayMessage("setSlotElement", a) : this.error(module$exports$omid$common$constants.ErrorType.GENERIC, "Session Client setSlotElement called when communication is not direct"));
};
module$exports$omid$sessionClient$AdSession.prototype.sendVideoElement_ = function(a) {
  null != a && (this.communication_.isDirectCommunication() ? this.sendOneWayMessage("setVideoElement", a) : this.error(module$exports$omid$common$constants.ErrorType.GENERIC, "Session Client setVideoElement called when communication is not direct"));
};
module$exports$omid$sessionClient$AdSession.prototype.setElementBounds = function(a) {
  module$exports$omid$common$argsChecker.assertNotNullObject("AdSession.elementBounds", a);
  this.sendOneWayMessage("setElementBounds", a);
};
module$exports$omid$sessionClient$AdSession.prototype.watchSessionEvents_ = function() {
  var a = this;
  this.isSupported && this.registerSessionObserver(function(b) {
    b.type === module$exports$omid$common$constants.AdEventType.SESSION_START && (a.isSessionRunning_ = !0);
    b.type === module$exports$omid$common$constants.AdEventType.SESSION_FINISH && (a.isSessionRunning_ = !1);
  });
};
(0,module$exports$omid$common$exporter.packageExport)("OmidSessionClient.AdSession", module$exports$omid$sessionClient$AdSession);
var module$exports$omid$sessionClient$AdEvents = function(a) {
  module$exports$omid$common$argsChecker.assertNotNullObject("AdEvents.adSession", a);
  try {
    a.registerAdEvents(), this.adSession = a;
  } catch (b) {
    throw Error("AdSession already has an ad events instance registered");
  }
};
module$exports$omid$sessionClient$AdEvents.prototype.impressionOccurred = function() {
  this.adSession.assertSessionRunning();
  this.adSession.impressionOccurred();
  this.adSession.sendOneWayMessage("impressionOccurred");
};
(0,module$exports$omid$common$exporter.packageExport)("OmidSessionClient.AdEvents", module$exports$omid$sessionClient$AdEvents);
var module$exports$omid$sessionClient$OmidVersion = function(a, b) {
  module$exports$omid$common$argsChecker.assertTruthyString("OmidVersion.semanticVersion", a);
  module$exports$omid$common$argsChecker.assertTruthyString("OmidVersion.apiLevel", b);
};
(0,module$exports$omid$common$exporter.packageExport)("OmidSessionClient.OmidVersion", module$exports$omid$sessionClient$OmidVersion);
var module$exports$omid$common$VastProperties = function(a, b, c, d) {
  this.isSkippable = a;
  this.skipOffset = b;
  this.isAutoPlay = c;
  this.position = d;
};
var module$exports$omid$sessionClient$VastPropertiesExports = {};
(0,module$exports$omid$common$exporter.packageExport)("OmidSessionClient.VastProperties", module$exports$omid$common$VastProperties);
var module$exports$omid$sessionClient$VideoEvents = function(a) {
  module$exports$omid$common$argsChecker.assertNotNullObject("VideoEvents.adSession", a);
  try {
    a.registerVideoEvents(), this.adSession = a;
  } catch (b) {
    throw Error("AdSession already has a video events instance registered");
  }
};
module$exports$omid$sessionClient$VideoEvents.prototype.loaded = function(a) {
  module$exports$omid$common$argsChecker.assertNotNullObject("VideoEvents.loaded.vastProperties", a);
  this.adSession.sendOneWayMessage("loaded", a);
};
module$exports$omid$sessionClient$VideoEvents.prototype.start = function(a, b) {
  module$exports$omid$common$argsChecker.assertNumber("VideoEvents.start.duration", a);
  module$exports$omid$common$argsChecker.assertNumberBetween("VideoEvents.start.videoPlayerVolume", b, 0, 1);
  this.adSession.sendOneWayMessage("start", a, b);
};
module$exports$omid$sessionClient$VideoEvents.prototype.firstQuartile = function() {
  this.adSession.sendOneWayMessage("firstQuartile");
};
module$exports$omid$sessionClient$VideoEvents.prototype.midpoint = function() {
  this.adSession.sendOneWayMessage("midpoint");
};
module$exports$omid$sessionClient$VideoEvents.prototype.thirdQuartile = function() {
  this.adSession.sendOneWayMessage("thirdQuartile");
};
module$exports$omid$sessionClient$VideoEvents.prototype.complete = function() {
  this.adSession.sendOneWayMessage("complete");
};
module$exports$omid$sessionClient$VideoEvents.prototype.pause = function() {
  this.adSession.sendOneWayMessage("pause");
};
module$exports$omid$sessionClient$VideoEvents.prototype.resume = function() {
  this.adSession.sendOneWayMessage("resume");
};
module$exports$omid$sessionClient$VideoEvents.prototype.bufferStart = function() {
  this.adSession.sendOneWayMessage("bufferStart");
};
module$exports$omid$sessionClient$VideoEvents.prototype.bufferFinish = function() {
  this.adSession.sendOneWayMessage("bufferFinish");
};
module$exports$omid$sessionClient$VideoEvents.prototype.skipped = function() {
  this.adSession.sendOneWayMessage("skipped");
};
module$exports$omid$sessionClient$VideoEvents.prototype.volumeChange = function(a) {
  module$exports$omid$common$argsChecker.assertNumberBetween("VideoEvents.volumeChange.videoPlayerVolume", a, 0, 1);
  this.adSession.sendOneWayMessage("volumeChange", a);
};
module$exports$omid$sessionClient$VideoEvents.prototype.playerStateChange = function(a) {
  module$exports$omid$common$argsChecker.assertNotNullObject("VideoEvents.playerStateChange.playerState", a);
  this.adSession.sendOneWayMessage("playerStateChange", a);
};
module$exports$omid$sessionClient$VideoEvents.prototype.adUserInteraction = function(a) {
  module$exports$omid$common$argsChecker.assertNotNullObject("VideoEvents.adUserInteraction.interactionType", a);
  this.adSession.sendOneWayMessage("adUserInteraction", a);
};
(0,module$exports$omid$common$exporter.packageExport)("OmidSessionClient.VideoEvents", module$exports$omid$sessionClient$VideoEvents);

}, typeof exports === 'undefined' ? undefined : exports));

////////////// OMSDK v1.0 .js /////////
;(function(omidGlobal) {
  console.log("###### OMSdk loading ... ######");

  'use strict';var k,aa='function'==typeof Object.defineProperties?Object.defineProperty:function(a,b,c){a!=Array.prototype&&a!=Object.prototype&&(a[b]=c.value)},l='undefined'!=typeof window&&window===this?this:'undefined'!=typeof global&&global?global:this;function ba(){ba=function(){};l.Symbol||(l.Symbol=ca)}var da=0;function ca(a){return'jscomp_symbol_'+(a||'')+da++}
function n(){ba();var a=l.Symbol.iterator;a||(a=l.Symbol.iterator=l.Symbol('iterator'));'function'!=typeof Array.prototype[a]&&aa(Array.prototype,a,{configurable:!0,writable:!0,value:function(){return ea(this)}});n=function(){}}function ea(a){var b=0;return fa(function(){return b<a.length?{done:!1,value:a[b++]}:{done:!0}})}function fa(a){n();a={next:a};a[l.Symbol.iterator]=function(){return this};return a}function p(a){n();ba();n();var b=a[Symbol.iterator];return b?b.call(a):ea(a)}
function q(a,b){function c(){}c.prototype=b.prototype;a.na=b.prototype;a.prototype=new c;a.prototype.constructor=a;for(var d in b)if('prototype'!=d)if(Object.defineProperties){var e=Object.getOwnPropertyDescriptor(b,d);e&&Object.defineProperty(a,d,e)}else a[d]=b[d]}function r(a){if(!(a instanceof Array)){a=p(a);for(var b,c=[];!(b=a.next()).done;)c.push(b.value);a=c}return a}
function t(a,b){if(b){var c=l;a=a.split('.');for(var d=0;d<a.length-1;d++){var e=a[d];e in c||(c[e]={});c=c[e]}a=a[a.length-1];d=c[a];b=b(d);b!=d&&null!=b&&aa(c,a,{configurable:!0,writable:!0,value:b})}}t('Object.assign',function(a){return a?a:function(a,c){for(var b=1;b<arguments.length;b++){var e=arguments[b];if(e)for(var f in e)Object.prototype.hasOwnProperty.call(e,f)&&(a[f]=e[f])}return a}});
t('Object.values',function(a){return a?a:function(a){var b=[],d;for(d in a)Object.prototype.hasOwnProperty.call(a,d)&&b.push(a[d]);return b}});t('Object.is',function(a){return a?a:function(a,c){return a===c?0!==a||1/a===1/c:a!==a&&c!==c}});t('Array.prototype.includes',function(a){return a?a:function(a,c){var b=this;b instanceof String&&(b=String(b));var e=b.length;for(c=c||0;c<e;c++)if(b[c]==a||Object.is(b[c],a))return!0;return!1}});function ha(a){return/\d+\.\d+\.\d+(-.*)?/.test(a)}
function ia(a){a=a.split('-')[0].split('.');for(var b=['1','0','3'],c=0;3>c;c++){var d=parseInt(a[c],10),e=parseInt(b[c],10);if(d>e)break;else if(d<e)return!1}return!0};function ja(a,b){return ha(a)&&ia(a)?b?b:[]:b&&'string'===typeof b?JSON.parse(b):[]};var u={ca:'loaded',ia:'start',Y:'firstQuartile',da:'midpoint',ja:'thirdQuartile',X:'complete',ea:'pause',ga:'resume',W:'bufferStart',V:'bufferFinish',ha:'skipped',la:'volumeChange',fa:'playerStateChange',S:'adUserInteraction'},ka={$:'generic',ka:'video'},la={F:'native',aa:'html'},ma={F:'native',ba:'javascript',NONE:'none'},na={U:'backgrounded',Z:'foregrounded'},oa={T:'app',ma:'web'};function v(a,b,c,d){this.b=a;this.method=b;this.version=c;this.a=d}function pa(a){return!!a&&void 0!==a.omid_message_guid&&void 0!==a.omid_message_method&&void 0!==a.omid_message_version&&'string'===typeof a.omid_message_guid&&'string'===typeof a.omid_message_method&&'string'===typeof a.omid_message_version&&(void 0===a.omid_message_args||void 0!==a.omid_message_args)}function qa(a){return new v(a.omid_message_guid,a.omid_message_method,a.omid_message_version,a.omid_message_args)}
function ra(a){var b={};b=(b.omid_message_guid=a.b,b.omid_message_method=a.method,b.omid_message_version=a.version,b);void 0!==a.a&&(b.omid_message_args=a.a);return b};function sa(a){this.b=a};function ta(a){var b=a.document.createElement('iframe');b.id='omid_v1_present';b.name='omid_v1_present';b.style.display='none';a.document.body.appendChild(b)}function ua(){var a=A,b=new MutationObserver(function(c){c.forEach(function(c){'BODY'===c.addedNodes[0].nodeName&&(ta(a),b.disconnect())})});b.observe(a.document.documentElement,{childList:!0})};function D(a){this.b=a;this.handleExportedMessage=D.prototype.c.bind(this)}q(D,sa);D.prototype.sendMessage=function(a,b){b=void 0===b?this.b:b;if(!b)throw Error('Message destination must be defined at construction time or when sending the message.');b.handleExportedMessage(ra(a),this)};D.prototype.c=function(a,b){pa(a)&&this.a&&this.a(qa(a),b)};function F(a){for(var b=[],c=0;c<arguments.length;++c)b[c-0]=arguments[c];va(function(){throw new (Function.prototype.bind.apply(Error,[null].concat(['Could not complete the test successfully - '],r(b))));},function(){return console.error.apply(console,[].concat(r(b)))})}function wa(a){for(var b=[],c=0;c<arguments.length;++c)b[c-0]=arguments[c];va(function(){},function(){return console.error.apply(console,[].concat(r(b)))})}
function va(a,b){'undefined'!==typeof jasmine&&jasmine?a():'undefined'!==typeof console&&console&&console.error&&b()};var Ba=eval('this'),A=function(){if('undefined'!==typeof omidGlobal&&omidGlobal)return omidGlobal;if('undefined'!==typeof global&&global)return global;if('undefined'!==typeof window&&window)return window;if('undefined'!==typeof Ba&&Ba)return Ba;throw Error('Could not determine global object context.');}();function G(a,b){this.b=b=b?b:A;var c=this;a.addEventListener('message',function(a){if('object'===typeof a.data){var b=a.data;pa(b)&&a.source&&c.a&&c.a(qa(b),a.source)}})}q(G,sa);G.prototype.sendMessage=function(a,b){b=b?b:this.b;if(!b)throw Error('Message destination must be defined at construction time or when sending the message.');b.postMessage(ra(a),'*')};function Ca(a,b){this.y=this.x=0;this.width=a;this.height=b};function I(a,b){this.x=null!=a.x?a.x:a.left;this.y=null!=a.y?a.y:a.top;this.width=a.width;this.height=a.height;this.endX=this.x+this.width;this.endY=this.y+this.height;this.adSessionId=a.adSessionId||void 0;this.isFriendlyObstructionFor=a.isFriendlyObstructionFor||[];this.clipsToBounds=void 0!==a.clipsToBounds?!0===a.clipsToBounds:!0;this.childViews=a.childViews||[];this.isCreative=a.isCreative||!1;this.a=b}function Da(a){var b={};return b.width=a.width,b.height=a.height,b}
function J(a){var b={};return Object.assign({},Da(a),(b.x=a.x,b.y=a.y,b))}function K(a){var b=J(a),c={};return Object.assign({},b,(c.endX=a.endX,c.endY=a.endY,c))}function Ea(a,b,c){a.x+=b;a.y+=c;a.endX+=b;a.endY+=c}I.prototype.v=function(a){if(!a)return!1;a=J(a);var b=a.y,c=a.width,d=a.height;return this.x===a.x&&this.y===b&&this.width===c&&this.height===d};function Fa(a){return a.width*a.height};function Ga(a,b){a=J(a);for(var c=[],d=[],e=0;e<b.length;e++){var f=J(b[e]),h=Math.max(a.y,f.y),g=Math.min(a.x+a.width,f.x+f.width),m=Math.min(a.y+a.height,f.y+f.height);L(c,Math.max(a.x,f.x));L(c,g);L(d,h);L(d,m)}c=c.sort(function(a,b){return a-b});d=d.sort(function(a,b){return a-b});return{P:c,R:d}}function L(a,b){-1===a.indexOf(b)&&a.push(b)};function Ha(){this.a=this.b=this.l=this.j=this.c=this.i=void 0;this.m=0;this.g=[];this.f=[];this.h=[]}Ha.prototype.v=function(a){return a?JSON.stringify(M(this))===JSON.stringify(M(a)):!1};
function M(a){var b=[],c={viewport:a.i,adView:{percentageInView:a.m,reasons:a.h}};if(a.b){c.adView.geometry=J(a.b);c.adView.onScreenGeometry=J(a.a);for(var d=0;d<a.f.length;d++)b.push(J(a.f[d]));c.adView.onScreenGeometry.obstructions=b;a.j&&a.l&&(c.adView.containerGeometry=J(a.j),c.adView.onScreenContainerGeometry=J(a.l),c.adView.measuringElement=!0)}return c}
function Ia(a,b){b=Da(b);a.i={};a.i.width=b.width;a.i.height=b.height;a.c={};a.c.x=0;a.c.y=0;a.c.width=b.width;a.c.height=b.height;a.c.endX=b.width;a.c.endY=b.height}function Ja(a,b){var c={};c.x=Math.max(a.x,b.x);c.y=Math.max(a.y,b.y);c.endX=Math.min(a.endX,b.endX);c.endY=Math.min(a.endY,b.endY);c.width=Math.max(0,c.endX-c.x);c.height=Math.max(0,c.endY-c.y);return c}function Ka(a,b){return a.width<b.width||a.height<b.height}
function La(a){var b=Fa(a.b);if(b){var c=Fa(a.a);var d=a.f,e=0;if(0<d.length){var f=Ga(a.a,d),h=f.P;f=f.R;for(var g=0;g<h.length-1;g++)for(var m=(h[g]+(h[g]+1))/2,N=h[g+1]-h[g],y=0;y<f.length-1;y++){for(var w=(f[y]+(f[y]+1))/2,H=f[y+1]-f[y],B=!1,C=0;C<d.length;C++){var x=J(d[C]);if(x.x<m&&x.x+x.width>m&&x.y<w&&x.y+x.height>w){B=!0;break}}B&&(e+=Math.round(N)*Math.round(H))}}b=Math.round((c-e)/b*100);a.m=Math.max(b,0)}}
function O(a,b){for(var c=!1,d=0;d<a.h.length;d++)a.h[d]===b&&(c=!0);c||a.h.push(b)};function Ma(){}function Na(a,b,c,d){var e=new Ha(0);b=new I(b,!1);Ia(e,b);Oa(a,b,e,d);if('backgrounded'===c)O(e,'backgrounded');else if(e.b){for(a=0;a<e.g.length;a++){c=e.g[a];if(0!==c.width&&0!==c.height&&e.a){d=K(e.a);b=d.y;var f=d.endX,h=d.endY;c=!(c.endX<=d.x||c.x>=f||c.endY<=b||c.y>=h)}else c=!1;if(c){a:{c=e.g[a];for(d=0;d<e.f.length;d++)if(e.f[d].v(c)){c=!0;break a}c=!1}c=!c}c&&(O(e,'obstructed'),e.f.push(e.g[a]))}La(e)}else O(e,'notFound');return e}
function Oa(a,b,c,d){var e=b.isCreative?!0:b.adSessionId===d;if(e){c.b=b;var f=K(c.b);a=Ja(c.c,f);Ka(a,f)&&O(c,'clipped');c.a=new I(a,!1)}else if(f=!0,b.a&&(f=-1!==b.isFriendlyObstructionFor.indexOf(d)?!1:!1===b.clipsToBounds),f)for(var h=b.childViews,g=0;g<h.length;g++)f=!!c.b,Oa(a,new I(h[g],f),c,d);!e&&c.b&&(b.a?-1!==b.isFriendlyObstructionFor.indexOf(d)||c.g.push(b):(e=K(b),d=K(c.a),J(c.a),a=c.a,0!==a.width&&0!==a.height&&b.clipsToBounds&&(b=Ja(d,e),Ka(b,d)&&(O(c,'clipped'),c.a=new I(b,!1)))))}
;function Pa(){return{apiVersion:'1.0',accessMode:'limited',environment:'app',omidJsInfo:{omidImplementer:'omsdk',serviceVersion:'1.2.15-iab990'}}}function Qa(){this.adSessionId=null;this.c=Pa();this.l='foregrounded';this.b=this.a='none';this.f=this.h=this.g=this.C=this.A=this.m=null;this.s=!0}var P;function Q(){P||(P=new Qa);return P};function Ra(a,b){this.b=a;this.c=b}l.Object.defineProperties(Ra.prototype,{a:{configurable:!0,enumerable:!0,get:function(){return this.b}},origin:{configurable:!0,enumerable:!0,get:function(){return this.c}}});function Sa(){this.b=[];this.c=[];this.g=[];this.h=[];this.f={};this.a=Q()}function Ta(a){a.b=[];a.c=[];a.g=[];a.h=[];a.f={};P.adSessionId=null;P.c=Pa();P.w=void 0;P.B=void 0;P.i=null;P.u=null;P.j=null;P.l='foregrounded';P.a='none';P.b='none';P.m=null;P.A=null;P.C=null;P.g=null;P.h=null;P.f=null;P.s=!0}function Ua(a,b){a.a&&a.a.adSessionId&&!1!==Va(b)&&a.g.filter(function(a){return a.type===b.a.type}).forEach(function(c){return a.i(c.o,b.a)})}
function Wa(a,b,c){a.a&&a.a.adSessionId&&a.b.filter(function(a){return a.a.type===b&&Va(a)}).map(function(a){return a.a}).forEach(c)}function Va(a){var b=a.a.type,c=-1!==Object.values(u).indexOf(b)&&'volumeChange'!==b;return'impression'===b?a.origin===Q().b:c?a.origin===Q().a:!0}function Xa(a,b,c){'video'===b?Ya(a,c):(a.g.push({type:b,o:c}),Wa(a,b,c))}function Ya(a,b){Object.keys(u).forEach(function(c){c=u[c];a.g.push({type:c,o:b});Wa(a,c,b)})}
function Za(a,b,c){a.h.push({O:c,o:b});a.c.forEach(function(d){var e=$a(d);'sessionStart'===d.a.type&&(e.data.verificationParameters=c&&a.f[c]);a.i(b,e)})}function ab(a){return a.b.some(function(a){return'impression'===a.a.type})||a.c.some(function(a){return'impression'===a.a.type})}function bb(a,b,c){var d=R(a,'sessionError','native',{errorType:b,message:c});a.c.push(d);a.h.forEach(function(b){a.i(b.o,d.a)})}
function cb(a,b){a.f=Object.assign(a.f,b);if(b=a.a.c){var c=R(a,'sessionStart','native',{context:b});a.c.push(c);a.h.forEach(function(b){var d=b.o,f=$a(c);b=b.O;f.data.verificationParameters=b&&a.f[b];a.i(d,f)},a);db(a)}}function eb(a){var b=a.h,c=R(a,'sessionFinish','native');a.c.push(c);Ta(a);b.forEach(function(b){return a.i(b.o,c.a)})}Sa.prototype.i=function(a,b){for(var c=[],d=1;d<arguments.length;++d)c[d-1]=arguments[d];try{a.apply(null,[].concat(r(c)))}catch(e){wa(e)}};
function fb(a,b){var c=a.a.C;var d=(d=Q().j)?M(d).viewport:void 0;var e=(e=Q().j)?M(e).adView:void 0;b=R(a,'impression',b,{mediaType:c,viewport:d,adView:e});a.b.push(b);Ua(a,b)}function gb(a,b,c,d){'start'!==b&&'volumeChange'!==b||null!=(d&&d.deviceVolume)||(d.deviceVolume=a.a.m);'start'!==b&&'volumeChange'!==b||null==(d&&d.videoPlayerVolume)||(a.a.A=d.videoPlayerVolume);b=R(a,b,c,d);a.b.push(b);Ua(a,b)}
function db(a){var b=a.a.a,c=a.b.filter(function(a){return Object.values(u).includes(a.a.type)&&a.origin===b}).map(function(a){return a.a}),d=a.a.adSessionId||'';c=p(c);for(var e=c.next();!e.done;e=c.next()){e=e.value;e.adSessionId||(e.adSessionId=d);for(var f=p(a.g),h=f.next();!h.done;h=f.next())h=h.value,h.type===e.type&&h.o(e)}}function hb(a,b){return'none'!==a.a.b&&a.a.b!==b?(F('Impression event is owned by '+(a.a.b+', not '+b+'.')),!1):!0}
function ib(a,b){return'none'!==a.a.a&&a.a.a!==b?(F('Video events are owned by '+(a.a.a+', not '+b+'.')),!1):!0}function R(a,b,c,d){return new Ra({adSessionId:a.a.adSessionId||'',timestamp:(new Date).getTime(),type:b,data:d},c)}function $a(a){a=a.a;return{adSessionId:a.adSessionId,timestamp:a.timestamp,type:a.type,data:a.data}};function jb(a,b,c){'container'===b&&void 0!==a.a.w&&a.a&&null!=a.a.adSessionId&&(a.a.i=Na(a.b,a.a.w,a.a.l,a.a.adSessionId));'creative'===b&&a.a.B&&(a.a.u=Na(a.b,a.a.B,a.a.l,a.a.adSessionId));if(a.a.i)if(a.a.u){b=new Ha(0);var d=a.a.i,e=a.a.u,f=d.i,h=d.b,g=d.a,m=e.b;e=e.a;f&&h&&g&&m&&e&&(Ia(b,f),b.j=new I(h,!1),b.l=new I(g,!1),b.g=Object.assign([],d.g),b.f=Object.assign([],d.f),b.h=Object.assign([],d.h),d=b.j.x,f=b.j.y,m=new I(m,!1),e=new I(e,!1),Ea(m,d,f),Ea(e,d,f),b.b=m,b.a=Ja(e,g),-1===b.h.indexOf('backgrounded')&&
La(b))}else b=a.a.i;else b=null;g=a.a.j;if(b&&!b.v(g)||c)g=M(b),c&&(g.adView.reasons=g.adView.reasons||[c]),c=a.c,g=R(c,'geometryChange','native',{viewport:g.viewport,adView:g.adView}),c.b.push(g),Ua(c,g),a.a.j=b};function kb(a,b,c){this.i=a;this.w=b;this.s=c;this.c=Q();this.b=null;this.a=this.g=void 0;this.u=!0;T(this)}function T(a){if(!a.b){var b;a:{if((b=a.i.document)&&b.getElementsByClassName&&(b=b.getElementsByClassName('omid-element'))){if(1==b.length){b=b[0];break a}1<b.length&&a.u&&(bb(a.s,'generic',"More than one element with 'omid-element' class name."),a.u=!1)}b=null}b&&(b.tagName&&'video'===b.tagName.toLowerCase()?a.c.h=b:a.c.g=b,lb(a))}}
function lb(a){a.c.h?(a.b=a.c.h,a.l()):a.c.g&&(a.b=a.c.g,'iframe'===a.b.tagName.toLowerCase()?a.c.f&&a.l():a.l())}function mb(a){a.a&&a.g&&(a.b.tagName&&'iframe'===a.b.tagName.toLowerCase()?a.c.f&&(a.a.isCreative=!1,nb(a),ob(a)):(a.c.f?(a.a.isCreative=!1,nb(a)):a.a.isCreative=!0,ob(a)))}function ob(a){a.c.B=a.g;jb(a.w,'creative')}
function nb(a){if(a.c.f){var b=new I(a.c.f,!1);Ea(b,a.a.x,a.a.y);b.clipsToBounds=!0;b.isCreative=!0;for(var c=0;c<a.a.childViews.length;c++)if(a.a.childViews[c].isCreative){a.a.childViews[c]=b;return}a.a.childViews.push(b)}};function pb(a,b,c){return rb(a,'setInterval')(b,c)}function sb(a,b){rb(a,'clearInterval')(b)}function tb(a,b){rb(a,'clearTimeout')(b)}function rb(a,b){return a.a&&a.a[b]?a.a[b]:ub(a,b)}
function vb(a,b,c,d){if(a.a.document&&a.a.document.body){var e=a.a.document.createElement('img');e.width=1;e.height=1;e.style.display='none';e.src=b;c&&e.addEventListener('load',function(){return c()});d&&e.addEventListener('error',function(){return d()});a.a.document.body.appendChild(e)}else ub(a,'sendUrl')(b,c,d)}function ub(a,b){if(a.a&&a.a.omidNative&&a.a.omidNative[b])return a.a.omidNative[b].bind(a.a.omidNative);throw Error('Native interface method "'+b+'" not found.');};function wb(a,b,c,d){kb.call(this,a,b,d);this.f=void 0;this.h=c}q(wb,kb);wb.prototype.m=function(){void 0!==this.f&&(sb(this.h,this.f),this.f=void 0)};wb.prototype.l=function(){var a=this;this.b?void 0===this.f&&(this.f=pb(this.h,function(){return xb(a)},200),xb(this)):this.f=void 0};
function xb(a){if(void 0!==a.f){var b=new I(new Ca(a.i.innerWidth,a.i.innerHeight),!1),c=a.b.getBoundingClientRect();if(null==c.x||isNaN(c.x))c.x=c.left;if(null==c.y||isNaN(c.y))c.y=c.top;c=new I(c,!1);b.v(a.g)&&c.v(a.a)||(a.a=c,a.a.clipsToBounds=!0,a.g=b,a.g.childViews.push(a.a),mb(a))}};function yb(a,b,c){kb.call(this,a,b,c);this.j=this.h=this.f=void 0}q(yb,kb);yb.prototype.m=function(){this.f&&this.f.disconnect();zb(this)};yb.prototype.l=function(){this.b&&(this.f||(this.f=Ab(this),this.f.observe(this.b)),Bb(this.b)&&Cb(this))};function zb(a){a.h&&(a.h.disconnect(),a.h=void 0);a.j&&((0,a.i.removeEventListener)('resize',a.j),a.j=void 0)}function Bb(a){a=a.getBoundingClientRect();return!a.width||!a.height}
function Ab(a){return new a.i.IntersectionObserver(function(b){try{if(b.length){for(var c=b[0],d=1;d<b.length;d++)b[d].time>c.time&&(c=b[d]);b=c;a.g=new I(b.rootBounds,!1);a.a=new I(b.boundingClientRect,!1);a.a.clipsToBounds=!0;a.g.childViews.push(a.a);mb(a)}}catch(e){a.m(),bb(a.s,'generic','Problem handling IntersectionObserver callback: '+e.message)}},{root:null,rootMargin:'0px',threshold:[0,.1,.2,.3,.4,.5,.6,.7,.8,.9,1]})}
function Cb(a){a.j||(a.j=function(){return Db(a)},(0,a.i.addEventListener)('resize',a.j));a.h||(a.h=new MutationObserver(function(){return Db(a)}),a.h.observe(a.b,{childList:!1,attributes:!0,subtree:!1}))}function Db(a){a.b&&!Bb(a.b)&&(a.f&&a.b&&(a.f.unobserve(a.b),a.f.observe(a.b)),zb(a))};function U(a){return'string'===typeof a}function V(a){return'object'===typeof a}function Eb(a){return'number'===typeof a&&!isNaN(a)&&0<=a}function W(a,b){return U(a)&&-1!==Object.values(b).indexOf(a)};function Fb(a,b,c){var d=this;c=c?c:A;this.b=a;this.a=b;this.g=c;this.c=[];this.f=!1;Gb(this,function(a){return Hb(d,a)})}function Gb(a,b){Za(a.b,b)}function Ib(a,b){Q().g=b;a.a&&lb(a.a)}function Jb(a,b){Q().h=b;a.a&&lb(a.a)}function Kb(a,b){Q().f=b;a.a&&lb(a.a);a.a&&mb(a.a)}Fb.prototype.error=function(a,b){bb(this.b,a,b)};
function X(a,b,c){'impression'==b?hb(a.b,'javascript')&&(fb(a.b,'javascript'),a.a&&T(a.a)):('loaded'==b?gb(a.b,b,'javascript',c):'javascript'===Q().a&&gb(a.b,b,'javascript',c),['loaded','start'].includes(b)&&a.a&&T(a.a))}
function Lb(a){if(a.f)if(a.g&&'undefined'!=typeof a.g.document){var b=a.c;a.c=[];for(var c=Q().s,d=0;d<b.length;d++){var e=b[d],f=d,h=e.resourceUrl,g=a.g.document,m=g.createElement('iframe');c&&(m.sandbox='allow-scripts');m.id='omid-verification-script-frame-'+f;m.style.display='none';m.srcdoc='<html><head>'+('<script type="text/javascript" src="'+h+'">\x3c/script>')+'</head><body></body></html>';g.body.appendChild(m);f=e.vendorKey;e=e.verificationParameters;f=void 0===f?'':f;e=void 0===e?'':e;f&&
'string'===typeof f&&''!==f&&e&&'string'===typeof e&&''!==e&&(a.b.f[f]=e)}}else F('OMID Session Client is not running within a window')}function Mb(a,b){a.c.push.apply(a.c,[].concat(r(b)));Lb(a)}function Hb(a,b){'sessionStart'===b.type&&(a.f=!0,Lb(a));'sessionFinish'===b.type&&(a.f=!1,Gb(a,function(b){return Hb(a,b)}))}function Nb(a,b,c){var d=Q().c||{};d.omidJsInfo=Object.assign({},d.omidJsInfo,{sessionClientVersion:a,partnerName:b,partnerVersion:c});Q().c=d};function Ob(a,b){b=b?b:omidGlobal;this.a=a;this.f=b;this.b=new D;this.f.omid=this.f.omid||{};this.f.omid.v1_SessionServiceCommunication=this.b;this.c=b&&b.addEventListener&&b.postMessage?new G(b):null;this.b.a=this.g.bind(this);this.c&&(this.c.a=this.h.bind(this))}Ob.prototype.g=function(a,b){Pb(this,a,b,this.b)};Ob.prototype.h=function(a,b){Pb(this,a,b,this.c)};
function Pb(a,b,c,d){function e(a){for(var b=[],e=0;e<arguments.length;++e)b[e-0]=arguments[e];b=new v(f,'response',g,ha(g)&&ia(g)?b:JSON.stringify(b));d.sendMessage(b,c)}var f=b.b,h=b.method,g=b.version;b=ja(g,b.a);try{switch(h){case 'SessionService.registerAdEvents':hb(a.a.b,'javascript');break;case 'SessionService.registerVideoEvents':ib(a.a.b,'javascript');break;case 'SessionService.registerSessionObserver':Gb(a.a,e);break;case 'SessionService.setSlotElement':var m=p(b).next().value;Ib(a.a,m);
break;case 'SessionService.setVideoElement':var N=p(b).next().value;Jb(a.a,N);break;case 'SessionService.setElementBounds':var y=p(b).next().value;Kb(a.a,y);break;case 'SessionService.impressionOccurred':X(a.a,'impression');break;case 'SessionService.loaded':var w=p(b).next().value,H={skippable:w.isSkippable,autoPlay:w.isAutoPlay,position:w.position};w.isSkippable&&(H.skipOffset=w.skipOffset);X(a.a,'loaded',H);break;case 'SessionService.start':var B=p(b),C=B.next().value,x=B.next().value;X(a.a,'start',
{duration:C,videoPlayerVolume:x});break;case 'SessionService.firstQuartile':X(a.a,'firstQuartile');break;case 'SessionService.midpoint':X(a.a,'midpoint');break;case 'SessionService.thirdQuartile':X(a.a,'thirdQuartile');break;case 'SessionService.complete':X(a.a,'complete');break;case 'SessionService.pause':X(a.a,'pause');break;case 'SessionService.resume':X(a.a,'resume');break;case 'SessionService.bufferStart':X(a.a,'bufferStart');break;case 'SessionService.bufferFinish':X(a.a,'bufferFinish');break;
case 'SessionService.skipped':X(a.a,'skipped');break;case 'SessionService.volumeChange':var xa={videoPlayerVolume:p(b).next().value};X(a.a,'volumeChange',xa);break;case 'SessionService.playerStateChange':var ya={state:p(b).next().value};X(a.a,'playerStateChange',ya);break;case 'SessionService.adUserInteraction':var za={interactionType:p(b).next().value};X(a.a,'adUserInteraction',za);break;case 'SessionService.setClientInfo':var S=p(b),Aa=S.next().value,z=S.next().value,Yb=S.next().value;Nb(Aa,z,Yb);
var Zb=Q().c.omidJsInfo.serviceVersion;e(Zb);break;case 'SessionService.injectVerificationScriptResources':var $b=p(b).next().value;Mb(a.a,$b);break;case 'SessionService.sessionError':var qb=p(b),ac=qb.next().value,bc=qb.next().value;a.a.error(ac,bc)}}catch(E){d.sendMessage(new v(f,'error',g,'\n              name: '+E.name+'\n              message: '+E.message+'\n              filename: '+E.filename+'\n              lineNumber: '+E.lineNumber+'\n              columnNumber: '+E.columnNumber+'\n              stack: '+
E.stack+'\n              toString(): '+E.toString()+'\n          '),c)}};function Y(a,b,c,d,e){this.b=a;this.f=b;this.g=c;this.c=e;this.a=Q()}k=Y.prototype;
k.H=function(a){if(a&&V(a)&&W(a.impressionOwner,ma)&&(!('videoEventsOwner'in a&&null!=a.videoEventsOwner)||W(a.videoEventsOwner,ma))){var b=a.videoEventsOwner;this.a.C=null==b||'none'===b?'display':'video';a&&null!=a.isolateVerificationScripts&&'boolean'===typeof a.isolateVerificationScripts&&(this.a.s=a.isolateVerificationScripts);b=this.b;var c=a.impressionOwner;a=a.videoEventsOwner;ab(b)?F('Impression event has occcured before the event owners have been registered.'):(b.a.b!==c&&'none'===b.a.b&&
(b.a.b=c),b.a.a!==a&&'none'===b.a.a&&(b.a.a=a))}};
k.N=function(a,b,c){var d;if(d=V(b)){if(d=W(b.environment,oa)&&W(b.adSessionType,la))d=b.omidNativeInfo,d=V(d)?U(d.partnerName)&&U(d.partnerVersion):!1;d&&(d=b.app,d=V(d)?U(d.libraryVersion)&&U(d.appId):!1)}d&&(this.a.adSessionId=a,a=b,b=this.a.c||{},a.omidJsInfo=Object.assign({},b.omidJsInfo||{},a.omidJsInfo||{}),b=a=Object.assign({},b,a),this.a.s||(this.a.h?(b.videoElement=this.a.h,b.accessMode='full'):this.a.g&&(b.slotElement=this.a.g,b.accessMode='full')),this.a.c=a,cb(this.b,c),this.c&&T(this.c))};
k.G=function(){eb(this.b);this.c.m()};k.L=function(a){V(a)&&Eb(a.x)&&Eb(a.y)&&Eb(a.width)&&Eb(a.height)&&(this.a.w=a,jb(this.f,'container'))};k.M=function(a){W(a,na)&&(this.a.l=a,'backgrounded'===a?jb(this.f,'container','backgrounded'):jb(this.f,'container'))};k.J=function(a){'impression'===a&&(this.D(),this.c&&T(this.c))};k.D=function(){hb(this.b,'native')&&fb(this.b,'native')};k.error=function(a,b){W(a,ka)&&bb(this.b,a,b)};
k.I=function(a,b){ib(this.b,'native')&&W(a,u)&&(void 0===b||V(b))&&gb(this.b,a,'native',b)};k.K=function(a){if('none'!==this.b.a.a&&'number'===typeof a&&!isNaN(a)){this.a.m=a;a=this.g;var b=a.a.A;null!=b&&gb(a.b,'volumeChange','native',{videoPlayerVolume:b,deviceVolume:a.a.m})}};Y.prototype.startSession=Y.prototype.N;Y.prototype.error=Y.prototype.error;Y.prototype.finishSession=Y.prototype.G;Y.prototype.publishAdEvent=Y.prototype.J;Y.prototype.publishImpressionEvent=Y.prototype.D;
Y.prototype.publishVideoEvent=Y.prototype.I;Y.prototype.setNativeViewHierarchy=Y.prototype.L;Y.prototype.setState=Y.prototype.M;Y.prototype.setDeviceVolume=Y.prototype.K;Y.prototype.init=Y.prototype.H;function Qb(a,b,c){c=c?c:A;this.g=a;this.a=b;this.h={};this.f={};this.c=new D;c.omid=c.omid||{};c.omid.v1_VerificationServiceCommunication=this.c;this.b=null;c&&c.addEventListener&&c.postMessage&&(this.b=new G(c));this.c.a=this.i.bind(this);this.b&&(this.b.a=this.j.bind(this))}function Rb(a,b,c,d){vb(a.a,b,c,d)}function Sb(a,b,c,d){ub(a.a,'downloadJavaScriptResource')(b,c,d)}Qb.prototype.j=function(a,b){this.b&&Tb(this,a,b,this.b)};Qb.prototype.i=function(a,b){Tb(this,a,b,this.c)};
function Tb(a,b,c,d){function e(a){for(var b=[],e=0;e<arguments.length;++e)b[e-0]=arguments[e];b=new v(f,'response',g,ha(g)&&ia(g)?b:JSON.stringify(b));d.sendMessage(b,c)}var f=b.b,h=b.method,g=b.version;b=ja(g,b.a);try{switch(h){case 'VerificationService.addEventListener':var m=p(b).next().value;Xa(a.g,m,e);break;case 'VerificationService.addSessionListener':var N=p(b).next().value;Za(a.g,e,N);break;case 'VerificationService.sendUrl':var y=p(b).next().value;Rb(a,y,function(){return e(!0)},function(){return e(!1)});
break;case 'VerificationService.setTimeout':var w=p(b),H=w.next().value,B=w.next().value;a.h[H]=rb(a.a,'setTimeout')(e,B);break;case 'VerificationService.clearTimeout':var C=p(b).next().value;tb(a.a,a.h[C]);break;case 'VerificationService.setInterval':var x=p(b),xa=x.next().value,ya=x.next().value;a.f[xa]=pb(a.a,e,ya);break;case 'VerificationService.clearInterval':var za=p(b).next().value;sb(a.a,a.f[za]);break;case 'VerificationService.injectJavaScriptResource':var S=p(b).next().value;Sb(a,S,function(a){return e(!0,
a)},function(){return e(!1)});break;case 'VerificationService.getVersion':p(b).next();var Aa=Q().c.omidJsInfo;e(Aa.serviceVersion)}}catch(z){d.sendMessage(new v(f,'error',g,'\n              name: '+z.name+'\n              message: '+z.message+'\n              filename: '+z.filename+'\n              lineNumber: '+z.lineNumber+'\n              columnNumber: '+z.columnNumber+'\n              stack: '+z.stack+'\n              toString(): '+z.toString()+'\n          '),c)}};var Z=new Sa,Ub=new function(){var a;this.a=a=void 0===a?omidGlobal:a};new Qb(Z,Ub);var Vb=new function(){var a=new Ma;this.c=Z;this.b=a;this.a=Q()},Wb;if(A){var Xb=A;Wb=Xb.IntersectionObserver&&Xb.MutationObserver?new yb(A,Vb,Z):new wb(A,Vb,Ub,Z)}else Wb=null;var cc=Wb,dc=new Fb(Z,cc);A.omidBridge=new Y(Z,Vb,new function(){var a=Q();this.b=Z;this.a=a},new function(){},cc);new Ob(dc);
if(A.frames&&A.document&&!('omid_v1_present'in A.frames)){var ec;if(ec=!A.document.body)ec='MutationObserver'in A;ec?ua():A.document.body?ta(A):A.document.write('<iframe style="display:none" id="omid_v1_present" name="omid_v1_present"></iframe>')};
console.log("###### OMSdk Loaded  ######");
}).call(this, this);

