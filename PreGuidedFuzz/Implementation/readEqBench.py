import os 

folderName="/media/laboni/HDD11/PASDA/EqBench/"
folders=os.listdir(folderName)

for folder in folders:
    subfolders=os.listdir(folderName+folder)
    for subfolder in subfolders:
        print("\""+folder+"/"+subfolder+"/Eq\"")
        print("\""+folder+"/"+subfolder+"/Neq\"")