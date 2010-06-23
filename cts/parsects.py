#!/usr/bin/python
#Filename: parsercts.py
#Jingtao: 2010-06-23

import xml.sax
import csv
import sys

target_mapping = {} #to store case info of the test result

class resultHandler(xml.sax.handler.ContentHandler):
  global target_mapping

  def __init__(self, isTarget):
     self.packageName = ""
     self.caseName = ""
     self.errMsg = ""
     self.testResult = ""

     self.isTarget = isTarget
     self.mapping = {}
     self.header = {}

  def startElement(self, name, attrs):
     if name == "TestResult":
         self.header["test endtime"] = attrs["endtime"]
         self.header["test starttime"] = attrs["starttime"]
         self.header["CTS plan version"] = attrs["version"]
     elif name == "BuildInfo":
         self.header["androidPlatformVersion"] = attrs["androidPlatformVersion"]
         self.header["buildName"] = attrs["buildName"]
         self.header["build_brand"] = attrs["build_brand"]
         self.header["build_device"] = attrs["build_device"]
         self.header["build_fingerprint"] = attrs["build_fingerprint"]
         self.header["build_model"] = attrs["build_model"]
         self.header["build_type"] = attrs["build_type"]
         self.header["imei"] = attrs["imei"]
         self.header["imsi"] = attrs["imsi"]
         self.header["locales"] = "\"" + attrs["locales"] + "\""
         self.header["network"] = attrs["network"]
     elif name == "Cts":
         self.header["CTS version"] = attrs["version"]

     elif name == "TestSuite" or name == "TestCase":
	 if self.packageName == '':
	      self.packageName = attrs['name']
	 else:
              self.packageName += '.' + attrs['name']

     elif name == 'Test':
         self.caseName = attrs['name']
         self.testResult = attrs['result']
         if self.testResult == "fail" and attrs.get('KnownFailure'): 
	      self.errMsg = attrs['KnownFailure']

     elif name == 'FailedScene':
         self.errMsg = attrs['message'].strip()


  def endElement(self, name):
    if name == 'Test':
      if self.isTarget:#record only not passed result if is target
        if self.testResult != "pass" and self.testResult != "notExecuted":
          self.mapping[self.packageName + "#" + self.caseName] = [self.testResult, self.errMsg]
      else:
        if self.packageName + "#" + self.caseName in target_mapping:#record all the result if is base and the result found in target
          self.mapping[self.packageName + "#" + self.caseName] = [self.testResult, self.errMsg]
      #clear the variables relate to current case
      self.caseName = ""
      self.errMsg = ""
      self.testResult = ""
    elif name == "TestCase" or name == "TestSuite":#popup case name
      offset = self.packageName[::-1].find(".") + 1
      self.packageName = self.packageName[:-offset]
    elif name == "TestPackage":#clear package name
      self.packageName = ""


#para input
def print_useage():
    cmd = sys.argv[0]
    print "usage: " + cmd + " <base> <target> [output file] [revert base and target] [list target header]"
    print "example: "
    print "       " + cmd + " dragon-r10000/testResult.xml" + " dragon-r12000/testResult.xml"
    print "       " + cmd + " nexus-one/testResult.xml" + " dragon-r12000/testResult.xml" + " compare.csv"
    print "       " + cmd + " nexus-one/testResult.xml" + " dragon-r12000/testResult.xml" + " compare.csv 0 1 (0 1 means don't revert base and target, but list header of target)"


args = sys.argv[1:]
if (len(args) < 2):
    print_useage() 
else:
    baseResult = sys.argv[1]
    targetResult = sys.argv[2]
    if (len(args) >= 4) and (sys.argv[4] == "1"):
        baseResult = sys.argv[2]
        targetResult = sys.argv[1]

    parser = xml.sax.make_parser()
    handler_base = resultHandler(False)
    handler_target = resultHandler(True)

    parser.setContentHandler(handler_target)
    parser.parse(targetResult)
    target_mapping = handler_target.mapping
    target_header = handler_target.header

    parser.setContentHandler(handler_base)
    parser.parse(baseResult)

    results = []
    for k in target_mapping:
        package = k.split("#")[0]
        case = k.split("#")[-1]
        target_value = target_mapping.get(k)
        target_result = target_value[0]#fail or timeout
        base_value = handler_base.mapping.get(k)
        compareInfo = ""
        if base_value:
            base_result = base_value[0]
            if base_result == "pass":
                compareInfo = "pass on base version"
            elif base_result == "notExecuted":
                compareInfo = "notExecuted on base version"
            elif base_result == "timeout":
                compareInfo = "timeout on base version"
            elif base_value == target_value:
                compareInfo = "same issue of base version"
            else:
                compareInfo = "different error on base version: " + base_value[-1]
        else:
            compareInfo = "not found on base version"
        if target_result == "fail":
            result = [compareInfo, package, case, target_value[-1]]
        else:
            result = [compareInfo, package, case, target_value[0]]
        results.append(result)

        if len(args) == 2:
            print result
    results.sort() #sort results by compare info

    if len(args) >= 3:
        csvWriter = csv.writer(file(sys.argv[3], 'w'))
	csvWriter.writerow(['Test Package', 'Test Cases', 'compare to base version\n' + sys.argv[1], 'CTS Error' + '(' + sys.argv[2] + ')'])
        for i in results:
            result = [i[1], i[2], i[0], i[3].encode("utf-8")] # need to convert err msg to utf-8 otherwise it may report exception
            csvWriter.writerow(result)

    headers = []
    if len(args) >= 5 and sys.argv[5] == "1":
        csvWriter.writerow("") 
        csvWriter.writerow("") 
        for i in target_header:
            headers.append([i, target_header[i]])
        headers.sort()
        for i in headers:
            csvWriter.writerow(i) 
