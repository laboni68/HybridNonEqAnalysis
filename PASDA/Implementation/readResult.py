import os 
import xlsxwriter


folderName="/media/laboni/HDD1/PASDA/benchmarks/"
execlfile="/media/laboni/HDD1/PASDA/benchmarks/Error.xlsx"
workbook = xlsxwriter.Workbook(execlfile)
worksheet = workbook.add_worksheet()

worksheet.write("A1","Folder Name")
worksheet.write("B1","File Name")
worksheet.write("C1","Type")

folders=os.listdir(folderName)
countError=0
countNoPath=0
countMismatch=0

row=2
for folder in folders:
    subfolderName=folderName+folder+"/"
    subfolders=os.listdir(subfolderName)
    for subfolder in subfolders:
        subsubfolderName=subfolderName+subfolder+"/"
        subsubfolders=os.listdir(subsubfolderName)
        for subsubfolder in subsubfolders:
            intrumentedFolderName=subsubfolderName+subsubfolder+"/instrumented/"
            instrumentedFiles=os.listdir(intrumentedFolderName)
            for instrumentedFile in instrumentedFiles:
                if instrumentedFile.startswith("Result"):
                    readContent=open(intrumentedFolderName+instrumentedFile,"r")
                    # lines=readContent.readlines()
                    allContent=readContent.read()
                    if allContent.find("No path conditions")!=-1:
                        countNoPath+=1
                        print(subsubfolderName+subsubfolder)
                        worksheet.write("A"+str(row),subsubfolderName)
                        worksheet.write("B"+str(row),subsubfolder)
                        worksheet.write("C"+str(row),"No path conditions")
                        row+=1
                        break
                    elif allContent.find("Method Summaries123")==-1:
                        countError+=1
                        print(subsubfolderName+subsubfolder)
                        worksheet.write("A"+str(row),subsubfolderName)
                        worksheet.write("B"+str(row),subsubfolder)
                        worksheet.write("C"+str(row),"Time out")
                        row+=1
                        break
                    elif allContent.find("DifferentOutputsException")==-1:
                        # print(subsubfolderName+subsubfolder+" Eq")
                        # print(subsubfolderName.split("/")[-2])
                        if subsubfolder=="NEq" or subsubfolderName.split("/")[-2]=="NEq":
                            print(subsubfolderName+subsubfolder+" Eq")
                            # worksheet.write("A"+str(row),subsubfolderName+subsubfolder)
                            # worksheet.write("B"+str(row),"Eq")
                            # worksheet.write("C"+str(row),"1")
                            # print("Mismatch")
                            countMismatch+=1
                            #row+=1
                    else:
                        # print(subsubfolderName+subsubfolder+" NEq")
                        if subsubfolder=="Eq" or subsubfolderName.split("/")[-2]=="Eq":
                            print(subsubfolderName+subsubfolder+" NEq")
                            # worksheet.write("A"+str(row),subsubfolderName+subsubfolder)
                            # worksheet.write("B"+str(row),"NEq")
                            # worksheet.write("C"+str(row),"2")
                            # print("Mismatch 2")
                            countMismatch+=1
                            #row+=1
                    # lines=allContent.split("\n")
                    # for line in lines:
                    #    if line.startswith("elapsed time: "):
                    #         print(subsubfolderName+subsubfolder+" "+line)
  
print("==============Result Summary==============")                 
print("Error ",countError)
print("No Path ",countNoPath)
print("Mismatch ",countMismatch)
print("Correct ", 141-countError-countMismatch-countNoPath)
workbook.close()

