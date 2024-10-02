import os 
import xlsxwriter


folderName="/media/laboni/HDD1/PASDA/benchmarks/"
# execlfile="/media/laboni/HDD1/PASDA/benchmarks/Error.xlsx"
# workbook = xlsxwriter.Workbook(execlfile)
# worksheet = workbook.add_worksheet()

# worksheet.write("A1","Folder Name")
# worksheet.write("B1","File Name")
# worksheet.write("C1","Type")

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
            intrumentedFolderName=subsubfolderName+subsubfolder+"/"
            instrumentedFiles=os.listdir(intrumentedFolderName)
            for instrumentedFile in instrumentedFiles:
                if instrumentedFile=="ResultSE_abs.txt":
                    command="rm "+intrumentedFolderName+instrumentedFile
                    print(command)
                    os.system(command)
            #     command="rm "+intrumentedFolderName+instrumentedFile
            #     print(command)
            #     os.system(command)
