import os 
import xlsxwriter
import sys
name=sys.argv[1]
fileName=sys.argv[2]
# number=sys.argv[3]

folderName="/media/laboni/HDD21/PASDAOriginal/"+name+"/"
excelFileName=folderName+fileName+".xlsx"

workbook = xlsxwriter.Workbook(excelFileName)
worksheet = workbook.add_worksheet()
worksheet.write("A1", "File Name")
worksheet.write("B1", "Result")
worksheet.write("C1", "Time (ms)")
worksheet.write("D1", "Total Equivalent")
worksheet.write("E1", "Total Not Equivalent")
worksheet.write("F1", "Total maybe Equivalent")
worksheet.write("G1", "Total maybe Not Equivalent")
worksheet.write("H1", "Total Unknown")
worksheet.write("I1", "Total Depth Limited")
worksheet.write("J1", "Total Time (ms)")
worksheet.write("K1", "Avg Time (s)")
worksheet.write("L1", "Base Time (ms)")
worksheet.write("M1", "Avg Eq time (s)")
worksheet.write("N1", "Avg Neq time (s)")
worksheet.write("O1", "Avg Maybe Eq time (s)")
worksheet.write("P1", "Avg Maybe Neq time (s)")
worksheet.write("Q1", "Avg Unknown time (s)")
worksheet.write("R1", "Avg Depth Limited time (s)")
worksheet.write("S1", "Avg Total unknown Time (s)")
totalEq=0
totalNeq=0
totalUnknown=0
totalMaybeEq=0
totalMaybeNeq=0
totalDepthLimited=0
totalTime=0
row = 2
end=143
mismatchCount=0
falsePositive=0
totalEqTime=0
totalNeqTime=0
totalMaybeEqTime=0
totalMaybeNeqTime=0
totalUnknownTime=0
totalDepthLimitedTime=0
folders=os.listdir(folderName)
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
                if file==fileName+".txt":
                    lines=open(subsubsubfolderName+file).readlines()
                    pasdaBaseLines=open(subsubsubfolderName+fileName+"base.txt").readlines()
                    # baseName=("/").join(fileName.split("/")[:-1])
                    # pasdaBaseLines=open(subsubsubfolderName+baseName+"ResultPASDAbase120.txt").readlines()
                    print(subsubsubfolderName)
                    baseTime=pasdaBaseLines[-1].split(":")[1].replace("ms","").strip()
                    # print(lines[-1])
                    # print(lines[-3])
                    result=""
                    time=""
                    # for line in lines:
                    #     if line.startswith("Output :"):
                    #     #    print(line)
                    #        result=line.split(":")[1].strip()
                    #     if line.startswith("Execution time"):
                    #         time=line.split(":")[1].replace("ms","").strip()
                    # print(subsubsubfolderName)
                    # print(lines[-2])
                    # print(lines[-1])
                    # print("=====================================")
                    for line in lines:
                        if line.startswith("Iteration "):
                            result=line.split("->")[1].strip()
                        if line.startswith("Execution time"):
                            time=line.split(":")[1].replace("ms","").strip()
                        elif line.startswith("TIMEOUT:"):
                            if "120" in fileName:
                                time=120000
                            else:
                                time=300000
                            result=line.split("->")[1].strip()
                    # print(subsubsubfolderName)
                    # print(result)
                    # print(time)
                    # print("=====================================")
                    worksheet.write("A"+str(row), subsubsubfolderName.split(name+"/")[-1])
                    worksheet.write("B"+str(row), result)
                    worksheet.write("C"+str(row), time)
                    worksheet.write("L"+str(row), baseTime)
                    if result=="" and time=="":
                        if "120" in fileName:
                            worksheet.write("C"+str(row), "120000")
                            totalTime+=120000
                        else:
                            worksheet.write("C"+str(row), "300000")
                            totalTime+=300000
                    else:
                        # print(subsubsubfolderName, time, baseTime)
                        totalTime+=int(time)+int(baseTime)
                    if result=="EQ":
                        totalEq+=1
                        totalEqTime+=int(time)
                    elif result=="NEQ":
                        totalNeq+=1
                        totalNeqTime+=int(time)
                    elif result=="MAYBE_EQ":
                        totalMaybeEq+=1
                        totalMaybeEqTime+=int(time)
                    elif result=="MAYBE_NEQ":
                        totalMaybeNeq+=1
                        totalMaybeNeqTime+=int(time)
                    elif result=="DEPTH_LIMITED":
                        totalDepthLimited+=1
                        totalDepthLimitedTime+=int(time)
                    elif result=="UNKNOWN" or result=="":
                        totalUnknown+=1
                        totalUnknownTime+=int(time)
                    if subsubsubfolderName.split("/")[-2]=="NEq" and result=="EQ":
                        mismatchCount+=1
                        print(subsubsubfolderName)
                    if subsubsubfolderName.split("/")[-2]=="Eq" and result=="NEQ":
                        mismatchCount+=1
                        print(subsubsubfolderName)
                    if "/Eq" in subsubsubfolderName and result=="MAYBE_NEQ":
                        falsePositive+=1
                        print(subsubsubfolderName, result)
                    if "NEq" in subsubsubfolderName and result=="MAYBE_EQ":
                        falsePositive+=1
                        print(subsubsubfolderName, result)
                    
                    row+=1
                    # print("=====================================")


# worksheet.write("D"+str(row), totalEq)
# worksheet.write("E"+str(row), totalNeq)
# worksheet.write("F"+str(row), totalUnknown)
# worksheet.write("G"+str(row), totalTime)
worksheet.write("D2", totalEq)
worksheet.write("E2", totalNeq)
worksheet.write("F2", totalMaybeEq)
worksheet.write("G2", totalMaybeNeq)
worksheet.write("H2", totalUnknown)
worksheet.write("I2", totalDepthLimited)
worksheet.write("J2", totalTime)
worksheet.write("K2", totalTime/((row-2)*1000))
worksheet.write("M2", totalEqTime/(totalEq*1000))
worksheet.write("N2", totalNeqTime/(totalNeq*1000))
worksheet.write("O2", totalMaybeEqTime/(totalMaybeEq*1000))
worksheet.write("P2", totalMaybeNeqTime/(totalMaybeNeq*1000))
worksheet.write("Q2", totalUnknownTime/(totalUnknown*1000))
if totalDepthLimited==0:
    worksheet.write("R2", 0)
else:
    worksheet.write("R2", totalDepthLimitedTime/(totalDepthLimited*1000))
worksheet.write("S2", (totalUnknownTime+totalDepthLimitedTime)/((totalUnknown+totalDepthLimited)*1000))

workbook.close()
print("Mismatch Count: "+str(mismatchCount))
print("Total Eq: "+str(totalEq))
print("Total NEq: "+str(totalNeq))
print("Total Maybe Eq: "+str(totalMaybeEq))
print("Total Maybe NEq: "+str(totalMaybeNeq))
print("Total Unknown: "+str(totalUnknown))
print("Total Depth Limited: "+str(totalDepthLimited))
print("Total False Positive: "+str(falsePositive))
print("Total: "+str(totalEq+totalNeq+totalMaybeEq+totalMaybeNeq+totalUnknown+totalDepthLimited))
print("Total Time: "+str(totalTime))
print("Avg Time: "+str(totalTime/((row-2)*1000)))
if totalEq!=0:
    print("Avg Eq Time: "+str(totalEqTime/(totalEq*1000)))
if totalNeq!=0:
    print("Avg Neq Time: "+str(totalNeqTime/(totalNeq*1000)))
if totalMaybeEq!=0:
    print("Avg Maybe Eq Time: "+str(totalMaybeEqTime/(totalMaybeEq*1000)))
if totalMaybeNeq!=0:
    print("Avg Maybe Neq Time: "+str(totalMaybeNeqTime/(totalMaybeNeq*1000)))
if totalUnknown!=0:
    print("Avg Unknown Time: "+str(totalUnknownTime/(totalUnknown*1000)))
if totalDepthLimited!=0:
    print("Avg Depth Limited Time: "+str(totalDepthLimitedTime/(totalDepthLimited*1000)))
if totalUnknown+totalDepthLimited!=0:
    print("Avg Total Unknown Time: "+str((totalUnknownTime+totalDepthLimitedTime)/((totalUnknown+totalDepthLimited)*1000)))