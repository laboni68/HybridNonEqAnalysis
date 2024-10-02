import os 
import xlsxwriter
import sys
name=sys.argv[1]
fileName=sys.argv[2]
writeExcel=sys.argv[3]
whichSubFolder=sys.argv[4]
folderName="/media/laboni/HDD11/PASDA/"+name+"/"
if writeExcel=="1":
    execlfile="/media/laboni/HDD11/PASDA/"+name+"/"+whichSubFolder+"/"+fileName+".xlsx"
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
countMismatch=0
ignoreNeq=["LARGEST_DIVISOR","IS_SIMPLE_POWER","IS_MULTIPLY_PRIME","ROUNDED_AVG"]
ignoreEq=["CHOOSE_NUM","IS_MULTIPLY_PRIME","ANY_INT"]

for folder in folders:
    if folder!=whichSubFolder:
        continue
    subfolderName=folderName+folder+"/"
    # if folder.endswith(".xlsx"):
    #     continue
    subfolders=os.listdir(subfolderName)
    for subfolder in subfolders:
        if whichSubFolder=="NEq" and subfolder in ignoreNeq:
            continue
        if whichSubFolder=="Eq" and subfolder in ignoreEq:
            continue
        if subfolder.endswith(".xlsx"):
            continue
        subsubfolderName=subfolderName+subfolder+"/"
        subsubfolders=os.listdir(subsubfolderName)
        for file in subsubfolders:
            Foldername='/'.join(subsubfolderName.split('/')[6:])
            result=""
            time=""
            if file=="Result"+fileName+".txt":
                f=open(subsubfolderName+file,"r")
                # subsubsubfolderName='/'.join(subsubsubfolderName.split('/')[6:])
                lines=f.readlines()
                # print(lines)
                time=lines[-1].split(" ")[-5]
                # print(subsubfolderName," ",time.strip())
                if lines[-1].startswith("TIMEOUT"):
                    time="97000"
                print(subsubfolderName," ",time.strip())
                if ("->") in lines[-2]:
                    print(lines[-2].split("->")[1])
                    result=lines[-2].split("->")[1].strip()
                elif ("->") in lines[-3]:
                    print(lines[-3].split("->")[1])
                    result=lines[-3].split("->")[1].strip()
                # print(result)
                if result=="EQ":
                    countEq+=1
                    timeEq+=int(time)
                elif result=="NEQ":
                    countNEq+=1
                    timeNEq+=int(time)
                elif result=="UNKNOWN":
                    countUnknown+=1
                    timeUnknown+=int(time)
                elif result=="MAYBE_EQ":
                    countMaybeEq+=1
                    timeMaybeEq+=int(time)
                elif result=="ERROR":
                    countError+=1
                    timeError+=int(time)
                elif result=="":
                    countError+=1
                    timeError+=int(time)
                elif result=="null":
                    countUnknown+=1
                    timeUnknown+=int(time)
                if result=="EQ" and "NEq" in subsubfolderName:
                    countMismatch+=1
                    print(subsubfolderName," ",result)
                if result=="NEq" and "/Eq/" in subsubfolderName:
                    countMismatch+=1
                    print(subsubfolderName," ",result)
                if writeExcel=="1":
                    worksheet.write("A"+str(row),Foldername)
                    worksheet.write("B"+str(row),result)
                    worksheet.write("C"+str(row),time)
                row+=1
 
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
if countError!=0:
    print("Time Error Average ",timeError/countError)
print("Total ",countEq+countNEq+countUnknown+countMaybeEq+countMaybeNEq+countError)
print("Total Time ",timeEq+timeNEq+timeUnknown+timeMaybeEq+timeMaybeNEq+timeError)
print("Total Time Average ",(timeEq+timeNEq+timeUnknown+timeMaybeEq+timeMaybeNEq+timeError)/(countEq+countNEq+countUnknown+countMaybeEq+countMaybeNEq+countError)," ms")
print("Total Time Average ",(timeEq+timeNEq+timeUnknown+timeMaybeEq+timeMaybeNEq+timeError)/(countEq+countNEq+countUnknown+countMaybeEq+countMaybeNEq+countError)/1000," s")
if writeExcel=="1":
    workbook.close()
