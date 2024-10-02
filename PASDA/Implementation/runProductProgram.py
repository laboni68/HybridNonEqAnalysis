import os 

folderName="/media/laboni/HDD1/PASDA/benchmarks/"

folders=os.listdir(folderName)

# mismatchFolders=["Ran","Bess", "ModDiff", "tsafe","gam"]
# mismatchSubFolders=["gammln","bnldev", "bessy1", "LoopMult15","LoopMult20","LoopMult10","conflict","gser"]
# mismatchSubSubFolders=["Eq","NEq","Eq","NEq","NEq","NEq","Eq","Eq"]

for folder in folders:
    subfolderName=folderName+folder+"/"
    subfolders=os.listdir(subfolderName)
    for subfolder in subfolders:
        subsubfolderName=subfolderName+subfolder+"/"
        subsubfolders=os.listdir(subsubfolderName)
        for subsubfolder in subsubfolders:
            # if folder in mismatchFolders and subfolder in mismatchSubFolders:
            #     index=mismatchSubFolders.index(subfolder)
            #     if subsubfolder==mismatchSubSubFolders[index]:
                    intrumentedFolderName=subsubfolderName+subsubfolder+"/instrumented/"
                    instrumentedFiles=os.listdir(intrumentedFolderName)
                    for instrumentedFile in instrumentedFiles:
                        # if instrumentedFile.startswith("Result"):
                        #     print(instrumentedFile)
                        #     command="rm "+intrumentedFolderName+instrumentedFile
                        #     print(command)
                        #     os.system(command)
                        if instrumentedFile.endswith(".jpf"):
                            # print(instrumentedFile)
                            command="timeout 300 java -jar -Djava.library.path=./jpf-git/jpf-symbc/lib/ ./jpf-git/jpf-core/build/RunJPF.jar "
                            command=command+intrumentedFolderName+instrumentedFile+" > "+intrumentedFolderName+"Result"+instrumentedFile[:-4]+"Reduced.txt"
                            print(command)
                            # os.system(command)