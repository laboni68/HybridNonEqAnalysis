

import os 
import xlsxwriter
import sys
name=sys.argv[1]
fileName=sys.argv[2]
# number=sys.argv[3]

folderName="../"+name+"/"

# folders=os.listdir(folderName)
# for folder in folders:
#     subfolderName=folderName+folder+"/"
#     if folder.endswith(".xlsx"):
#         continue
#     subfolders=os.listdir(subfolderName)
#     for subfolder in subfolders:
#         if subfolder.endswith(".xlsx"):
#             continue
#         subsubfolderName=subfolderName+subfolder+"/"
#         subsubfolders=os.listdir(subsubfolderName)
#         for subsubfolder in subsubfolders:
#             # print(subsubfolder)
#             if subsubfolder=="instrumented" or subsubfolder=="outputs" or subsubfolder=="z3models":
#                 command="rm -r "+subsubfolderName+subsubfolder
#                 print(command)
#                 os.system(command)
# import os 
# import xlsxwriter
# import sys
# name=sys.argv[1]
# fileName=sys.argv[2]
# number=sys.argv[3]


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
                # print(file)
                # if file=="ResultHybrid.txt":
                # if file=="Result"+fileName+".txt":
                if file=="instrumented" or file=="outputs" or file=="z3models":
                    # command="mv "+subsubsubfolderName+file+" "+subsubsubfolderName+"Result"+fileName+number+".txt"
                    command="rm -r "+subsubsubfolderName+file
                    os.system(command)
                    print(command)