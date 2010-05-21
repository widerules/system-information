#!/usr/bin/python
#Filename: parsercts.py
#Jingtao: 2010-05-20

import xml.sax
import csv
import sys

global_mapping = {}

class resultHandler(xml.sax.handler.ContentHandler):
  global global_mapping

  def __init__(self, isTarget):
     self.packageName = ""
     self.caseName = ""
     self.errMsg = ""
     self.testResult = ""

     self.isTarget = isTarget
     self.mapping = {}

  def startElement(self, name, attrs):
     if name == "TestSuite" or name == "TestCase":
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
        if self.packageName + "#" + self.caseName in global_mapping:#record all the result if is base
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
    print "input 3 path parameters:1.the path of CTS testResult.xml file as base. 2.the path of CTS testResult.xml file to compare with the base one."
    print "                        3.the third paremeter is optional:The path of compare result file"
    print "2 parameters example:python " + cmd + " dragon-r10000/testResult.xml" + " dragon-r12000/testResult.xml"
    print "3 parameters example:python " + cmd + " nexus-one/testResult.xml" + " dragon-r12000/testResult.xml" + " compare.csv"


args = sys.argv[1:]
if (len(args) < 2):
    print_useage() 
else:
    parser = xml.sax.make_parser()
    handler_base = resultHandler(False)
    handler_target = resultHandler(True)

    parser.setContentHandler(handler_target)
    parser.parse(sys.argv[2])
    global_mapping = handler_target.mapping

    parser.setContentHandler(handler_base)
    parser.parse(sys.argv[1])

    results = []
    for k in global_mapping:
        package = k.split("#")[0]
        case = k.split("#")[-1]
        target_value = global_mapping.get(k)
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

    if len(args) == 3:
        csvWriter = csv.writer(file(sys.argv[3], 'w'))
	csvWriter.writerow(['Test Package', 'Test Cases', 'compare to base version\n' + sys.argv[1], 'CTS Error' + '(' + sys.argv[2] + ')'])
        for i in results:
            result = [i[1], i[2], i[0], i[3].encode("utf-8")] # need to convert err msg to utf-8 otherwise it may report exception
            csvWriter.writerow(result)
