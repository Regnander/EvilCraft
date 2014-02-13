#!/usr/bin/env python3
# encoding: utf-8
# Is able to parse input xml info files.

import getopt
import sys
import re

import xml.etree.ElementTree as etree
from collections import OrderedDict

def main():
    try:
        opts, args = getopt.getopt(sys.argv[1:], "f:", ["file="])
    except getopt.GetoptError as err:
        print(err)
        usage()
        sys.exit(2)
    file = None
    for o, a in opts:
        if o in ("-f", "--file"):
            file = a
            
    if(file == None):
        print("Please provide a valid input file.")
        sys.exit(2)
        
    info = parseInfo(file)
    print(generateBBCode(info))
    
# Parse the file
# TODO: keep original ordening!
def parseInfo(file):
    info = OrderedDict();
    
    tree = etree.parse(file)
    root = tree.getroot() 
    imgPrefix = root.attrib["imgPrefix"]
    imgSuffix = root.attrib["imgSuffix"]
    
    info = parseCategory(root, imgPrefix, imgSuffix)
            
    return info

# Recursively parse the xml
def parseCategory(category, imgPrefix, imgSuffix):
    catInfo = OrderedDict()
    if(len(category) == 0):
        text = category.text
        text = re.sub(r'\[img([^\]]*)\]([^\]]*)\[/img\]', r'[img\1]'+imgPrefix+r'\2'+imgSuffix+r'[/img]', text);
        catInfo = {"content" : text.strip()}
    else:
        for element in category:
            nameid = element.attrib["name"].replace(" ", "")
            if("img" in element.attrib):
                imgid = nameid
                if(element.attrib["img"] != "true"):
                    imgid = element.attrib["img"]
                element.text += "\n[img]" + imgid + "[/img]\n"
            if("recipe" in element.attrib):
                imgid = "recipes/" + nameid
                if(element.attrib["recipe"] != "true"):
                    imgid = element.attrib["recipe"]
                element.text += "\n[b]Recipe:[/b]\n[center][img]" + imgid + "[/img]\n\n[/center]\n"
            catInfo[element.attrib["name"]] = parseCategory(element, imgPrefix, imgSuffix)
    return catInfo
    
# Make BBCode from a given info dictionary
def generateBBCode(info):
    code = ""
    for (k, v) in info.items():
        if(isinstance(v, str)):
            code += v
        else:
            content = re.sub(' +', ' ', re.sub('\n ', '', re.sub('\t', '    ', generateBBCode(v))))
            if(len(v) == 1):
                code += "[u][b]" + k + "[/b][/u]" + "\n" + content + "\n"
            else:
                code += r"[spoiler='" + k + r"']"  + content + r"[/spoiler]" + "\n"
    return code

if __name__ == "__main__":
    main()