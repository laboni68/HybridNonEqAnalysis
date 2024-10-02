import os 
import xlsxwriter
import sys
name=sys.argv[1]
fileName=sys.argv[2]
writeExcel=sys.argv[3]
folderName="/media/laboni/HDD11/PASDA/"+name+"/"
if writeExcel=="1":
    execlfile="/media/laboni/HDD11/PASDA/"+name+"/"+fileName+"_F.xlsx"
    workbook = xlsxwriter.Workbook(execlfile)
    worksheet = workbook.add_worksheet()

    worksheet.write("A1","Folder Name")
    # worksheet.write("B1","File Name")
    worksheet.write("B1","Result")
    worksheet.write("C1","Time")
    worksheet.write("D1","Possible Reason")

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

differetPrintResult=["BFuzz7000", "BFuzz8000", "BFuzz9000", "BFuzz10000", "Fuzz7000", "Fuzz8000", "Fuzz9000", "Fuzz10000","BFuzz500"]

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
            time=""
            result=""
            for file in subsubsubfolders:
                # if file=="ResultHybrid.txt":
                # print(file)
                if file=="Result"+fileName+".txt":
                    print("----------------------------------------------")
                    f=open(subsubsubfolderName+file,"r")
                    subsubsubfolderName='/'.join(subsubsubfolderName.split('/')[6:])
                    lines=f.readlines()
                    print(lines[-1])
                    for line in lines:
                        if "Execution time in" in line:
                            time=line.split(" ")[4]
                        if "->" in line:
                            result=line.split("->")[-1].strip()
                        if "TIMEOUT" in line:
                            time="115000"
                    print(result,time)
                    if result=="" and time=="":
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"Error")
                            worksheet.write("C"+str(row),"115000")
                            worksheet.write("D"+str(row),"")
                            row+=1
                        print(subsubsubfolderName,"Error (loop stuck)")
                        countError+=1
                        continue
                    if result=="EQ":
                        countEq+=1
                        timeEq+=int(time)
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"Eq")
                            worksheet.write("C"+str(row),time)
                            worksheet.write("D"+str(row),"")
                            
                    elif result=="NEQ":
                        countNEq+=1
                        timeNEq+=int(time)
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"NEq")
                            worksheet.write("C"+str(row),time)
                            worksheet.write("D"+str(row),"")
                    else:
                        countUnknown+=1
                        timeUnknown+=int(time)
                        if writeExcel=="1":
                            worksheet.write("A"+str(row),subsubsubfolderName)
                            worksheet.write("B"+str(row),"UNKNOWN")
                            worksheet.write("C"+str(row),time)
                            worksheet.write("D"+str(row),"")
                    row+=1
                    
                        
                        
# print(count)
print("Eq ",countEq)
print("NEq ",countNEq)
print("Unknown ",countUnknown)
# print("Maybe Eq ",countMaybeEq)
# print("Maybe NEq ",countMaybeNEq)
# print("Mismatch ",countMismatch)
# print("Error ",countError)
print("Time Eq ",timeEq)
print("Time NEq ",timeNEq)
print("Time Unknown ",timeUnknown)
print("Time Maybe Eq ",timeMaybeEq)
print("Time Maybe NEq ",timeMaybeNEq)
# print("Time Error ",timeError)
print("--------------------")
if countEq!=0:
    print("Time Eq Average (s) ",timeEq/(countEq*1000))
if countNEq!=0:
    print("Time NEq Average (s) ",timeNEq/(countNEq*1000))
if countUnknown!=0:
    print("Time Unknown Average (s) ",timeUnknown/(1000*countUnknown))
# if countMaybeEq!=0:
#     print("Time Maybe Eq Average ",timeMaybeEq/countMaybeEq)
# if countMaybeNEq!=0:
#     print("Time Maybe NEq Average ",timeMaybeNEq/countMaybeNEq)
# if countError!=0:
#     print("Time Error Average ",timeError/countError)
print("Total ",countEq+countNEq+countUnknown+countMaybeEq+countMaybeNEq+countError)
print("Total Time ",timeEq+timeNEq+timeUnknown+timeMaybeEq+timeMaybeNEq+timeError)
print("Total Time Average ",(timeEq+timeNEq+timeUnknown+timeMaybeEq+timeMaybeNEq+timeError)/(countEq+countNEq+countUnknown+countMaybeEq+countMaybeNEq+countError)," ms")
print("Total Time Average ",(timeEq+timeNEq+timeUnknown+timeMaybeEq+timeMaybeNEq+timeError)/(countEq+countNEq+countUnknown+countMaybeEq+countMaybeNEq+countError)/1000," s")
if writeExcel=="1":
    workbook.close()
