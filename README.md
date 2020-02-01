# mhtoolsgui
This is a fork (sorta, I was too lazy to actually reupload and delete files for a legit fork in guthub so I just uploaded to a new repo) of the original codestation mhtools https://github.com/codestation/mhtools. This version has a gui and multi file selection for easier use and less terminal command spamming.

Things to note.
- The KIRK related functions requires the Bouncy Castle Crypto API
that can be downloaded from http://www.bouncycastle.org/download/bcprov-jdk16-146.jar
- common-lang 3.9 is required. https://mvnrepository.com/artifact/org.apache.commons/commons-lang3/3.9
- The code is pretty trash to be honest, but for out purpose it was a quick solution that helped a lot regardless.

Here is a copy paste of the use info from codestation's repo. One thing to note is that these are for the command line use of the 
program. The gui version can be used either way, just note that the parameters have changed slightly and can be seen in the gui 
version. Folder and multi-file selection is used depending on which command is selected. 

ex: --create-patch <folder path> will recursively go through all the encrypted bins to add them to the patch. Prior to my change, 
you had to manually type or paste every directory for the patch. Another thing to note, when selecting files its best to either shift+click or ctrl+click.


encoder/decoder number meaning to use in binary files:

(Newer data.bin)
1 -> 0017, 2813-2818
3 -> 4202-4204
4 -> 3973-3987
7 -> mib files between 2819-3972

(Older data.bin)
1 -> 0016, 4757-4759
2 -> 5311-5323
3 -> 5370-5373

7 -> mib quests

To unpack and decrypt all the files from a container, e.g.:
java -jar mhtools.jar --dec-all /home/user/data.bin output_dir

To extract all the string tables from a binary file, e.g.:
java -jar mhtools.jar --extract 0017.bin 1

To rebuild a new binary file using the string tables, e.g.:
java -jar mhtools.jar --rebuild /home/user/0017 1

To encrypt a binary file, e.g.:
java -jar mhtools.jar --encrypt 0017.bin

To rebuild and encrypt, e.g.:
java -jar mhtools.jar --reb-enc /home/user/0017 1

To decrypt a binary file, e.g.:
java -jar mhtools.jar --decrypt 0017.bin.enc

To decrypt and extract, e.g.:
java -jar mhtools.jar --dec-ext 0017.bin.enc 1

To only generate a index.bin (necessary to decrypt/encrypt), e.g.:
java -jar mhtools.jar --gen-index /home/user/data.bin

To create a patchfile, e.g.:
java -jar mhtools.jar --create-patch 0017.bin.enc 2813.bin.enc 2814.bin.enc MHP3RD_DATA.BIN
(Note, it can use an optional data_install.txt to create tables that can be used in
patchers who can patch the data install)

To extract images from a TMH container
java -jar mhtools.jar --extract container.tmh 5

To pack images into a TMH container
java -jar mhtools.jar --rebuild /home/user/unpacked_countainer_dir 5

To unpack a .pak file
java -jar mhtools.jar --extract file.pak 6

Thanks to codestation for all of his work creating the original tool. If he has an issue with this repo then feel free to contact me.
