#!/usr/bin/python
#Filename: parsercts.py
#Jingtao: 2010-07-23

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
    print "usage: " + cmd + " <base> <target> [output file] [revert base and target]"
    print "example: "
    print "       " + cmd + " nexus-one/testResult.xml" + " dragon-r12000/testResult.xml" " //(it will output compare result to nexus-one_dragon-r12000.csv)"
    print "       " + cmd + " nexus-one/testResult.xml" + " dragon-r12000/testResult.xml" + " compare.csv 1 (1 means revert base and target)"


args = sys.argv[1:]
if (len(args) < 2):
    print_useage() 
else:
    baseResult = sys.argv[1]
    targetResult = sys.argv[2]
    if (len(args) >= 4) and (sys.argv[4] == "1"): #revert base and target
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
    base_header = handler_base.header

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
                compareInfo = "pass on base report"
            elif base_result == "notExecuted":
                compareInfo = "notExecuted on base report"
            elif base_result == "timeout":
                compareInfo = "timeout on base report"
            elif base_value == target_value:
                compareInfo = "same issue of base report"
            else:
                compareInfo = "different error on base report: " + base_value[-1]
        else:
            compareInfo = "not found on base report"
        if target_result == "fail":
            result = [compareInfo, package, case, target_value[-1]]
        else:
            result = [compareInfo, package, case, target_value[0]]
        results.append(result)

    results.sort() #sort results by compare info

    baseName = sys.argv[1].split("/")[-2]
    targetName = sys.argv[2].split("/")[-2]
    outputFile = baseName + "_" + targetName + ".csv"
    if (len(args) >= 4) and (sys.argv[4] == "1"): #revert base and target
        outputFile = targetName + "_" + baseName + ".csv"
    if len(args) >= 3: #the name is specified by user
        outputFile = sys.argv[3]

    csvWriter = csv.writer(file(outputFile, 'w'))
    csvWriter.writerow(['Test Package', 'Test Cases', 'compare to base report\n' + baseResult, 'CTS Error' + '(' + targetResult + ')'])
    for i in results:
        result = [i[1], i[2], i[0], i[3].encode("utf-8")] # need to convert err msg to utf-8 otherwise it may report exception
        csvWriter.writerow(result)

    #write head info to result table
    headers = []
    csvWriter.writerow("") 
    csvWriter.writerow("") 
    csvWriter.writerow(["", "target report info", "base report info"]) 
    for i in target_header:
        headers.append([i, target_header[i], base_header[i]])
    headers.sort()
    for i in headers:
        csvWriter.writerow(i) 
