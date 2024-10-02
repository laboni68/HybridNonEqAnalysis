import os 

copyFrom="../EqBenchPrev/"
copyTo="../EqBench/"

folders=os.listdir(copyFrom)
foldersCopyTo=os.listdir(copyTo)
count=0
for folder in folders:
    folderName=copyFrom+folder+"/"
    if folder.endswith(".xlsx"):
        continue
    subfolders=os.listdir(folderName)
    for subfolder in subfolders:
        subfolderName=folderName+subfolder+"/"
        subsubfolders=os.listdir(subfolderName)
        for subsubfolder in subsubfolders:
            subsubfolderName=subfolderName+subsubfolder+"/"
            subsubsubfolders=os.listdir(subsubfolderName)
            if os.path.exists(copyTo+folder+"/"+subfolder+"/"+subsubfolder):
                count+=1
                # print(subsubfolderName)
                command="cp -r "+copyFrom+folder+"/"+subfolder+"/"+subsubfolder+"/* "+copyTo+folder+"/"+subfolder+"/"+subsubfolder+"/"
                print(command)
                os.system(command)
            # else:
            #     print("Not found")
            #     print(subsubfolderName)
print(count)