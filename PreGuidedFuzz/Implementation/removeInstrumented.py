import os 
import xlsxwriter


folderName="../EqBench/"

folders=os.listdir(folderName)
countError=0
countNoPath=0
countMismatch=0

row=2
for folder in folders:
    subfolderName=folderName+folder+"/"
    if subfolderName.endswith(".xlsx/"):
        continue
    subfolders=os.listdir(subfolderName)
    for subfolder in subfolders:
        subsubfolderName=subfolderName+subfolder+"/"
        subsubfolders=os.listdir(subsubfolderName)
        for subsubfolder in subsubfolders:
            intrumentedFolderName=subsubfolderName+subsubfolder+"/instrumented/"
            command="rm -r "+intrumentedFolderName
            print(command)
            os.system(command)
            # instrumentedFiles=os.listdir(intrumentedFolderName)
            # for instrumentedFile in instrumentedFiles:
            #     if instrumentedFile.endswith(".json"):
            #         command="rm "+intrumentedFolderName+instrumentedFile
            #         print(command)
            #         os.system(command)
                # command="rm "+intrumentedFolderName+instrumentedFile
                # print(command)
                # os.system(command)
