/*  MHTools - MH Utilities
    Copyright (C) 2008-2011 Codestation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import crypt.Decrypter;
import crypt.Encrypter;
import crypt.KirkCypher;
import crypt.QuestCypher;
import crypt.SavedataCypher;
import dec.ExtractPluginA;
import dec.ExtractPluginB;
import dec.ExtractPluginC;
import dec.ExtractPluginD;
import dec.ExtractPluginE;
import enc.RebuildPluginA;
import enc.RebuildPluginB;
import enc.RebuildPluginC;
import enc.RebuildPluginD;
import enc.RebuildPluginE;

public class MHTools {

    public static void extract(String filename, String decoder) {
        // (00[1-2][0-9]|47[0-9][0-9])\\..* decoder A
        // 53[0-9][0-9]\\..* decoder B
        // 54[0-9][0-9]\\..* decoder C

        Decoder dec = null;
        int type = 0;
        try {
        	type = Integer.parseInt(decoder);
        } catch(NumberFormatException e) {
        	Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        }
        
        switch (type) {
        case 1:
            dec = new ExtractPluginA();
            break;
        case 2:
            dec = new ExtractPluginB(0);
            break;
        case 4:
            dec = new ExtractPluginB(1);
            break;
        case 7:
            dec = new ExtractPluginB(2);
            break;
        case 3:
            dec = new ExtractPluginC();
            break;
        case 5:
            dec = new ExtractPluginD();
            break;
        case 6:
            dec = new ExtractPluginE();
            break;
        default:
            Window.writeToConsole("Unknown decoder: " + decoder);
        }
        dec.extract(filename);
    }

    public static void rebuild(String filename, String encoder) {
        Encoder enc = null;
        int type = 0;
        try {
        	type = Integer.parseInt(encoder);
        } catch(NumberFormatException e) {
        	Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        }
        
        if(type < 5) {
            String str = checkFile(filename + "/filelist.txt");
            if (str == null) {
                Window.writeToConsole(filename + "/filelist.txt could not be found");
            }
        }
        switch (type) {
        case 1:
            enc = new RebuildPluginA();
            break;
        case 2:
            enc = new RebuildPluginB(0);
            break;
        case 4:
        case 7:
            enc = new RebuildPluginB(type);
            break;
        case 3:
            enc = new RebuildPluginC();
            break;
        case 5:
            enc = new RebuildPluginD();
            break;
        case 6:
            enc = new RebuildPluginE();
            break;
        default:
            Window.writeToConsole("Unknown encoder: " + encoder);
        }
        enc.compile(filename);
    }
    
    public static void createPatch(String[] args) {
        
    }

    public static void process(String[] args) {
        Window.writeToConsole("mhtools v2.0 - MHP2G/MHFU/MHP3 utils");
        if (args.length < 2) {
        	Window.writeToConsole("Usage: java -jar mhtools.jar --extract <path to xxxx.bin> <decoder number>");
        	Window.writeToConsole("       java -jar mhtools.jar --rebuild <path to project folder> <encoder number>");
        	Window.writeToConsole("       java -jar mhtools.jar --decrypt <path to xxxx.bin>");
        	Window.writeToConsole("       java -jar mhtools.jar --encrypt <path to xxxx.bin>");
        	Window.writeToConsole("       java -jar mhtools.jar --dec-ext <path to xxxx.bin> <decoder number>");
        	Window.writeToConsole("       java -jar mhtools.jar --reb-enc <path to project folder> <encoder number>");
        	Window.writeToConsole("       java -jar mhtools.jar --gen-index <data.bin>");
        	Window.writeToConsole("       java -jar mhtools.jar --dec-all <data.bin> <path to output folder>");
        	Window.writeToConsole("       java -jar mhtools.jar --create-patch <xxxx.bin.enc> [ ... <xxxx.bin.enc>] <output_file>");
        	Window.writeToConsole("       java -jar mhtools.jar --decrypt-quest <mxxxxx.mib>");
        	Window.writeToConsole("       java -jar mhtools.jar --encrypt-quest <mxxxxx.mib>");
        	Window.writeToConsole("       java -jar mhtools.jar --extract-quests <xxxxxx.bin>");
        	Window.writeToConsole("       java -jar mhtools.jar --update-sha1 <mxxxxx.mib>");
        	Window.writeToConsole("       java -jar mhtools.jar --decrypt-save <xxxxx.bin>");
        	Window.writeToConsole("       java -jar mhtools.jar --encrypt-save <xxxxx.bin>");
        	Window.writeToConsole("       java -jar mhtools.jar --decrypt-kirk <xxxxx.bin>");
        	Window.writeToConsole("       java -jar mhtools.jar --encrypt-kirk <xxxxx.bin>");
        } else {
            if (args[0].equals("--extract")) {
            	if (args.length < 3) {
            		Window.writeToConsole("Decoder number missing. Aborting.");
            	}
            	else {
                	for(int i = 1; i < args.length-1; i++) {
                		extract(args[i], args[args.length-1]);
                	}	
            	}
                
            } else if (args[0].equals("--rebuild")) {
                if (args.length < 3) {
                	Window.writeToConsole("Decoder number missing. Aborting");
                } else {
                	 rebuild(args[1], args[2]);
                }
                
            } else if (args[0].equals("--decrypt")) {
                
                for(int i = 1; i < args.length; i++) {
                	new Decrypter().decrypt(args[i], args[i] + ".dec");
            	}
                
            } else if (args[0].equals("--encrypt")) {
                for(int i = 1; i < args.length; i++) {
                	new Encrypter().encrypt(args[i], args[i] + ".enc");
            	}
               
                
            } else if (args[0].equals("--dec-ext")) {
                if (args.length < 3) {
                	Window.writeToConsole("Decoder number missing. Aborting");
                } else {
                	new Decrypter().decrypt(args[1], args[1] + ".dec");
                    new File(args[1]).renameTo(new File(args[1] + ".tmp"));
                    new File(args[1] + ".dec").renameTo(new File(args[1]));
                    extract(args[1], args[2]);
                    new File(args[1]).delete();
                    new File(args[1] + ".tmp").renameTo(new File(args[1]));
                }
                
            } else if (args[0].equals("--reb-enc")) {
                if (args.length < 3) {
                	Window.writeToConsole("Decoder number missing. Aborting");
                } else {
                	rebuild(args[1], args[2]);
                    String filename = new File(args[1]).getName();
                    new Encrypter().encrypt(filename + ".bin.out", args[1]
                            + ".bin.enc");
                    Window.writeToConsole("Moving to " + args[1] + ".bin.enc");
                    new File(filename + ".bin.out").delete();
                }
                
               
                
            } else if (args[0].equals("--gen-index")) {
                new Decrypter().decrypt_index(args[1], null);
                
            } else if (args[0].equals("--dec-all")) {
                if (args.length < 3) {
                	Window.writeToConsole("Output folder missing. Aborting");
                } else {
                	 new Decrypter().decrypt_whole(args[1], args[2]);
                }
               
                
            } else if(args[0].equals("--create-patch")) {
            	List<String> files = new ArrayList<>();
            	File file;
            	
            	for(int i = 1; i < args.length-1; i++) {
            		file = new File(args[i]);
            		files.addAll(searchDirectory(file, ".enc"));
            	}

            	files.add(0, args[0]);
            	files.add(args[args.length-1]);
                new PatchBuilder().create(files.toArray(new String[files.size()]));
                
            } else if(args[0].equals("--encrypt-quest")) {
                new QuestCypher().encrypt(args[1]);
                
            } else if(args[0].equals("--decrypt-quest")) {
                new QuestCypher().decrypt(args[1]);
                
            } else if(args[0].equals("--extract-quests")) {
                new QuestCypher().extract(args[1]);
                
            } else if(args[0].equals("--update-sha1")) {
                new QuestCypher().update_sha1(args[1]);
                
            } else if(args[0].equals("--encrypt-save")) {
                new SavedataCypher().encrypt(args[1]);
                
            } else if(args[0].equals("--decrypt-save")) {
                new SavedataCypher().decrypt(args[1]);
                
            } else if(args[0].equals("--encrypt-kirk")) {
                new KirkCypher().encrypt(args[1]);
                
            } else if(args[0].equals("--decrypt-kirk")) {
                new KirkCypher().decrypt(args[1]);
                
            } else {
            	Window.writeToConsole("Unknown parameter: " + args[0]);
            }
        }
    }

    private static List<String> searchDirectory(File directory, String extension) {
    	List<String> files = new ArrayList<>();
    	
    	if(directory.isDirectory()) {
    		files.addAll(search(directory, extension));
    	} else if(directory.getName().endsWith(extension)) {
    		
    		files.add(directory.getAbsolutePath());
    		Window.writeToConsole("Added to patch file list: " + directory.getName());
    		
    	}
    	
    	return files;
    }
    
    private static List<String> search(File file, String ext) {
    	List<String> files = new ArrayList<>();
    	
    	if(file.isDirectory()) {
    		Window.writeToConsole("Searching directory ..." + file.getName());
    		
    		for(File f : file.listFiles()) {
    			if(f.isDirectory()) {
    				files.addAll(search(f, ext));
    			} else if(f.getName().endsWith(ext)){
    				files.add(f.getAbsolutePath());
    				Window.writeToConsole("Added to patch file list: " + f.getName());
    			}
    		}
    	}
    	
    	return files;
    }
    
    public static String checkFile(String filename) {
        try {
            BufferedReader file = new BufferedReader(new FileReader(filename));
            String name = file.readLine().split(" ")[0];
            file.close();
            return name;
        } catch (FileNotFoundException e) {
        	Window.writeToConsole(ExceptionUtils.getStackTrace(e));
            return null;
        } catch (IOException e) {
        	Window.writeToConsole(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}
