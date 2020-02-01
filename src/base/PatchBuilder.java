/*  MHTools - MH Utilities
    Copyright (C) 2011 Codestation

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
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class PatchBuilder {
    public void create(String[] args) {
        List<String> list = new ArrayList<String>(Arrays.asList(args));
        Window.writeToConsole(list.toString());
        list.remove(0);
        String outfile = list.get(list.size()-1);
        list.remove(list.size()-1);
        try {
            RandomAccessFile out = new RandomAccessFile(outfile, "rw");
            int table_size = (list.size() + 1) * 4 * 2;
            Window.writeToConsole("Table size: " + table_size + " bytes");
            if(table_size % 16 > 0) {
                table_size += 16 - (table_size % 16);
                Window.writeToConsole("Table size (padded): " + table_size + " bytes");
            }
            out.setLength(table_size);
            out.seek(0);
            long current = 0;
            MHUtils.writeInt(out, list.size());
            for (String file : list) {
                int offset = MHUtils.getOffset(MHUtils.extractNumber(file)) << 11;
                MHUtils.writeInt(out, offset);
                InputStream in = new FileInputStream(file);
                int len = (int)new File(file).length();
                int filler = ((MHUtils.getOffset(MHUtils.extractNumber(file)+1) << 11) - offset) - len;
                MHUtils.writeInt(out, len + filler);
                Window.writeToConsole(file + ", offset: " + offset  + ", size: " + (len + filler));
                if(filler > 0) {
                	Window.writeToConsole("> Adding " + filler  + " bytes of filler");
                }
                current = out.getFilePointer();
                byte buffer[] = new byte[1024];
                out.seek(out.length());
                while((len = in.read(buffer)) > 0)
                    out.write(buffer, 0, len);
                byte filler_arr[] = new byte[filler];
                out.write(filler_arr);
                in.close();
                out.seek(current);                
            }
            MHUtils.writeInt(out, 0);
            install_tables(out, list);
            out.close();
        } catch(FileNotFoundException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        } catch (EOFException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        }
    }
    
    private void install_tables(RandomAccessFile out, List<String> list) {
    	Window.writeToConsole("Trying to add data_install tables...");
        try {
            BufferedReader in = new BufferedReader(new FileReader("data_install.txt"));
            out.seek(out.length());
            
            String line;
            Vector<Integer> patch_files = new Vector<Integer>();
            Vector<String> install_files = new Vector<String>();
            Vector<String> offsets = new Vector<String>();
            
            while((line = in.readLine()) != null) {
                if(!line.startsWith("#")) {
                    String []tokens = line.split(",");
                    patch_files.add(Integer.parseInt(tokens[0]));
                    install_files.add(tokens[1]);
                    offsets.add(tokens[2]);
                }
            }
            Vector<String> install_uniq = new Vector<String>(new LinkedHashSet<String>(install_files));
            while(install_uniq.remove("NONE"));
            MHUtils.writeInt(out, install_uniq.size());
            int install_count = 0;
            String match = install_files.firstElement();
            out.write(match.getBytes());
            MHUtils.writeInt(out, install_count);
            for(String file : install_files) {
                if(!file.equals("NONE") && !match.equals(file)) {
                    out.write(file.getBytes());
                    match = file;
                    MHUtils.writeInt(out, install_count);
                }
                install_count++;
            }
            for(String trans_file : list) {
                int index = patch_files.indexOf(MHUtils.extractNumber(trans_file));
                int offset = -1;
                if(index != -1) {
                    // UGH!!, 11 years and the Integer.parseInt bug isn't fixed
                    // This is the last time that i use Java in a project of mine
                    // http://bugs.sun.com/view_bug.do?bug_id=4215269
                	
                	//Since this has been fixed I set it to Integer :D ~ eclipse
                    offset = Integer.parseInt(offsets.get(index),16);
                } else {
                	Window.writeToConsole("Install info not found for " + trans_file);
                }
                MHUtils.writeInt(out, offset);
            }            
            long filelength = out.length();
            if(filelength % 16 > 0) {
                filelength += 16 - (filelength % 16);
                out.setLength(filelength);
            }
            Window.writeToConsole("Finished");
        } catch (FileNotFoundException e) {
        	Window.writeToConsole("data_install.txt not found");
        } catch (IOException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        }
    }
}
