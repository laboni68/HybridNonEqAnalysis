import os 
import xlsxwriter
import sys
name=sys.argv[1]
fileName=sys.argv[2]
writeExcel=sys.argv[3]
folderName="/media/laboni/HDD11/PASDA/"+name+"/"
if writeExcel=="1":
    execlfile="/media/laboni/HDD11/PASDA/"+name+"/BoundaryArDiff.xlsx"
    workbook = xlsxwriter.Workbook(execlfile)
    worksheet = workbook.add_worksheet()

    worksheet.write("A1","Folder Name")
    # worksheet.write("B1","File Name")
    worksheet.write("B1","Result 1")
    worksheet.write("C1","Result 2")
    worksheet.write("D1","Result 3")
    worksheet.write("E1","Result 4")
    worksheet.write("F1","Result 5")
    worksheet.write("G1","Time 1")
    worksheet.write("H1","Time 2")
    worksheet.write("I1","Time 3")
    worksheet.write("J1","Time 4")
    worksheet.write("K1","Time 5")
    

    row=2

folders=os.listdir(folderName)
countNoPath=0
countMismatch=0

row=2
count=0
countEq=0
countNEq=0
countUnknown=0
countMaybeEq=0
countMaybeNEq=0
countError=0
countMismatch=0
timeEq=0
timeNEq=0
timeUnknown=0
timeMaybeEq=0
timeMaybeNEq=0
timeError=0

for folder in folders:
    subfolderName=folderName+folder+"/"
    if folder.endswith(".xlsx"):
        continue
    subfolders=os.listdir(subfolderName)
    for subfolder in subfolders:
        subsubfolderName=subfolderName+subfolder+"/"
        subsubfolders=os.listdir(subsubfolderName)
        for subsubfolder in subsubfolders:
            subsubsubfolderName=subsubfolderName+subsubfolder+"/"
            subsubsubfolders=os.listdir(subsubsubfolderName)

            for file in subsubsubfolders:
                # if file=="ResultHybrid.txt":
                if file=="Result"+fileName+".txt":
                    f=open(subsubsubfolderName+file,"r")
                    subsubsubfolderName='/'.join(subsubsubfolderName.split('/')[6:])
                    lines=f.readlines()
                    # print(lines)
                    if len(lines)<2:
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"Error")
                            worksheet.write("C"+str(row),"Timeout")
                            worksheet.write("D"+str(row),"")
                            row+=1
                        print(subsubsubfolderName,"Error (loop stuck)")
                        countError+=1
                        continue
                    if lines[-3].find("->")==-1 and lines[-2].find("->")==-1 and lines[-1].find("TIMEOUT")==-1:
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"Error")
                            worksheet.write("C"+str(row),"Timeout")
                            worksheet.write("D"+str(row),"timeout")
                            row+=1
                        print(subsubsubfolderName," Error (timeout)")
                        countError+=1
                        continue
                    # print("-------------------")
                    # print(subsubsubfolderName)
                    # print(lines[-2])
                    count+=1
                    #print(lines[-2].split(" "))
                    if lines[-3].split(" ")[-1]=="EQ\n":
                        # print(lines[-2].split("/")[2],lines[-2].split("/")[3])
                        if (lines[-3].split("/")[2]=="ModDiff" and lines[-3].split("/")[3]!="Eq") or (lines[-3].split("/")[2]!="ModDiff" and lines[-3].split("/")[4]!="Eq"):
                        #if lines[-2].split("/")[2]=="ModDiff" and lines[-2].split("/")[3]!="Eq":
                            if writeExcel=="1":
                                worksheet.write("A"+str(row),subsubsubfolderName)
                                worksheet.write("B"+str(row),"Eq")
                                worksheet.write("C"+str(row),lines[-3].strip())
                                worksheet.write("D"+str(row),"Mismatch: Neq but says Eq")
                                row+=1
                            print(subsubsubfolderName)
                            print(lines[-3].strip())
                            print("Mismatch: Neq but says Eq")
                            countMismatch+=1
                        else:
                            if writeExcel=="1":
                                worksheet.write("A"+str(row),subsubsubfolderName)
                                worksheet.write("B"+str(row),"Eq")
                                worksheet.write("C"+str(row),lines[-1].split(" ")[4])
                                worksheet.write("D"+str(row),"")
                                row+=1
                        countEq+=1
                        #print("Time Eq ",int(lines[-1].split(" ")[4])/1000," s")
                        timeEq+=int(lines[-1].split(" ")[4])/1000
                    elif lines[-3].split(" ")[-1]=="NEQ\n":
                        # print(lines[-2].split("/")[4])
                        if (lines[-3].split("/")[2]=="ModDiff" and lines[-3].split("/")[3]!="NEq") or (lines[-3].split("/")[2]!="ModDiff" and lines[-3].split("/")[4]!="NEq"):
                            # if lines[-2].split("/")[2]=="ModDiff" and lines[-2].split("/")[3]!="NEq":
                            if writeExcel=="1":
                                worksheet.write("A"+str(row),subsubsubfolderName)
                                worksheet.write("B"+str(row),"NEq")
                                worksheet.write("C"+str(row),lines[-1].split(" ")[4])
                                worksheet.write("D"+str(row),"Mismatch: eq but says Neq")
                                row+=1
                            print(subsubsubfolderName)
                            print(lines[-3].strip())
                            print("Mismatch: eq but says Neq")
                            
                            countMismatch+=1
                        else:
                            if writeExcel=="1":
                                worksheet.write("A"+str(row),subsubsubfolderName)
                                worksheet.write("B"+str(row),"NEq")
                                worksheet.write("C"+str(row),lines[-1].split(" ")[4])
                                worksheet.write("D"+str(row),"")
                                row+=1
                        countNEq+=1
                        #print("Time Neq ",int(lines[-1].split(" ")[4])/1000," s")
                        timeNEq+=int(lines[-1].split(" ")[4])/1000
                    elif lines[-3].split(" ")[-1]=="UNKNOWN\n":
                        if (lines[-3].split("/")[2]=="ModDiff" and lines[-3].split("/")[3]=="NEq") or (lines[-3].split("/")[2]!="ModDiff" and lines[-3].split("/")[4]=="NEq"):
                            print(subsubsubfolderName)
                            print(lines[-3].strip())
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"Unknown")
                            worksheet.write("C"+str(row),lines[-1].split(" ")[4])
                            worksheet.write("D"+str(row),"")
                            row+=1
                        countUnknown+=1
                        #print("Time unknown ",int(lines[-1].split(" ")[4])/1000," s")
                        timeUnknown+=int(lines[-1].split(" ")[4])/1000
                    elif lines[-3].split(" ")[-1]=="MAYBE_EQ\n":
                        if (lines[-3].split("/")[2]=="ModDiff" and lines[-3].split("/")[3]=="NEq") or (lines[-3].split("/")[2]!="ModDiff" and lines[-3].split("/")[4]=="NEq"):
                            print("-------------------")
                            print(subsubsubfolderName)
                            print(lines[-3].strip())
                            print("-------------------")
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"Maybe Eq")
                            worksheet.write("C"+str(row),lines[-1].split(" ")[4])
                            worksheet.write("D"+str(row),"")
                            row+=1
                        countMaybeEq+=1
                        print("++++++++++++++++++")
                        print(subsubsubfolderName)
                        print(lines[-3].strip())
                        print("++++++++++++++++++")
                        #print("Time maybe_eq ",int(lines[-1].split(" ")[4])/1000," s")
                        timeMaybeEq+=int(lines[-1].split(" ")[4])/1000
                    elif lines[-3].split(" ")[-1]=="MAYBE_NEQ\n":
                        # if (lines[-2].split("/")[2]=="ModDiff" and lines[-2].split("/")[3]=="NEq") or (lines[-2].split("/")[2]!="ModDiff" and lines[-2].split("/")[4]=="NEq"):
                        #     print(subsubsubfolderName)
                        #     print(lines[-2])
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"Maybe NEq")
                            worksheet.write("C"+str(row),lines[-1].split(" ")[4])
                            worksheet.write("D"+str(row),"")
                            row+=1
                        countMaybeNEq+=1
                        #print("Time maybe_neq ",int(lines[-1].split(" ")[4])/1000," s")
                        timeMaybeNEq+=int(lines[-1].split(" ")[4])/1000
                    elif lines[-3].split(" ")[-1]=="ERROR\n":
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"Error")
                            worksheet.write("C"+str(row),lines[-1].split(" ")[4])
                            worksheet.write("D"+str(row),"")
                            row+=1
                        countError+=1
                        print(subsubsubfolderName)
                        print(lines[-3].strip())
                        #print("Time error ",int(lines[-1].split(" ")[4])/1000," s")
                        timeError+=int(lines[-1].split(" ")[4])/1000
                    elif lines[-2].split(" ")[-1]=="NEQ\n":
                        # print(lines[-2].split("/")[4])
                        if (lines[-2].split("/")[2]=="ModDiff" and lines[-2].split("/")[3]!="NEq") or (lines[-2].split("/")[2]!="ModDiff" and lines[-2].split("/")[4]!="NEq"):
                            # if lines[-2].split("/")[2]=="ModDiff" and lines[-2].split("/")[3]!="NEq":
                            if writeExcel=="1":
                                worksheet.write("A"+str(row),subsubsubfolderName)
                                worksheet.write("B"+str(row),"NEq")
                                worksheet.write("C"+str(row),lines[-1].split(" ")[4])
                                worksheet.write("D"+str(row),"Mismatch: eq but says Neq")
                                row+=1
                            print(subsubsubfolderName)
                            print(lines[-2].strip())
                            print("Mismatch: eq but says Neq")
                            
                            countMismatch+=1
                        else:
                            if writeExcel=="1":
                                worksheet.write("A"+str(row),subsubsubfolderName)
                                worksheet.write("B"+str(row),"NEq")
                                worksheet.write("C"+str(row),lines[-1].split(" ")[4])
                                worksheet.write("D"+str(row),"")
                                row+=1
                        countNEq+=1
                        #print("Time Neq ",int(lines[-1].split(" ")[4])/1000," s")
                        timeNEq+=int(lines[-1].split(" ")[4])/1000
                    elif lines[-1].find("TIMEOUT")!=-1 and lines[-1].split(" ")[-1]=="MAYBE_EQ\n":
                        if (lines[-1].split("/")[2]=="ModDiff" and lines[-1].split("/")[3]=="NEq") or (lines[-1].split("/")[2]!="ModDiff" and lines[-1].split("/")[4]=="NEq"):
                            print("-------------------")
                            print(subsubsubfolderName)
                            print(lines[-1].strip())
                            print("-------------------")
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"Maybe Eq")
                            worksheet.write("C"+str(row),"120")
                            worksheet.write("D"+str(row),"")
                            row+=1
                        print("-------------------")
                        print(subsubsubfolderName)
                        print(lines[-1].strip())
                        print("-------------------")
                        countMaybeEq+=1
                        timeMaybeEq+=120
                    elif lines[-1].find("TIMEOUT")!=-1 and lines[-1].split(" ")[-1]=="MAYBE_NEQ\n":
                            if (lines[-1].split("/")[2]=="ModDiff" and lines[-1].split("/")[3]=="NEq") or (lines[-1].split("/")[2]!="ModDiff" and lines[-1].split("/")[4]=="NEq"):
                                print("-------------------")
                                print(subsubsubfolderName)
                                print(lines[-1].strip())
                                print("-------------------")
                            if writeExcel=="1":
                                worksheet.write("A"+str(row),subsubsubfolderName)
                                worksheet.write("B"+str(row),"Maybe NEq")
                                worksheet.write("C"+str(row),"120")
                                worksheet.write("D"+str(row),"")
                                row+=1
                            print("-------------------")
                            print(subsubsubfolderName)
                            print(lines[-1].strip())
                            print("-------------------")
                            countMaybeNEq+=1
                            timeMaybeNEq+=120
                    elif lines[-1].find("TIMEOUT")!=-1 and lines[-1].split(" ")[-1]=="UNKNOWN\n":
                        # if (lines[-1].split("/")[2]=="ModDiff" and lines[-1].split("/")[3]=="NEq") or (lines[-1].split("/")[2]!="ModDiff" and lines[-1].split("/")[4]=="NEq"):
                        #         print("-------------------")
                        #         print(subsubsubfolderName)
                        #         print(lines[-1].strip())
                        #         print("-------------------")
                        if writeExcel=="1":
                                worksheet.write("A"+str(row),subsubsubfolderName)
                                worksheet.write("B"+str(row),"Unknown")
                                worksheet.write("C"+str(row),"120")
                                worksheet.write("D"+str(row),"")
                                row+=1
                        print("-------------------")
                        print(subsubsubfolderName)
                        print(lines[-1].strip())
                        print("-------------------")
                        countUnknown+=1
                        timeUnknown+=120
                    
                        
                        
# print(count)
print("Eq ",countEq)
print("NEq ",countNEq)
print("Unknown ",countUnknown)
print("Maybe Eq ",countMaybeEq)
print("Maybe NEq ",countMaybeNEq)
print("Mismatch ",countMismatch)
print("Error ",countError)
print("Time Eq ",timeEq)
print("Time NEq ",timeNEq)
print("Time Unknown ",timeUnknown)
print("Time Maybe Eq ",timeMaybeEq)
print("Time Maybe NEq ",timeMaybeNEq)
# print("Time Error ",timeError)
print("--------------------")
if countEq!=0:
    print("Time Eq Average ",timeEq/countEq)
if countNEq!=0:
    print("Time NEq Average ",timeNEq/countNEq)
if countUnknown!=0:
    print("Time Unknown Average ",timeUnknown/countUnknown)
if countMaybeEq!=0:
    print("Time Maybe Eq Average ",timeMaybeEq/countMaybeEq)
if countMaybeNEq!=0:
    print("Time Maybe NEq Average ",timeMaybeNEq/countMaybeNEq)
print("Total ",countEq+countNEq+countUnknown+countMaybeEq+countMaybeNEq+countError)
if writeExcel=="1":
    workbook.close()
