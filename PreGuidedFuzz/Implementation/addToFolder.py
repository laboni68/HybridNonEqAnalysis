import os 
import xlsxwriter
import sys
name=sys.argv[1]
# fileName=sys.argv[2]
# number=sys.argv[3]

folderName="/media/laboni/HDD11/PASDA/"+name+"/"

folders=os.listdir(folderName)

def addToFolder():
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
                # print(subsubsubfolderName+"boundary/")
                os.makedirs(subsubsubfolderName+"boundaryNoSeed/", exist_ok=True)
                os.makedirs(subsubsubfolderName+"uniformNoSeed/", exist_ok=True)
                for file in subsubsubfolders:
                    if file.startswith("ResultBFuzz"):
                        command="mv "+subsubsubfolderName+file+" "+subsubsubfolderName+"boundaryNoSeed/"
                        os.system(command)
                        print(command)
                    elif file.startswith("ResultFuzz"):
                        command="mv "+subsubsubfolderName+file+" "+subsubsubfolderName+"uniformNoSeed/"
                        os.system(command)
                        print(command)

def copyToMainFolder():
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
                # print(subsubsubfolderName+"boundary/")
                # os.makedirs(subsubsubfolderName+"boundarySeed/", exist_ok=True)
                # os.makedirs(subsubsubfolderName+"uniformSeed/", exist_ok=True)
                command="cp "+subsubsubfolderName+"boundaryNoSeed/* "+subsubsubfolderName
                os.system(command)
                print(command)
                command="cp "+subsubsubfolderName+"uniformNoSeed/* "+subsubsubfolderName
                os.system(command)
                print(command)

# copyToMainFolder()
addToFolder()